package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 应急力量统计，与前端 {@code emergency.openapi.json#/EmergencyStrength} 对齐。
 */
@Data
public class EmergencyStrength implements Serializable {

    /** 按维度统计的应急资源 */
    private List<EmergencyResource> resources;
}
