package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 消防态势大屏 - 重点监控对象集合。 */
@Data
public class FireMonitoredObjectSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<FireMonitoredObject> items;
}
