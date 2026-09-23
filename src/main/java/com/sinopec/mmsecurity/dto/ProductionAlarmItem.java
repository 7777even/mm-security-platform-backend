package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 生产报警项（对应前端 ProductionAlarmItem）。
 * 大屏报警面板与装置区二级页共用；装置区场景下 location / description 由服务端按设施名重写。
 */
@Data
public class ProductionAlarmItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String titleColor;
    private String location;
    private String time;
    private String description;
    private String status;
    /** 是否误报：是 / 否 / 未核实（可空，写回后回填）。 */
    private String falseAlarm;
    /** 处置情况文本（写回后回填）。 */
    private String handleResult;
    /** 处置时间（yyyy-MM-dd HH:mm:ss，写回后回填）。 */
    private String handleTime;
    /** 派单人员（逗号分隔，写回后回填）。 */
    private String dispatchPersonnel;
    /** 通知方式（APP/SMS，逗号分隔，写回后回填）。 */
    private String notifyMethod;
    private Integer iconIndex;
    private String thumb;
}
