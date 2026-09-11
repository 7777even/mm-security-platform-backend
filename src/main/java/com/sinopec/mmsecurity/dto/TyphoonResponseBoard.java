package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 台风应急响应板聚合：预警横幅 + 预案指令 + 临时指令（取代前端硬编码，V41）。 */
@Data
public class TyphoonResponseBoard {
    private List<TyphoonAlertBanner> banners;

    private List<TyphoonCommand> planCommands;

    private List<TyphoonCommand> temporaryCommands;
}
