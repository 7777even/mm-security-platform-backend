package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 应急调度资源（与台风防汛力量清单同构，按域独立建表） */
@Data
public class EmergencyDispatchResource implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String type;
    private String name;
    private String code;
    private String organization;
    private String area;
    private String status;
    private Double distanceKm;
    private Integer etaMinutes;
    private String capacity;
    private String contact;
    private String phone;
    private Double longitude;
    private Double latitude;
}
