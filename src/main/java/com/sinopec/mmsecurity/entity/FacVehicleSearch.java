package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

/**
 * 车辆识别检索实体。数据来自真实表 fac_vehicle_search；keyword 检索在服务端按车牌/卡口/状态过滤。
 */
@Data
@TableName("fac_vehicle_search")
public class FacVehicleSearch {
    private Long id;
    private String plate;
    private Integer confidence;
    private String gate;
    private String status;
    private String time;

    // V28 新增：检索详情扩展（原前端 securitySearchMock 的 vehicleDetailExtras）
    private String vehicleType;
    private String driverName;
    private String driverPhone;
    private String company;
    private String appointmentNo;
    private String appointmentTime;
    private String visitPurpose;
    private String waybillNo;
    private String cargo;
    private String destination;

    @Version
    private Long version;
}
