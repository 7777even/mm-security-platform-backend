package com.sinopec.mmsecurity.dto;

import java.util.List;
import lombok.Data;

/** 指令日志条目，契约源：前端 CommandActionDynamicEntry。 */
@Data
public class CommandActionDynamicEntry {

    private String id;
    private String time;
    private String type;
    private String operator;
    private String result;
    private String content;
    private String attachment;
    private List<CommandActionMedia> media;
}
