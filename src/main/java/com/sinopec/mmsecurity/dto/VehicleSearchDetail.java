package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 车辆识别检索详情（检索结果字段 + 派单/货物扩展），契约源：前端 VehicleSearchDetail。 */
@Data
public class VehicleSearchDetail {

    private Long id;
    private String plate;
    private Integer confidence;
    private String gate;
    private String status;
    private String time;
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
}
