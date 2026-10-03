package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        List<MgmtLedgerRow> rows = rowMapper.selectList(
                new LambdaQueryWrapper<MgmtLedgerRow>()
                        .eq(MgmtLedgerRow::getDomain, domain)
                        .orderByAsc(MgmtLedgerRow::getRowNo));

        MgmtLedgerListResult result = new MgmtLedgerListResult();
        result.setColumns(columns);
        result.setFilters(metaDto.getFilters());
        result.setPage(page);
        result.setSize(size);

        if (rows.isEmpty()) {
            result.setRows(new ArrayList<>());
            result.setRowIds(new ArrayList<>());
            result.setTotal(0);
            return result;
        }

        List<Long> rowIds = rows.stream().map(MgmtLedgerRow::getId).collect(Collectors.toList());
        List<MgmtLedgerCell> cells = cellMapper.selectList(
                new LambdaQueryWrapper<MgmtLedgerCell>()
                        .in(MgmtLedgerCell::getRowId, rowIds)
                        .orderByAsc(MgmtLedgerCell::getColIndex));
        Map<Long, List<MgmtLedgerCell>> cellMap = new LinkedHashMap<>();
        for (MgmtLedgerCell c : cells) {
            cellMap.computeIfAbsent(c.getRowId(), k -> new ArrayList<>()).add(c);
        }

        // RowView 绑定「行 id + 单元格」，保证 rowIds 与筛选/分页后的显示行严格对齐
        List<RowView> all = new ArrayList<>();
        for (MgmtLedgerRow row : rows) {
            List<MgmtLedgerCell> rowCells = cellMap.getOrDefault(row.getId(), new ArrayList<>());
            all.add(new RowView(row.getId(),
                    rowCells.stream().map(this::toCellDto).collect(Collectors.toList())));
        }

        List<RowView> filtered = all.stream()
                .filter(r -> matchKeyword(r.cells, keyword))
                .filter(r -> matchFilters(r.cells, columns, filters))
                .collect(Collectors.toList());

        int total = filtered.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(total, from + size);
        List<RowView> pageRows = from <= to ? filtered.subList(from, to) : new ArrayList<>();

        result.setRows(pageRows.stream().map(r -> r.cells).collect(Collectors.toList()));
        result.setRowIds(pageRows.stream().map(r -> r.id).collect(Collectors.toList()));
        result.setTotal(total);
        return result;
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

    private boolean matchKeyword(List<MgmtLedgerCellDto> row, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String k = keyword.toLowerCase();
        return row.stream().anyMatch(c -> c.getText() != null && c.getText().toLowerCase().contains(k));
    }

    private boolean matchFilters(List<MgmtLedgerCellDto> row, List<String> columns, Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, String> e : filters.entrySet()) {
            String val = e.getValue();
            if (!StringUtils.hasText(val) || "全部".equals(val) || "全部中队".equals(val)) {
                continue;
            }
            int idx = columns.indexOf(e.getKey());
            if (idx < 0 || idx >= row.size()) {
                continue;
            }
            String cellText = row.get(idx).getText();
            if (cellText == null || !cellText.equals(val)) {
                return false;
            }
        }
        return true;
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
