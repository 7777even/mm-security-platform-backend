package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 台风资源调度单视图：落库后的完整行，含服务端生成的 {@code orderNo} 与 {@code prevStatus}。
 *
 * <p>契约 {@code typhoon-emergency.openapi.json#/components/schemas/TyphoonDispatchOrderView}。</p>
 */
@Data
public class TyphoonDispatchOrderView implements Serializable {

    private Long id;
    /** 调度单号（服务端按 TD-年月日-四位序号 生成） */
    private String orderNo;
    private String resourceCode;
    private String resourceName;
    private String dispatchAction;
    /** 调度前资源状态（首次调度为空） */
    private String prevStatus;
    private String currStatus;
    private String assignee;
    private Integer quantity;
    private String remark;
    private String operator;
    private LocalDateTime createdAt;
}
