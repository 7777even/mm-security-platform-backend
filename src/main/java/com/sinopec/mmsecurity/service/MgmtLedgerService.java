package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.annotation.RealtimeSync;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.MgmtLedgerCellDto;
import com.sinopec.mmsecurity.dto.MgmtLedgerCellWriteDto;
import com.sinopec.mmsecurity.dto.MgmtLedgerFilterDto;
import com.sinopec.mmsecurity.dto.MgmtLedgerListResult;
import com.sinopec.mmsecurity.dto.MgmtLedgerMetaDto;
import com.sinopec.mmsecurity.dto.MgmtLedgerRowWriteRequest;
import com.sinopec.mmsecurity.entity.MgmtLedgerCell;
import com.sinopec.mmsecurity.entity.MgmtLedgerMeta;
import com.sinopec.mmsecurity.entity.MgmtLedgerRow;
import com.sinopec.mmsecurity.mapper.MgmtLedgerCellMapper;
import com.sinopec.mmsecurity.mapper.MgmtLedgerMetaMapper;
import com.sinopec.mmsecurity.mapper.MgmtLedgerRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理台账通用服务：只读查询 + 行级写操作（新增/更新/删除）。
 * 数据来自 mgmt_ledger_* 三张表（V51 种子化自 mgmtMenus 静态数据）。
 * 写操作在同一事务内维护行与单元格；主键与排序号由 {@link LedgerIdSupport} 显式分配，
 * 规避 Flyway 种子显式插 id 导致的自增序列滞后、新增撞主键（409「数据冲突」）。
 */
@Service
@RequiredArgsConstructor
public class MgmtLedgerService {

    private final MgmtLedgerMetaMapper metaMapper;
    private final MgmtLedgerRowMapper rowMapper;
    private final MgmtLedgerCellMapper cellMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MgmtLedgerMetaDto meta(String domain) {
        MgmtLedgerMeta meta = metaMapper.selectOne(
                new LambdaQueryWrapper<MgmtLedgerMeta>().eq(MgmtLedgerMeta::getDomain, domain));
        if (meta == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "台账不存在: " + domain);
        }
        return toMetaDto(meta);
    }

    public MgmtLedgerListResult list(String domain, int page, int size,
                                     String keyword, Map<String, String> filters) {
        MgmtLedgerMetaDto metaDto = meta(domain);
        List<String> columns = metaDto.getColumns();

        // 命中行主键：列筛选(relational-division) + 关键字(LIKE) 全部在 DB 完成，仅取主键集合
        List<Long> matched = null;
        boolean hasKeyword = StringUtils.hasText(keyword);
        if (filters != null && !filters.isEmpty()) {
            List<MgmtLedgerCellMapper.ColVal> clauses = new ArrayList<>();
            for (Map.Entry<String, String> e : filters.entrySet()) {
                String val = e.getValue();
                if (!StringUtils.hasText(val) || "全部".equals(val) || "全部中队".equals(val)) {
                    continue; // 占位值忽略（与旧行为一致）
                }
                clauses.add(new MgmtLedgerCellMapper.ColVal(e.getKey(), val));
            }
            if (!clauses.isEmpty()) {
                matched = cellMapper.selectRowIdsMatchingAllFilters(domain, clauses, clauses.size());
            }
        }
        if (hasKeyword) {
            List<Long> kwIds = cellMapper.selectRowIdsByKeyword(domain, "%" + escapeLike(keyword) + "%");
            matched = (matched == null) ? kwIds : intersect(matched, kwIds);
        }

        MgmtLedgerListResult result = new MgmtLedgerListResult();
        result.setColumns(columns);
        result.setFilters(metaDto.getFilters());
        result.setPage(page);
        result.setSize(size);

        if (matched != null && matched.isEmpty()) {
            result.setRows(new ArrayList<>());
            result.setRowIds(new ArrayList<>());
            result.setTotal(0);
            return result;
        }

        // DB 分页：仅取命中行（无筛选/关键字时直接按 domain 分页），再取本页单元格
        Page<MgmtLedgerRow> pg = new Page<>(page, size);
        LambdaQueryWrapper<MgmtLedgerRow> qw = new LambdaQueryWrapper<MgmtLedgerRow>()
                .eq(MgmtLedgerRow::getDomain, domain)
                .orderByAsc(MgmtLedgerRow::getRowNo);
        if (matched != null) {
            qw.in(MgmtLedgerRow::getId, matched);
        }
        rowMapper.selectPage(pg, qw);
        List<MgmtLedgerRow> rows = pg.getRecords();

        if (rows.isEmpty()) {
            result.setRows(new ArrayList<>());
            result.setRowIds(new ArrayList<>());
            result.setTotal(matched == null ? 0 : matched.size());
            return result;
        }

        List<Long> pageRowIds = rows.stream().map(MgmtLedgerRow::getId).collect(Collectors.toList());
        List<MgmtLedgerCell> cells = cellMapper.selectList(
                new LambdaQueryWrapper<MgmtLedgerCell>()
                        .in(MgmtLedgerCell::getRowId, pageRowIds)
                        .orderByAsc(MgmtLedgerCell::getColIndex));
        Map<Long, List<MgmtLedgerCell>> cellMap = new LinkedHashMap<>();
        for (MgmtLedgerCell c : cells) {
            cellMap.computeIfAbsent(c.getRowId(), k -> new ArrayList<>()).add(c);
        }

        // RowView 绑定「行 id + 单元格」，保证 rowIds 与筛选/分页后的显示行严格对齐
        List<RowView> pageViews = new ArrayList<>();
        for (MgmtLedgerRow row : rows) {
            List<MgmtLedgerCell> rowCells = cellMap.getOrDefault(row.getId(), new ArrayList<>());
            pageViews.add(new RowView(row.getId(),
                    rowCells.stream().map(this::toCellDto).collect(Collectors.toList())));
        }

        result.setRows(pageViews.stream().map(r -> r.cells).collect(Collectors.toList()));
        result.setRowIds(pageViews.stream().map(r -> r.id).collect(Collectors.toList()));
        result.setTotal(matched == null ? pg.getTotal() : matched.size());
        return result;
    }

    /** LIKE 转义：% / _ / \\ 前加转义符，避免被当作通配符。 */
    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /** 两个主键集合求交集（用于「列筛选 ∩ 关键字」）。 */
    private static List<Long> intersect(List<Long> a, List<Long> b) {
        Set<Long> set = new HashSet<>(a);
        set.retainAll(b);
        return new ArrayList<>(set);
    }

    /**
     * 新增台账行（含单元格）。行与单元格在同一事务内写入；返回新行主键。
     * 主键/排序号由 {@link LedgerIdSupport} 显式分配（max+1），规避自增序列滞后。
     */
    @Transactional
    @RealtimeSync(domain = "mgmt-ledger")
    public long createRow(String domain, MgmtLedgerRowWriteRequest req) {
        List<String> columns = meta(domain).getColumns();
        MgmtLedgerRow row = new MgmtLedgerRow();
        row.setId(LedgerIdSupport.nextId(rowMapper, MgmtLedgerRow::getId, MgmtLedgerRow::getId));
        row.setDomain(domain);
        int order = LedgerIdSupport.nextSortNo(rowMapper, MgmtLedgerRow::getRowNo, MgmtLedgerRow::getRowNo);
        row.setRowNo(order);
        row.setSortNo(order);
        rowMapper.insert(row);
        insertCells(row.getId(), columns, req.getCells());
        return row.getId();
    }

    /**
     * 更新台账行：按 rowId 定位（须属于该 domain），删除旧单元格后按请求重写。
     * 行本身不动（保持 rowNo/排序稳定），仅刷新单元格内容。
     */
    @Transactional
    @RealtimeSync(domain = "mgmt-ledger")
    public void updateRow(String domain, long rowId, MgmtLedgerRowWriteRequest req) {
        MgmtLedgerRow row = rowMapper.selectById(rowId);
        if (row == null || !domain.equals(row.getDomain())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "台账行不存在: " + rowId);
        }
        List<String> columns = meta(domain).getColumns();
        cellMapper.delete(new LambdaQueryWrapper<MgmtLedgerCell>().eq(MgmtLedgerCell::getRowId, rowId));
        insertCells(rowId, columns, req.getCells());
    }

    /** 删除台账行（含其单元格）。行不存在或不属于该 domain 返回 NOT_FOUND。广播 mgmt-ledger.changed 触发三端实时刷新。 */
    @Transactional
    @RealtimeSync(domain = "mgmt-ledger")
    public void deleteRow(String domain, long rowId) {
        MgmtLedgerRow row = rowMapper.selectById(rowId);
        if (row == null || !domain.equals(row.getDomain())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "台账行不存在: " + rowId);
        }
        cellMapper.delete(new LambdaQueryWrapper<MgmtLedgerCell>().eq(MgmtLedgerCell::getRowId, rowId));
        rowMapper.deleteById(rowId);
    }

    private void insertCells(Long rowId, List<String> columns, List<MgmtLedgerCellWriteDto> cells) {
        if (cells == null) return;
        for (MgmtLedgerCellWriteDto c : cells) {
            if (c.getColIndex() == null) continue;
            MgmtLedgerCell cell = new MgmtLedgerCell();
            cell.setId(LedgerIdSupport.nextId(cellMapper, MgmtLedgerCell::getId, MgmtLedgerCell::getId));
            cell.setRowId(rowId);
            cell.setColIndex(c.getColIndex());
            cell.setColKey(c.getColIndex() < columns.size() ? columns.get(c.getColIndex()) : null);
            cell.setCellText(c.getText());
            cell.setCellType(c.getType());
            cellMapper.insert(cell);
        }
    }

    private MgmtLedgerMetaDto toMetaDto(MgmtLedgerMeta meta) {
        MgmtLedgerMetaDto dto = new MgmtLedgerMetaDto();
        dto.setDomain(meta.getDomain());
        dto.setTitle(meta.getTitle());
        try {
            dto.setColumns(objectMapper.readValue(meta.getColumnsJson(), new TypeReference<List<String>>() {
            }));
        } catch (Exception ex) {
            dto.setColumns(new ArrayList<>());
        }
        try {
            dto.setFilters(objectMapper.readValue(meta.getFilterJson(), new TypeReference<List<MgmtLedgerFilterDto>>() {
            }));
        } catch (Exception ex) {
            dto.setFilters(new ArrayList<>());
        }
        return dto;
    }

    private MgmtLedgerCellDto toCellDto(MgmtLedgerCell c) {
        MgmtLedgerCellDto dto = new MgmtLedgerCellDto();
        dto.setText(c.getCellText());
        dto.setType(c.getCellType());
        return dto;
    }

    /** 行视图：绑定行主键与单元格列表，使 rowIds 与显示行一一对应。 */
    private static final class RowView {
        final Long id;
        final List<MgmtLedgerCellDto> cells;

        RowView(Long id, List<MgmtLedgerCellDto> cells) {
            this.id = id;
            this.cells = cells;
        }
    }
}
