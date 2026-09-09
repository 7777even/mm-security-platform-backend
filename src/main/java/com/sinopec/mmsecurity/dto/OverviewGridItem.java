package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 总览网格项（设施或设备分类卡片，对应前端 OverviewGridItem）。
 * image 为前端 production 模块静态资源文件名（如 image_0001.png）。
 */
@Data
public class OverviewGridItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Integer count;
    private String image;
}
