package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 防汛排涝可调度资源清单。 */
@Data
@TableName("fac_typhoon_dispatch_resource")
public class FacTyphoonDispatchResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 资源 id */
    private String resourceCode;
    /** 资源类型 */
    private String resourceType;
    /** 资源名称 */
    private String resourceName;
    /** 资源编码 */
    private String code;
    /** 所属单位 */
    private String organization;
    /** 驻防区域 */
    private String area;
    /** 调度状态 */
    private String statusName;
    /** 距离 km */
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
    /** 排序号 */
    private Integer sortNo;}
