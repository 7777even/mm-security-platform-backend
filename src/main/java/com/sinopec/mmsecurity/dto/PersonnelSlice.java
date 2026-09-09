package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 人员构成切片（对应前端 PersonnelSlice），用于装置区二级页环形图。 */
@Data
public class PersonnelSlice implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private Integer value;
    private String color;
}
