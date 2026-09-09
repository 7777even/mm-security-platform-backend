package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 特殊作业 - 现场视频项（对应前端 SpecialOperationVideo）。 */
@Data
public class SpecialOperationVideoItem {
    private Long id;
    private String name;
    private String location;
}
