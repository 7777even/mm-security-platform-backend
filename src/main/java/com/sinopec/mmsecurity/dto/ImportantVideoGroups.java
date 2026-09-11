package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 常驻视频监控分组聚合：高空AR 与 重点关注区域 两组。 */
@Data
public class ImportantVideoGroups {
    private List<ImportantVideoGroup> highArGroups;
    private List<ImportantVideoGroup> focusGroups;
}
