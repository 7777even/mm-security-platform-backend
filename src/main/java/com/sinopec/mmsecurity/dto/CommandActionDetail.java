package com.sinopec.mmsecurity.dto;

import java.util.List;
import lombok.Data;

/** 应急指挥指令行动详情，契约源：前端 CommandActionDetail。 */
@Data
public class CommandActionDetail {

    private String id;
    private String name;
    private String type;
    private List<String> notifyChannels;
    private String status;
    private String dispatchMode;
    private String location;
    private String description;
    private String attachment;
    private List<CommandActionRecipient> addressBookRecipients;
    private List<CommandActionRecipient> dutyRecipients;
    private List<CommandActionDynamicEntry> dynamics;
}
