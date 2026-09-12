package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 巡更执行记录：打卡与结果上报（A2 业务写侧，V47）。
 *
 * <p>与既有 fac_fire_patrol（巡查计划/班次记录，仍只读）分离：
 * 本表记录每次实际执行与结果，属写侧留痕。</p>
 */
@Data
@TableName("fac_patrol_execution")
public class FacPatrolExecution {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 巡查日期 YYYY-MM-DD */
    private String patrolDate;
    /** 班次：上午 / 下午 / 夜间 */
    private String shiftName;
    private String dutyPerson;
    /** 当班第几次巡查，如「第1次」 */
    private String patrolCount;
    /** 本次巡查部位 */
    private String location;
    /** 执行结果：NORMAL / ABNORMAL */
    private String execResult;
    /** 异常描述（正常时为空） */
    private String finding;
    /** 派单后关联的工单号 */
    private String workOrderNo;
    private String operator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
