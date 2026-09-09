package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 消防设施监测 - 维护保养记录（date/content/reportFile）。 */
@Data
public class FireFacilityMaintenanceRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String date;
    private String content;
    private String reportFile;
}
