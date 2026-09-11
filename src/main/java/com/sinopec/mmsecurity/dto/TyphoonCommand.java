package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 台风响应板指令（前端 TyphoonLeftPanel 的 weatherCommands / temporaryCommands 项，V41）。 */
@Data
public class TyphoonCommand {
    /** 指令编码：w1..w5 / t1..t2 */
    private String id;

    /** 指令分组：预警与启动 / 现场加派 ... */
    private String group;

    private String name;

    /** 责任对象 */
    private String target;

    /** 已完成 / 执行中 / 待执行 */
    private String status;

    /** 指令时间（HH:mm 文案） */
    private String time;

    private String detail;
}
