package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 值班人员 */
@Data
public class RescueDutyPerson implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String role;
    private String phone;
    private Integer avatarIndex;
}
