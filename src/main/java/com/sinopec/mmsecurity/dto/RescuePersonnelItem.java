package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 应急救援资源 - 救援人员条目，字段与前端 rescuePersonnelMock.ts 一致。 */
@Data
public class RescuePersonnelItem {
    private Long id;
    private String name;
    private String squadron;
    private String role;
}
