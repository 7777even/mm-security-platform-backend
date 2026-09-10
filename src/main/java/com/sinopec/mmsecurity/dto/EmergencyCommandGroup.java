package com.sinopec.mmsecurity.dto;

import java.util.List;
import lombok.Data;

/** 应急指挥指令分组（固定/临时），契约源：前端 EmergencyCommandGroup。 */
@Data
public class EmergencyCommandGroup {

    private String id;
    private String label;
    private List<EmergencyCommandInstruction> items;
}
