package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 特殊作业 - 作业票实体（对应 H2 表 fac_special_operation_ticket）。
 * 保留字规避：type→op_type、level→op_level、status→ticket_status、location→work_location。
 */
@Data
@TableName(value = "fac_special_operation_ticket")
public class FacSpecialOperationTicket implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ticketArea;

    private String opType;

    private String opLevel;

    private String ticketStatus;

    private String startTime;

    private String endTime;

    private String timeRange;

    private String workUnit;

    private String applyUnit;

    private String operationDate;

    private String workLocation;

    private String isContractor;

    private String hazardType;

    private String leaderName;

    private String leaderPhone;

    private String position;

    private Double longitude;

    private Double latitude;

    private String changeReason;

    private String cancelReason;

    private String guardianName;

    private String workers;

    private String permitNo;

    private String content;

    private Integer videoCount;

    private Integer gasMonitorCount;

    private Integer personnelCount;

    private Integer sortNo;
}
