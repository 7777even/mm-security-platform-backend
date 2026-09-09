package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 视频控制 - 摄像头分页（前端默认每页 9 宫格，pages 由 total/size 向上取整）。 */
@Data
public class VideoCameraPage {
    private Long total;
    private Integer page;
    private Integer size;
    private Integer pages;
    private List<VideoCameraItem> list;
}
