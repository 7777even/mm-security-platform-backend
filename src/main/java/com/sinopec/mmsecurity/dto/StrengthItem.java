package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应急力量明细预览项，随 {@link EmergencyResource#getItems()} 返回。
 * 用于大屏「应急力量救援」面板点击资源类别时就地展示的真实明细（取各台账前若干条）。
 */
@Data
public class StrengthItem implements Serializable {

    /** 明细名称（专家姓名 / 装备名 / 车牌 / 队伍名） */
    private String name;

    /** 辅助说明（岗位 / 规格 / 车型 / 区域），无则为空 */
    private String meta;
}
