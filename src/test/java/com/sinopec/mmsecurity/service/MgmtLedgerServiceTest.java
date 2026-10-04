package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.MgmtLedgerCellDto;
import com.sinopec.mmsecurity.dto.MgmtLedgerListResult;
import com.sinopec.mmsecurity.entity.MgmtLedgerCell;
import com.sinopec.mmsecurity.entity.MgmtLedgerMeta;
import com.sinopec.mmsecurity.entity.MgmtLedgerRow;
import com.sinopec.mmsecurity.mapper.MgmtLedgerCellMapper;
import com.sinopec.mmsecurity.mapper.MgmtLedgerMetaMapper;
import com.sinopec.mmsecurity.mapper.MgmtLedgerRowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MgmtLedgerServiceTest {

    @Mock
    private MgmtLedgerMetaMapper metaMapper;
    @Mock
    private MgmtLedgerRowMapper rowMapper;
    @Mock
    private MgmtLedgerCellMapper cellMapper;

    @InjectMocks
    private MgmtLedgerService service;

    private static final String DOMAIN = "alarm-config";

    @BeforeEach
    void setUp() {
        MgmtLedgerMeta meta = new MgmtLedgerMeta();
        meta.setDomain(DOMAIN);
        meta.setTitle("告警配置");
        meta.setColumnsJson("[\"名称\",\"状态\",\"备注\"]");
        meta.setFilterJson("[]");
        when(metaMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(meta);

        // selectPage 副作用：把伪造行写入 page（records + total）
        doAnswer(inv -> {
            Page<MgmtLedgerRow> pg = inv.getArgument(0);
            pg.setRecords(fakeRows());
            pg.setTotal(fakeRows().size());
            return pg;
        }).when(rowMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        // 单元格：每页 3 行，每行 3 列
        when(cellMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(fakeCells());
    }

    private List<MgmtLedgerRow> fakeRows() {
        List<MgmtLedgerRow> rows = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            MgmtLedgerRow r = new MgmtLedgerRow();
            r.setId(i);
            r.setDomain(DOMAIN);
            r.setRowNo((int) i);
            r.setSortNo((int) i);
            rows.add(r);
        }
        return rows;
    }

    private List<MgmtLedgerCell> fakeCells() {
        List<MgmtLedgerCell> cells = new ArrayList<>();
        for (long rid = 1; rid <= 3; rid++) {
            String[] texts = {"A", "B", "C"};
            for (int c = 0; c < 3; c++) {
                MgmtLedgerCell cell = new MgmtLedgerCell();
                cell.setId(rid * 10 + c);
                cell.setRowId(rid);
                cell.setColIndex(c);
                cell.setColKey(texts[c].equals("A") ? "名称" : texts[c].equals("B") ? "状态" : "备注");
                cell.setCellText(texts[c]);
                cell.setCellType(null);
                cells.add(cell);
            }
        }
        return cells;
    }

    private List<String> firstRowTexts(MgmtLedgerListResult res) {
        return res.getRows().get(0).stream().map(MgmtLedgerCellDto::getText).collect(Collectors.toList());
    }

    @Test
    @DisplayName("无筛选无关键字：直接按 domain 分页，不扫单元格匹配")
    void list_noFilter_noKeyword_paginatesByDomain() {
        MgmtLedgerListResult res = service.list(DOMAIN, 1, 20, null, Collections.emptyMap());

        assertEquals(3, res.getTotal());
        assertEquals(3, res.getRows().size());
        assertEquals(List.of(1L, 2L, 3L), res.getRowIds());
        // 首行单元格文本按列序组装
        assertEquals(List.of("A", "B", "C"), firstRowTexts(res));

        verify(cellMapper, never()).selectRowIdsMatchingAllFilters(anyString(), anyList(), anyInt());
        verify(cellMapper, never()).selectRowIdsByKeyword(anyString(), anyString());
        verify(rowMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("仅关键字：走 selectRowIdsByKeyword，total=命中数，LIKE 转义 %/_")
    void list_keyword_only_usesKeywordQuery() {
        when(cellMapper.selectRowIdsByKeyword(anyString(), anyString())).thenReturn(List.of(1L, 3L));
        ArgumentCaptor<String> patCap = ArgumentCaptor.forClass(String.class);

        MgmtLedgerListResult res = service.list(DOMAIN, 1, 20, "foo_bar%baz", Collections.emptyMap());

        assertEquals(2, res.getTotal());
        verify(cellMapper).selectRowIdsByKeyword(eq(DOMAIN), patCap.capture());
        // 转义后两端加 %，且 % _ \\ 均被转义
        assertEquals("%foo\\_bar\\%baz%", patCap.getValue());
    }

    @Test
    @DisplayName("仅列筛选：走 selectRowIdsMatchingAllFilters，clauses 数=筛选条件数")
    void list_filter_only_usesFilterQuery() {
        when(cellMapper.selectRowIdsMatchingAllFilters(anyString(), anyList(), anyInt())).thenReturn(List.of(2L));
        ArgumentCaptor<List<MgmtLedgerCellMapper.ColVal>> clausesCap = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Integer> countCap = ArgumentCaptor.forClass(Integer.class);

        Map<String, String> filters = Map.of("状态", "B", "备注", "C");
        MgmtLedgerListResult res = service.list(DOMAIN, 1, 20, null, filters);

        assertEquals(1, res.getTotal());
        verify(cellMapper).selectRowIdsMatchingAllFilters(eq(DOMAIN), clausesCap.capture(), countCap.capture());
        assertEquals(2, clausesCap.getValue().size());
        assertEquals(2, countCap.getValue());
    }

    @Test
    @DisplayName("关键字+列筛选：两者交集")
    void list_keyword_and_filter_intersect() {
        when(cellMapper.selectRowIdsByKeyword(anyString(), anyString())).thenReturn(List.of(1L, 2L, 3L));
        when(cellMapper.selectRowIdsMatchingAllFilters(anyString(), anyList(), anyInt())).thenReturn(List.of(2L, 3L));

        MgmtLedgerListResult res = service.list(DOMAIN, 1, 20, "x", Map.of("状态", "B"));

        assertEquals(2, res.getTotal()); // {1,2,3} ∩ {2,3} = {2,3}
    }

    @Test
    @DisplayName("命中为空：短路返回空页，不再查行/单元格")
    void list_noMatch_shortCircuits() {
        when(cellMapper.selectRowIdsByKeyword(anyString(), anyString())).thenReturn(Collections.emptyList());

        MgmtLedgerListResult res = service.list(DOMAIN, 1, 20, "nope", Collections.emptyMap());

        assertEquals(0, res.getTotal());
        assertTrue(res.getRows().isEmpty());
        verify(rowMapper, never()).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("占位筛选值(全部)被忽略，退化为按 domain 分页")
    void list_placeholderFilter_ignored() {
        MgmtLedgerListResult res = service.list(DOMAIN, 1, 20, null, Map.of("状态", "全部"));

        assertEquals(3, res.getTotal());
        verify(cellMapper, never()).selectRowIdsMatchingAllFilters(anyString(), anyList(), anyInt());
    }
}
