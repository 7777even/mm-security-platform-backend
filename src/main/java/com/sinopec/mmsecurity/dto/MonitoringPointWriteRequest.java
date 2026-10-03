package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 监测点位写请求（新建/更新共用）。
 *
 * <p>实体主键 id 为 <b>String</b>（非自增 Long），故<b>不能</b>用 {@code LedgerIdSupport}
 * （其 getter 要求返回 Long）——id 由客户端随请求给定，重复时在写方法内抛 {@code ResultCode.CONFLICT}。
 */
@Data
public class MonitoringPointWriteRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 监测点位编码（字符串主键，由请求给定）。 */
    @NotBlank
    private String id;

    private String name;

    private String category;

    private String status;

    private String lastTime;

    private String org;

    private Double longitude;

    private Double latitude;
}
