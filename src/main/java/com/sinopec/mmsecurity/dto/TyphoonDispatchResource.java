package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 防汛排涝可调度资源，与 typhoon-emergency.openapi.json#/TyphoonDispatchResource 对齐。
 */
@Data
public class TyphoonDispatchResource implements Serializable {

    /** 资源 id */
    private String id;
    /** 资源类型 */
    private String type;
    /** 资源名称 */
    private String name;
    /** 资源编码 */
    private String code;
    /** 所属单位 */
    private String organization;
    /** 驻防区域 */
    private String area;
    /** 调度状态 */
    private String status;
    /** 距事件点 km */
    private Double distanceKm;
    /** 预计到达分钟 */
    private Integer etaMinutes;
    /** 能力描述 */
    private String capacity;
    /** 联系人 */
    private String contact;
    /** 联系电话 */
    private String phone;
    /** 经度 */
    private Double longitude;
    /** 纬度 */
    private Double latitude;
}
