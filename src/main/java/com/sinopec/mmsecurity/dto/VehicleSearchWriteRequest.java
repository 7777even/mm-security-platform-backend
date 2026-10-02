package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VehicleSearchWriteRequest {

    @NotBlank(message = "车牌号不能为空")
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
