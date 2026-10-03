package com.sinopec.mmsecurity.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 防火巡查记录写请求（管理端台账新增 / 编辑，fire:patrol-write）。
 *
 * <p>字段白名单：仅包含管理端可录入的台账字段，<b>不含任何设备实时状态 / 检查项结果</b>
 * （检查项结果由 fac_fire_patrol_item_result 单独维护，本轮只读）。locations 为部位列表，
 * 落库时以逗号拼接存于 fac_fire_patrol.locations（与 GET /patrols 返回结构对齐）。
 */
@Data
public class FirePatrolWriteRequest {

    /** 巡查日期，格式 YYYY-MM-DD（必填）。 */
    @NotBlank(message = "巡查日期不能为空")
    private String patrolDate;

    /** 班次（上午 / 下午 / 夜间）。 */
    private String shift;

    /** 巡查责任人姓名。 */
    private String dutyPerson;

    /** 当班第几次巡查，如「第1次」。 */
    private String patrolCount;

    /** 巡查覆盖部位列表（前端逗号 / 顿号录入，落库逗号拼接）。 */
    private List<String> locations;

    /** 是否已完成巡查。 */
    private Boolean completed;

    /** 关联工单号，异常派单后有值。 */
    private String workOrderNo;
}
