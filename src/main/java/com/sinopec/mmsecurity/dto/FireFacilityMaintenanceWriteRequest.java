package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 消防设施台账维保记录新增请求：挂在 ledger_id 下，记录一次维护保养。
 * <p>字段名与前端契约 {@code FireFacilityMaintenanceWriteRequest} schema 同名，守门脚本按同名类逐字段对拍。
 * <ul>
 *   <li>date：维保日期（yyyy-MM-dd，必填）</li>
 *   <li>content：维保内容（必填）</li>
 *   <li>reportFile：维保报告附件名（可空）</li>
 * </ul>
 */
@Data
public class FireFacilityMaintenanceWriteRequest {
    /** 维保日期（yyyy-MM-dd）。 */
    private String date;
    /** 维保内容。 */
    private String content;
    /** 维保报告附件名（可空）。 */
    private String reportFile;
}
