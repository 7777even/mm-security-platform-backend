package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 视频控制 - 摄像头画面项（对应前端 VideoControlCell，status: live/loading/ai）。 */
@Data
public class VideoCameraItem {
    private Long id;
    private String name;
    private String cameraType;
    private String location;
    private String status;
    private Boolean hd;
    private Integer thumbIndex;
}
