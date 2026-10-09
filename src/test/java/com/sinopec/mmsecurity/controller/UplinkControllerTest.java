package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.dto.AuditLogItem;
import com.sinopec.mmsecurity.service.UplinkService;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UplinkController（纯 Mockito）：CSV 导出（③b）直接写 HttpServletResponse，
 * 校验 UTF-8 BOM、中文表头、单元格转义（逗号/引号/换行）及查询参数透传至 service。
 */
class UplinkControllerTest {

    private final UplinkService service = mock(UplinkService.class);
    private final UplinkController controller = new UplinkController(service);

    @Test
    void exportAudit_writesUtf8BomCsvWithEscaping() throws Exception {
        AuditLogItem row = new AuditLogItem();
        row.setId(1L);
        row.setAction("login,logout");
        row.setModule("ADMIN");
        row.setActor("ad\"min");
        row.setEventAt(1717488000000L);
        row.setCreatedAt("2026-09-14 10:20:00");
        row.setDetailJson("line1\nline2");
        when(service.exportAudit(anyInt(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(row));

        StringWriter sw = new StringWriter();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        controller.exportAudit(null, null, null, null, null, resp);

        String csv = sw.toString();
        assertTrue(csv.startsWith("﻿"), "CSV 须以 UTF-8 BOM 开头（Excel 中文不乱码）");
        assertTrue(csv.contains("ID,操作动作,模块,操作人,事件时间,落库时间,详情"), "CSV 须含中文表头");
        assertTrue(csv.contains("\"login,logout\""), "含逗号的单元格须被双引号包裹");
        assertTrue(csv.contains("\"ad\"\"min\""), "含引号的单元格须被双引号包裹且内部引号翻倍");
        assertTrue(csv.contains("\"line1\nline2\""), "含换行的单元格须被双引号包裹");
    }

    @Test
    void exportAudit_plumbsQueryParamsToService() throws Exception {
        when(service.exportAudit(anyInt(), any(), any(), any(), any(), any())).thenReturn(List.of());
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        long start = 1_700_000_000_000L;
        long end = 1_800_000_000_000L;
        controller.exportAudit("ADMIN", "login", "admin", start, end, resp);

        verify(service).exportAudit(anyInt(), eq("ADMIN"), eq("login"), eq("admin"), eq(start), eq(end));
    }

    @Test
    void queryAudit_plumbsQueryParamsToService() {
        when(service.queryAudit(anyInt(), anyInt(), any(), any(), any(), any(), any()))
                .thenReturn(new com.sinopec.mmsecurity.dto.AuditLogPageResult());

        controller.queryAudit(1L, 20L, "ADMIN", "login", "admin", 1_700_000_000_000L, 1_800_000_000_000L);

        verify(service).queryAudit(
                eq(1L), eq(20L), eq("ADMIN"), eq("login"), eq("admin"),
                eq(1_700_000_000_000L), eq(1_800_000_000_000L));
    }
}
