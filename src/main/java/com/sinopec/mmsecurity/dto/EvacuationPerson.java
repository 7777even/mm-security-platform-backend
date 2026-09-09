package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 疏散人员名册条目（与前端 EvacuationPerson 契约一致，经纬度由前端按 routeProgress 自行插值）。 */
@Data
public class EvacuationPerson {

    private Long id;

    private String name;

    private String org;

    private String job;

    /** 疏散路线上的归一化进度，取值 0..1。 */
    private Double routeProgress;
}
