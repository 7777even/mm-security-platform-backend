package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 人员识别检索详情（检索结果字段 + 访客/作业扩展），契约源：前端 PersonSearchDetail。 */
@Data
public class PersonSearchDetail {

    private Long id;
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
