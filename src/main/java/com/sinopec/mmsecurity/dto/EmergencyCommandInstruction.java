package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 应急指挥单条指令（列表卡片），契约源：前端 EmergencyCommandInstruction。 */
@Data
public class EmergencyCommandInstruction {

    private String id;
    private String type;
    private String name;
    private String location;
    private String status;
    private String actionLabel;
    private Boolean done;
}
