package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 救援车辆新增 / 编辑入参，字段名对齐 {@link RescueVehicleItem}。
 *
 * <p><b>新增语义</b>：{@code plate} 必填（车牌是台账唯一业务标识，service 校验）；
 * {@code sortNo} 取当前最大值 +1。<b>编辑语义</b>：局部更新，字段为 {@code null} 表示不修改。</p>
 *
 * <p>本请求只覆盖车辆本体字段——乘员 / 随车装备 / 耗材 / 出动汇总分属
 * fac_rescue_vehicle_crew 等子表，由详情端点聚合，台账维护暂不提供批量改写
 * （避免一次保存误删子表行）。</p>
 */
@Data
public class RescueVehicleWriteRequest {

    /** 车牌号 */
    private String plate;

    /** 车辆类型（如 泡沫消防车） */
    private String type;

    /** 所属中队 */
    private String squadron;

    /** 车长姓名 */
    private String leaderName;

    /** 车长电话 */
    private String leaderPhone;

    /** 车辆状态（如 待命 / 出动 / 维修） */
    private String status;

    /** 业务名称 */
    private String businessName;

    /** 车辆类型全称 */
    private String vehicleTypeFull;

    /** 停放位置 */
    private String parkingLocation;

    /** 底盘型号 */
    private String chassisModel;

    /** 出厂日期 */
    private String manufactureDate;

    /** 年检到期日 */
    private String inspectionExpiry;

    /** 泡沫罐容积 */
    private String foamTankVolume;

    /** 水罐容积 */
    private String waterTankVolume;

    /** 最大出水流量 */
    private String maxWaterFlow;

    /** 泡沫类型 */
    private String foamType;

    /** 上次保养日期 */
    private String lastMaintenanceDate;

    /** 下次保养日期 */
    private String nextMaintenanceDate;

    /** 总里程 */
    private String totalMileage;

    /** 故障记录 */
    private String faultRecord;

    /** 年检状态 */
    private String inspectionStatus;
}
