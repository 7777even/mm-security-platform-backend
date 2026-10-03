package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 特殊作业票写请求（新建/更新共用）。
 *
 * <p>id 与 sort_no 由服务端按 {@code LedgerIdSupport} 分配（规避三方言自增序列滞后撞主键）。
 * 列名沿用实体的保留字规避约定：op_type / op_level / ticket_status / work_location。
 * 只写主票表 {@code fac_special_operation_ticket}；现场视频 / 气体检测点 / 作业人员的子表本批不开放写。
 */
@Data
public class SpecialOperationWriteRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String opType;

    private String ticketArea;

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
}
