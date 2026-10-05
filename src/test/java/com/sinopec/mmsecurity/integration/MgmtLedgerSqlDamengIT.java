package com.sinopec.mmsecurity.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 三方言真机 SQL 复核 —— 达梦 DM8（环境变量门控）。
 *
 * <p>与 {@code MgmtLedgerSqlPostgresqlIT} 同口径，在<b>真实 DM8</b> 上实跑 #1 列表下推的两条手写 SQL
 * （relational-division 列筛选 + LIKE ESCAPE 关键字）并断言。DM8 无公开 Testcontainers 镜像、
 * 驱动需 {@code -Pdm} + 本地 jar 注入，故以环境变量 {@code DAMENG_JDBC_URL} 门控；未提供时跳过，
 * 保证 {@code mvn test} 在无达梦实例环境仍绿。</p>
 */
class MgmtLedgerSqlDamengIT {

    private static final String[][] DATA = {
        {"R1", "储罐A", "立式"},
        {"R2", "储罐B", "卧式"},
        {"R3", "罐区C", "立式"},
        {"R4", "含%特殊", "卧式"},
        {"R5", "含ABC特殊", "卧式"},
    };
    private static final String[] COLS = {"编号", "名称", "类型"};

    @Test
    void relationalDivisionAndLikeEscape_onRealDameng() {
        String url = System.getenv("DAMENG_JDBC_URL");
        assumeTrue(url != null && !url.isBlank(),
                "未配置 DAMENG_JDBC_URL：跳达梦真机 SQL 验证（须 -Pdm + 本地驱动 jar + 达梦实例）");

        String user = System.getenv().getOrDefault("DAMENG_JDBC_USER", "SYSDBA");
        String password = System.getenv().getOrDefault("DAMENG_JDBC_PASSWORD", "SYSDBA");

        Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration/dameng")
                .load()
                .migrate();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement st = conn.createStatement()) {
            seed(st);

            // 列筛选 relational-division：双条件须全满足（HAVING COUNT(DISTINCT col_key)=2）
            String relDiv = "SELECT c.row_id FROM mgmt_ledger_cell c "
                    + "JOIN mgmt_ledger_row r ON r.id = c.row_id "
                    + "WHERE r.domain_code = 'it-ledger-list' AND ("
                    + "(c.col_key = '类型' AND c.cell_text = '立式') "
                    + "OR (c.col_key = '名称' AND c.cell_text = '储罐A')) "
                    + "GROUP BY c.row_id HAVING COUNT(DISTINCT c.col_key) = 2";
            assertEquals(1, countRows(st, relDiv), "DM：relational-division 双条件应命中 R1");

            // 关键字 LIKE 转义：% 必须作字面量 → 仅命中 R4(含%特殊)
            String likeEsc = "SELECT DISTINCT c.row_id FROM mgmt_ledger_cell c "
                    + "JOIN mgmt_ledger_row r ON r.id = c.row_id "
                    + "WHERE r.domain_code = 'it-ledger-list' "
                    + "AND c.cell_text LIKE '%含\\%%' ESCAPE '\\'";
            assertEquals(1, countRows(st, likeEsc), "DM：LIKE 转义 % 应字面匹配仅 R4");

            // 普通子串关键字
            String likePlain = "SELECT DISTINCT c.row_id FROM mgmt_ledger_cell c "
                    + "JOIN mgmt_ledger_row r ON r.id = c.row_id "
                    + "WHERE r.domain_code = 'it-ledger-list' "
                    + "AND c.cell_text LIKE '%ABC%'";
            assertEquals(1, countRows(st, likePlain), "DM：普通子串 ABC 应仅命中 R5");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void seed(Statement st) throws Exception {
        // 达梦是本机持久实例（不像 PG 每次用全新容器），重复跑本 IT 会残留上一轮种子，
        // 直接 INSERT 会撞唯一性约束。故先按 id 区间清理，保证幂等、可反复执行。
        // 顺序须先子后父：cell -> row -> meta。
        st.execute("DELETE FROM mgmt_ledger_cell WHERE row_id >= 9101");
        st.execute("DELETE FROM mgmt_ledger_row WHERE id >= 9101");
        st.execute("DELETE FROM mgmt_ledger_meta WHERE id = 9001");

        // DM8：这些表 id 均为 IDENTITY 自增列，显式给 id 必须先用 SET IDENTITY_INSERT 打开
        // （会话级、同一时刻只能对一张表 ON），插完立刻 OFF。
        st.execute("SET IDENTITY_INSERT mgmt_ledger_meta ON");
        st.execute("INSERT INTO mgmt_ledger_meta(id,domain_code,title,columns_json,filter_json,sort_no) "
                + "VALUES (9001,'it-ledger-list','IT设施测试','[\"编号\",\"名称\",\"类型\"]','[]',9001)");
        st.execute("SET IDENTITY_INSERT mgmt_ledger_meta OFF");

        long rowId = 9101;
        st.execute("SET IDENTITY_INSERT mgmt_ledger_row ON");
        for (int ri = 0; ri < DATA.length; ri++) {
            long id = rowId + ri;
            st.execute("INSERT INTO mgmt_ledger_row(id,domain_code,row_no,sort_no) VALUES ("
                    + id + ",'it-ledger-list'," + (id - 9100) + "," + (id - 9100) + ")");
        }
        st.execute("SET IDENTITY_INSERT mgmt_ledger_row OFF");

        st.execute("SET IDENTITY_INSERT mgmt_ledger_cell ON");
        for (int ri = 0; ri < DATA.length; ri++) {
            long id = rowId + ri;
            for (int ci = 0; ci < COLS.length; ci++) {
                st.execute("INSERT INTO mgmt_ledger_cell(id,row_id,col_index,col_key,cell_text,cell_type) VALUES ("
                        + (id * 10 + ci) + "," + id + "," + ci + ",'"
                        + COLS[ci] + "','" + DATA[ri][ci] + "',null)");
            }
        }
        st.execute("SET IDENTITY_INSERT mgmt_ledger_cell OFF");
    }

    private int countRows(Statement st, String sql) throws Exception {
        try (ResultSet rs = st.executeQuery(sql)) {
            int n = 0;
            while (rs.next()) n++;
            return n;
        }
    }
}
