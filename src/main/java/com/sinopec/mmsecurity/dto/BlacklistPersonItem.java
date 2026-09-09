package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 安防黑名单 - 人员条目（字段与前端 blacklistMock.personBlacklist 对齐，idCard 为脱敏形态）。 */
@Data
public class BlacklistPersonItem {
    private Long id;
    private String name;
    private String idCard;
    private String reason;
    private String time;
    private String status;
}
