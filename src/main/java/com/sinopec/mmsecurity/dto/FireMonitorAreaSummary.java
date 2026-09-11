package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 消防态势大屏 - 装置区保障汇总集合。 */
@Data
public class FireMonitorAreaSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<FireMonitorArea> items;
}
