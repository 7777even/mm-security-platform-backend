package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建/更新应急事件入参（不含 alarmId/ts 等系统字段），与前端 {@code alarm.openapi.json#/EmergencyEventPayload} 对齐。
 *
 * <p>校验：level/type/deviceCode/location/description 为必填；deviceCode 为 20 位 MDM 物理主键，
 * 由 {@code AlarmService} 二次校验长度（非 20 位抛 {@code DEVICE_CODE_INVALID}）。
 * status 可空，缺省后端置 ACTIVE。</p>
 */
@Data
public class EmergencyEventPayload {

    /** 报警等级 1-4（1 最高） */
    @NotNull(message = "level 必填")
    private Integer level;

    /** 报警类型 FIRE/GAS/TEMP/CCTV/SOS */
    @NotBlank(message = "type 必填")
    private String type;

    /** 状态枚举 ACTIVE/ACKED/DISPATCHED/CLOSED；可空，缺省 ACTIVE */
    private String status;

    /** 关联 20 位 MDM 设备编码 */
    @NotBlank(message = "deviceCode 必填")
    private String deviceCode;

    /** 事发位置 */
    @NotBlank(message = "location 必填")
    private String location;

    /** 事件描述 */
    @NotBlank(message = "description 必填")
    private String description;
}
