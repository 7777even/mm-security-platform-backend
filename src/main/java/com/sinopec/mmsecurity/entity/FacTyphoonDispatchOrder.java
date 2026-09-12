package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 台风（防汛排涝）资源调度单：指派 / 确认 / 释放（A2 业务写侧，V47）。
 *
 * <p>与只读清单表 fac_typhoon_dispatch_resource 分离：资源清单保持只读，
 * 调度动作落本表，避免写操作污染读模型（D2 决策）。</p>
 */
@Data
@TableName("fac_typhoon_dispatch_order")
public class FacTyphoonDispatchOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 调度单号 */
    private String orderNo;
    /** 关联资源编码（fac_typhoon_dispatch_resource.resource_code） */
    private String resourceCode;
    private String resourceName;
    /** 调度动作：ASSIGN / CONFIRM / RELEASE */
    private String dispatchAction;
    private String prevStatus;
    private String currStatus;
    /** 被指派单位 / 责任人 */
    private String assignee;
    /** 调度数量 */
    private Integer quantity;
    private String remark;
    private String operator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
