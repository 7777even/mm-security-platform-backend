package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 应急救援资源 - 救援人员列表：中队/岗位筛选项 + 人员总数 + 条目。 */
@Data
public class RescuePersonnelList {
    private List<String> squadrons;
    private List<String> roles;
    private Integer totalCount;
    private List<RescuePersonnelItem> items;
}
