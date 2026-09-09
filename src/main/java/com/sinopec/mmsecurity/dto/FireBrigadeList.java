package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 应急救援资源 - 消防队伍列表：区域筛选项 + 队伍条目。 */
@Data
public class FireBrigadeList {
    private List<String> areas;
    private List<FireBrigadeTeam> items;
}
