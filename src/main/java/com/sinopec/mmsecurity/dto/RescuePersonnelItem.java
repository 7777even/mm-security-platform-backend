package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 应急救援资源 - 救援人员条目，字段与前端 rescuePersonnelMock.ts 一致。 */
@Data
public class RescuePersonnelItem {
    private Long id;
    private String name;
    private String squadron;
    private String role;

    /** 所属分组（专家分类）；与 RescuePersonnelWriteRequest.personGroup 同源。 */
    private String personGroup;

    /** 联系电话；应急专家台账要能直接联系到人，故随列表一并返回。 */
    private String phone;

    /** 值班状态（在岗 / 备勤 / 休整）。 */
    private String dutyStatus;
}
