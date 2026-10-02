package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PersonSearchWriteRequest {

    @NotBlank(message = "姓名不能为空")
    private String name;
    private String gate;
    private String status;
    private String date;
    private String gender;
    private String phone;
    private String company;
    private String idNumber;
    private String appointmentNo;
    private String appointmentTime;
    private String visitPurpose;
    private String specialOperation;
    private String operationArea;
}
