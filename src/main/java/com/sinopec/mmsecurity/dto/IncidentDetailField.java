package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 事件详情字段 */
@Data
public class IncidentDetailField implements Serializable {
    private static final long serialVersionUID = 1L;

    private String label;
    private String value;
}
