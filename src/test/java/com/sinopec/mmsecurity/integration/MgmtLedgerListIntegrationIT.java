package com.sinopec.mmsecurity.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.entity.MgmtLedgerCell;
import com.sinopec.mmsecurity.entity.MgmtLedgerMeta;
import com.sinopec.mmsecurity.entity.MgmtLedgerRow;
import com.sinopec.mmsecurity.mapper.MgmtLedgerCellMapper;
import com.sinopec.mmsecurity.mapper.MgmtLedgerMetaMapper;
import com.sinopec.mmsecurity.mapper.MgmtLedgerRowMapper;
import com.sinopec.mmsecurity.service.MgmtLedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 管理台账列表查询「真实 DB」集成测试（H2）—— 对应 #1 列表下推 DB 分页优化。
 *
 * <p>既有 {@code MgmtLedgerServiceTest} 用 Mockito 桩住 Mapper，只验「编排」不验「真实 SQL」。
 * 本测试启动真实 Spring 上下文 + 内存 H2（Flyway 应用 db/migration/h2 全部 V 文件），真正执行
 * {@code MgmtLedgerCellMapper} 的两条手写 SQL：</p>
 * <ul>
 *   <li>列筛选 relational-division：{@code GROUP BY row_id HAVING COUNT(DISTINCT col_key) = 条件数}</li>
 *   <li>关键字模糊：{@code LIKE ... ESCAPE '\'}，且 service 侧 {@code escapeLike} 转义 %/_/\\</li>
 * </ul>
 *
 * <p>在 H2 上跑通证明 SQL 可实际执行、逻辑正确；PG/DM 真库执行见
 * {@code MgmtLedgerListPostgresqlIT}（Testcontainers，需 Docker）。H2 通过 ≠ PG/DM 通过，
 * 但本测试已把「只验编排」升级为「验真 SQL」。</p>
 */
@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:mm_security_ledger;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
class MgmtLedgerListIntegrationIT {

    private static final String DOMAIN = "it-ledger-list";
    private static final List<String> COLS = List.of("编号", "名称", "类型");

    @Autowired
    private MgmtLedgerService ledgerService;
    @Autowired
    private MgmtLedgerMetaMapper metaMapper;
    @Autowired
    private MgmtLedgerRowMapper rowMapper;
    @Autowired
    private MgmtLedgerCellMapper cellMapper;

    /** 每用例前重置测试域数据，保证独立：R1/R2/R3 普通，R4=含%特殊、R5=含ABC特殊（LIKE 转义判别行）。 */
    @BeforeEach
    void seed() {
        List<MgmtLedgerRow> existing = rowMapper.selectList(
                new LambdaQueryWrapper<MgmtLedgerRow>().eq(MgmtLedgerRow::getDomain, DOMAIN));
        if (!existing.isEmpty()) {
            List<Long> ids = existing.stream().map(MgmtLedgerRow::getId).collect(Collectors.toList());
            cellMapper.delete(new LambdaQueryWrapper<MgmtLedgerCell>().in(MgmtLedgerCell::getRowId, ids));
            rowMapper.delete(new LambdaQueryWrapper<MgmtLedgerRow>().in(MgmtLedgerRow::getId, ids));
        }
        metaMapper.delete(new LambdaQueryWrapper<MgmtLedgerMeta>().eq(MgmtLedgerMeta::getDomain, DOMAIN));

        MgmtLedgerMeta meta = new MgmtLedgerMeta();
        meta.setId(9001L);
        meta.setDomain(DOMAIN);
        meta.setTitle("IT设施测试");
        meta.setColumnsJson("[\"编号\",\"名称\",\"类型\"]");
        meta.setFilterJson("[]");
        meta.setSortNo(9001);
        metaMapper.insert(meta);

        String[][] data = {
            {"R1", "储罐A", "立式"},
            {"R2", "储罐B", "卧式"},
            {"R3", "罐区C", "立式"},
            {"R4", "含%特殊", "卧式"},
            {"R5", "含ABC特殊", "卧式"},
        };
        long rowId = 9101;
        for (String[] d : data) {
            MgmtLedgerRow r = new MgmtLedgerRow();
            r.setId(rowId);
            r.setDomain(DOMAIN);
            long no = rowId - 9100;
            r.setRowNo((int) no);
            r.setSortNo((int) no);
            rowMapper.insert(r);
            for (int ci = 0; ci < COLS.size(); ci++) {
                MgmtLedgerCell c = new MgmtLedgerCell();
                c.setId(rowId * 10 + ci);
                c.setRowId(rowId);
                c.setColIndex(ci);
                c.setColKey(COLS.get(ci));
                c.setCellText(d[ci]);
                c.setCellType(null);
                cellMapper.insert(c);
            }
            rowId++;
        }
    }

    @Test
    void noFilter_returnsAllRows() {
        var res = ledgerService.list(DOMAIN, 1, 20, null, null);
        assertEquals(5, res.getTotal());
        assertEquals(5, res.getRows().size());
    }

    @Test
    void keyword_single_match() {
        var res = ledgerService.list(DOMAIN, 1, 20, "储罐", null);
        assertEquals(2, res.getTotal(), "关键字『储罐』应命中 R1/R2（罐区C/含%特殊/含ABC特殊 不含）");
    }

    @Test
    void columnFilter_single() {
        var res = ledgerService.list(DOMAIN, 1, 20, null, Map.of("类型", "立式"));
        assertEquals(2, res.getTotal(), "列筛选『类型=立式』应命中 R1/R3");
    }

    @Test
    void columnFilter_twoConditions_relationalDivision() {
        // 双列筛选 → relational-division 要求两条件都满足（HAVING COUNT(DISTINCT col_key)=2）
        var res = ledgerService.list(DOMAIN, 1, 20, null, Map.of("类型", "立式", "名称", "储罐A"));
        assertEquals(1, res.getTotal(), "双列筛选『类型=立式 AND 名称=储罐A』应仅命中 R1");
    }

    @Test
    void keywordIntersectColumnFilter() {
        var res = ledgerService.list(DOMAIN, 1, 20, "罐", Map.of("类型", "立式"));
        assertEquals(2, res.getTotal(), "关键字『罐』∩『类型=立式』应命中 R1(储罐A)/R3(罐区C)");
    }

    @Test
    void columnFilter_noMatch_emptyResult() {
        var res = ledgerService.list(DOMAIN, 1, 20, null, Map.of("类型", "不存在Z"));
        assertEquals(0, res.getTotal());
        assertTrue(res.getRows().isEmpty());
    }

    @Test
    void columnFilter_placeholderIgnored() {
        // 占位值『全部』应被忽略，等价于无筛选
        var res = ledgerService.list(DOMAIN, 1, 20, null, Map.of("类型", "全部"));
        assertEquals(5, res.getTotal(), "占位值『全部』应忽略，返回全部");
    }

    @Test
    void keyword_likeEscape_percentIsLiteral() {
        // 关键判别：关键字『含%』若 escapeLike/ESCAPE 生效，% 当作字面量 → 仅命中 R4(含%特殊)；
        // 若未转义，% 作通配符 → 命中 R4+R5（含ABC特殊）。H2 跑通证明转义链路正确。
        var res = ledgerService.list(DOMAIN, 1, 20, "含%", null);
        assertEquals(1, res.getTotal(), "LIKE 转义应使 % 字面匹配，仅命中 R4(含%特殊)");
    }

    @Test
    void keyword_normalSubstringStillWorks() {
        var res = ledgerService.list(DOMAIN, 1, 20, "ABC", null);
        assertEquals(1, res.getTotal(), "普通子串『ABC』应仅命中 R5(含ABC特殊)");
    }
}
