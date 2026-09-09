package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 视频控制 - 顶部分类项（对应前端 VideoControlCategory）。 */
@Data
public class VideoCategoryItem {
    private String id;
    private String label;
    private Integer iconType;
}
