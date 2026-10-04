package com.sinopec.mmsecurity.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 三方言真机 SQL 复核 —— PostgreSQL（Testcontainers）。
 *
 * <p>对应 #1 列表下推优化的两条手写 SQL（relational-division 列筛选 + LIKE ESCAPE 关键字），
 * 在<b>真实 PG</b> 上实跑并断言结果，弥补 {@code MgmtLedgerListIntegrationIT}(H2) 只验 H2 的缺口。
 * H2 通过 ≠ PG 通过；本测试是「真库复核」在 PG 上的落地。</p>
 *
 * <p>无 Docker 时 {@code assumeTrue(DockerClientFactory.instance().isDockerAvailable())} 跳过，
 * 保证 {@code mvn test} 在无容器环境仍绿；daemon 就绪后自动真实运行。</p>
 */
class MgmtLedgerSqlPostgresqlIT {

    private static final String[][] DATA = {
        {"R1", "储罐A", "立式"},
        {"R2", "储罐B", "卧式"},
        {"R3", "罐区C", "立式"},
        {"R4", "含%特殊", "卧式"},
        {"R5", "含ABC特殊", "卧式"},
    };
    private static final String[] COLS = {"编号", "名称", "类型"};

    @Test
    void relationalDivisionAndLikeEscape_onRealPostgres() throws Exception {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
                "Docker daemon 未运行：跳过 PG 真机 SQL 验证（daemon 就绪后自动运行）");

        try (PostgreSQLContainer<?> pg = new PostgreSQLContainer<>(
                DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("mm_security").withUsername("mm").withPassword("mm")) {
            pg.start();
            Flyway.configure()
                    .dataSource(pg.getJdbcUrl(), pg.getUsername(), pg.getPassword())
                    .locations("classpath:db/migration/postgresql")
                    .load()
                    .migrate();

            try (Connection conn = DriverManager.getConnection(
                    pg.getJdbcUrl(), pg.getUsername(), pg.getPassword());
                 Statement st = conn.createStatement()) {
                seed(st);

                // 列筛选 relational-division：双条件须全满足（HAVING COUNT(DISTINCT col_key)=2）
                String relDiv = "SELECT c.row_id FROM mgmt_ledger_cell c "
                        + "JOIN mgmt_ledger_row r ON r.id = c.row_id "
                        + "WHERE r.domain_code = 'it-ledger-list' AND ("
                        + "(c.col_key = '类型' AND c.cell_text = '立式') "
                        + "OR (c.col_key = '名称' AND c.cell_text = '储罐A')) "
                        + "GROUP BY c.row_id HAVING COUNT(DISTINCT c.col_key) = 2";
                assertEquals(1, countRows(st, relDiv), "PG：relational-division 双条件应命中 R1");

                // 关键字 LIKE 转义：% 必须作字面量 → 仅命中 R4(含%特殊)，不能通配到 R5(含ABC特殊)
                String likeEsc = "SELECT DISTINCT c.row_id FROM mgmt_ledger_cell c "
                        + "JOIN mgmt_ledger_row r ON r.id = c.row_id "
                        + "WHERE r.domain_code = 'it-ledger-list' "
                        + "AND c.cell_text LIKE '%含\\%%' ESCAPE '\\'";
                assertEquals(1, countRows(st, likeEsc), "PG：LIKE 转义 % 应字面匹配仅 R4");

                // 普通子串关键字（不触发转义歧义）
                String likePlain = "SELECT DISTINCT c.row_id FROM mgmt_ledger_cell c "
                        + "JOIN mgmt_ledger_row r ON r.id = c.row_id "
                        + "WHERE r.domain_code = 'it-ledger-list' "
                        + "AND c.cell_text LIKE '%ABC%'";
                assertEquals(1, countRows(st, likePlain), "PG：普通子串 ABC 应仅命中 R5");
            }
        }
    }

    private void seed(Statement st) throws Exception {
        st.execute("INSERT INTO mgmt_ledger_meta(id,domain_code,title,columns_json,filter_json,sort_no) "
                + "VALUES (9001,'it-ledger-list','IT设施测试','[\"编号\",\"名称\",\"类型\"]','[]',9001)");
        long rowId = 9101;
        for (String[] d : DATA) {
            st.execute("INSERT INTO mgmt_ledger_row(id,domain_code,row_no,sort_no) VALUES ("
                    + rowId + ",'it-ledger-list'," + (rowId - 9100) + "," + (rowId - 9100) + ")");
            for (int ci = 0; ci < COLS.length; ci++) {
                st.execute("INSERT INTO mgmt_ledger_cell(id,row_id,col_index,col_key,cell_text,cell_type) VALUES ("
                        + (rowId * 10 + ci) + "," + rowId + "," + ci + ",'"
                        + COLS[ci] + "','" + d[ci] + "',null)");
            }
            rowId++;
        }
    }

    private int countRows(Statement st, String sql) throws Exception {
        try (ResultSet rs = st.executeQuery(sql)) {
            int n = 0;
            while (rs.next()) n++;
            return n;
        }
    }
}
