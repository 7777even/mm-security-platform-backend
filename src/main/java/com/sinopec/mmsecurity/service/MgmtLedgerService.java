package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.MgmtLedgerCellDto;
import com.sinopec.mmsecurity.dto.MgmtLedgerFilterDto;
import com.sinopec.mmsecurity.dto.MgmtLedgerListResult;
import com.sinopec.mmsecurity.dto.MgmtLedgerMetaDto;
import com.sinopec.mmsecurity.entity.MgmtLedgerCell;
import com.sinopec.mmsecurity.entity.MgmtLedgerMeta;
import com.sinopec.mmsecurity.entity.MgmtLedgerRow;
import com.sinopec.mmsecurity.mapper.MgmtLedgerCellMapper;
import com.sinopec.mmsecurity.mapper.MgmtLedgerMetaMapper;
import com.sinopec.mmsecurity.mapper.MgmtLedgerRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理台账通用只读服务。数据来自 mgmt_ledger_* 三张表（V51 种子化自 mgmtMenus 静态数据）。
 * 仅 GET 查询，零下行控制；支持 keyword 模糊搜索与按列精确筛选。
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
        MgmtLedgerMeta meta = metaMapper.selectOne(
                new LambdaQueryWrapper<MgmtLedgerMeta>().eq(MgmtLedgerMeta::getDomain, domain));
        if (meta == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "台账不存在: " + domain);
        }
        MgmtLedgerMetaDto metaDto = toMetaDto(meta);
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

        List<List<MgmtLedgerCellDto>> allRows = new ArrayList<>();
        for (MgmtLedgerRow row : rows) {
            List<MgmtLedgerCell> rowCells = cellMap.getOrDefault(row.getId(), new ArrayList<>());
            allRows.add(rowCells.stream().map(this::toCellDto).collect(Collectors.toList()));
        }

        List<List<MgmtLedgerCellDto>> filtered = allRows.stream()
                .filter(r -> matchKeyword(r, keyword))
                .filter(r -> matchFilters(r, columns, filters))
                .collect(Collectors.toList());

        int total = filtered.size();
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(total, from + size);
        result.setRows(from <= to ? filtered.subList(from, to) : new ArrayList<>());
        result.setTotal(total);
        return result;
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
}
