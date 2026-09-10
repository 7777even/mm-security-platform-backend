package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 指令日志回传媒体（图片/视频/语音），契约源：前端 CommandActionMedia。 */
@Data
public class CommandActionMedia {

    private String id;
    private String type;
    private String name;
    private String src;
    private String duration;
}
