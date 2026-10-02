package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 消防队伍（救援队伍）新增 / 编辑入参，字段名对齐 {@link FireBrigadeTeam}。
 *
 * <p><b>新增语义</b>：{@code name} 必填（service 校验）；{@code sortNo} 取当前最大值 +1。
 * <b>编辑语义</b>：局部更新，字段为 {@code null} 表示不修改。</p>
 *
 * <p>经纬度为可选：队伍台账允许先建后补坐标；缺省时大屏地图不落点（前端按 null 跳过飞入，
 * 避免 Cesium 因 NaN 抛 DeveloperError）。</p>
 */
@Data
public class RescueBrigadeWriteRequest {

    /** 队伍名称 */
    private String name;

    /** 所属区域 */
    private String area;

    /** 队员人数 */
    private Integer memberCount;

    /** 负责人姓名 */
    private String leaderName;

    /** 负责人电话 */
    private String leaderPhone;

    /** 驻扎位置 */
    private String location;

    /** 经度 */
    private Double longitude;

    /** 纬度 */
    private Double latitude;

    /** 队伍简介 */
    private String description;

    /** 可投入救援人数 */
    private Integer rescuePersonnel;

    /** 可投入救援车辆数 */
    private Integer rescueVehicles;
}
