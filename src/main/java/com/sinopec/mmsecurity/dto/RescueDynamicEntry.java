package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 动态快讯条目（含 rescue/command/brief/awareness 四类） */
@Data
public class RescueDynamicEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String category;
    private String title;
    private String tag;
    private String time;
    private String command;
    private String responder;
    private String reply;
    private String stageLabel;
}
