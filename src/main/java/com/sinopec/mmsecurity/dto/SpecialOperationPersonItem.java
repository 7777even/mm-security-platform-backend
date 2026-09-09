package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 特殊作业 - 作业人员（对应前端 SpecialOperationPerson）。 */
@Data
public class SpecialOperationPersonItem {
    private Long id;
    private String name;
    private String role;
    private String phone;
}
