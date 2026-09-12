package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 台风（防汛排涝）资源调度单写请求（A2 业务写侧）。
 *
 * <p>资源清单本体（{@code GET /typhoon/dispatch-resources}）保持只读，
 * 调度动作落独立的 V47 {@code fac_typhoon_dispatch_order} 表。</p>
 *
 * <p>契约 {@code typhoon-emergency.openapi.json#/components/schemas/TyphoonDispatchOrderWriteRequest}。</p>
 */
@Data
public class TyphoonDispatchOrderWriteRequest implements Serializable {

    /** 关联资源编码（fac_typhoon_dispatch_resource.resource_code），必填 */
    private String resourceCode;

    /** 资源名称 */
    private String resourceName;

    /** 调度动作：ASSIGN 指派 / CONFIRM 确认 / RELEASE 释放，必填 */
    private String dispatchAction;

    /** 被指派单位 / 责任人 */
    private String assignee;

    /** 调度数量 */
    private Integer quantity;

    /** 备注说明 */
    private String remark;
}
