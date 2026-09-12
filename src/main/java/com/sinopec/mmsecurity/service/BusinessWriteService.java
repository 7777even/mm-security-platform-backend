package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.DutySignInView;
import com.sinopec.mmsecurity.dto.DutySignInWriteRequest;
import com.sinopec.mmsecurity.dto.EmergencyCommandRecordView;
import com.sinopec.mmsecurity.dto.EmergencyCommandRecordWriteRequest;
import com.sinopec.mmsecurity.dto.PatrolExecutionView;
import com.sinopec.mmsecurity.dto.PatrolExecutionWriteRequest;
import com.sinopec.mmsecurity.dto.TyphoonDispatchOrderView;
import com.sinopec.mmsecurity.dto.TyphoonDispatchOrderWriteRequest;
import com.sinopec.mmsecurity.entity.FacDutySignIn;
import com.sinopec.mmsecurity.entity.FacEmergencyCommandRecord;
import com.sinopec.mmsecurity.entity.FacPatrolExecution;
import com.sinopec.mmsecurity.entity.FacTyphoonDispatchOrder;
import com.sinopec.mmsecurity.mapper.FacDutySignInMapper;
import com.sinopec.mmsecurity.mapper.FacEmergencyCommandRecordMapper;
import com.sinopec.mmsecurity.mapper.FacPatrolExecutionMapper;
import com.sinopec.mmsecurity.mapper.FacTyphoonDispatchOrderMapper;
import com.sinopec.mmsecurity.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A2 业务写侧服务：应急指令 / 台风调度 / 巡更执行 / 值班签到 4 域的写与列表。
 *
 * <p><b>与零下行红线的边界（D4 拍板，务必勿越）</b>：本服务只做<b>业务留痕</b>——
 * 记录系统内部的指令下发与状态推进、资源调度单据、巡更执行结果、值班签到动作。
 * <b>绝不</b>触发任何物理设备；消防泵、广播强切、门禁断电、疏散喷淋等物理下行由
 * {@link com.sinopec.mmsecurity.security.HardControlPaths} 在前后端双重拦截，
 * 与本服务无关。新增能力时先确认不在红线清单内。</p>
 *
 * <p><b>写侧约束</b>：</p>
 * <ul>
 *   <li>操作人取 {@link UserContext}，缺失落 {@code unknown}（不因审计字段阻断业务）。</li>
 *   <li>状态推进类（指令 / 调度单）落 prev→curr 前后值，供事后追溯。</li>
 *   <li>每次写操作经 {@link SystemAuditHelper} 落 fac_audit_log（尽力而为，失败仅 warn）。</li>
 *   <li>列表走方言安全上限 {@code Page(1, N, false)}，避免全表扫。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessWriteService {

    /** 列表端点方言安全上限（H3 范式：Page(1, n, false) 走 PaginationInnerInterceptor）。 */
    private static final int MAX_LIST_SIZE = 200;

    private static final DateTimeFormatter SIGN_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Set<String> DISPATCH_ACTIONS = Set.of("ASSIGN", "CONFIRM", "RELEASE");
    private static final Set<String> PATROL_RESULTS = Set.of("NORMAL", "ABNORMAL");
    private static final Set<String> SIGN_ACTIONS = Set.of("SIGN_IN", "SIGN_OUT");

    private final FacEmergencyCommandRecordMapper commandRecordMapper;
    private final FacTyphoonDispatchOrderMapper dispatchOrderMapper;
    private final FacPatrolExecutionMapper patrolExecutionMapper;
    private final FacDutySignInMapper dutySignInMapper;
    private final SystemAuditHelper audit;

    /* ==================== 1) 应急指令下发 / 状态推进 ==================== */

    /**
     * 登记一条应急指令下发或状态推进记录（系统内部留痕，不触发物理设备）。
     * prevStatus 取该 commandCode 上一条记录的 currStatus，首次下发为空。
     */
    public EmergencyCommandRecordView createCommandRecord(EmergencyCommandRecordWriteRequest req) {
        if (req == null || !StringUtils.hasText(req.getCommandCode())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "指令编码 commandCode 不能为空");
        }
        if (!StringUtils.hasText(req.getCurrStatus())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "推进后状态 currStatus 不能为空");
        }
        String operator = currentOperator();
        String prevStatus = lastCommandStatus(req.getCommandCode().trim());

        FacEmergencyCommandRecord entity = new FacEmergencyCommandRecord();
        entity.setCommandCode(req.getCommandCode().trim());
        entity.setCommandName(req.getCommandName());
        entity.setCommandKind(req.getCommandKind());
        entity.setPrevStatus(prevStatus);
        entity.setCurrStatus(req.getCurrStatus().trim());
        entity.setDispatchMode(req.getDispatchMode());
        entity.setTarget(req.getTarget());
        entity.setRemark(req.getRemark());
        entity.setOperator(operator);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        commandRecordMapper.insert(entity);

        audit.record("emergency", "emergency.command.create", detail(
                "id", entity.getId(),
                "commandCode", entity.getCommandCode(),
                "prevStatus", String.valueOf(prevStatus),
                "currStatus", entity.getCurrStatus(),
                "operator", operator));
        return toCommandView(entity);
    }

    /** 应急指令记录列表（按 id 倒序，最多 200 条）。 */
    public List<EmergencyCommandRecordView> listCommandRecords() {
        IPage<FacEmergencyCommandRecord> page = commandRecordMapper.selectPage(
                new Page<>(1, MAX_LIST_SIZE, false),
                new LambdaQueryWrapper<FacEmergencyCommandRecord>()
                        .orderByDesc(FacEmergencyCommandRecord::getId));
        return page.getRecords().stream().map(BusinessWriteService::toCommandView).toList();
    }

    /* ==================== 2) 台风资源调度 ==================== */

    /** 登记一条资源调度单（指派 / 确认 / 释放），orderNo 由服务端生成。 */
    public TyphoonDispatchOrderView createDispatchOrder(TyphoonDispatchOrderWriteRequest req) {
        if (req == null || !StringUtils.hasText(req.getResourceCode())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "资源编码 resourceCode 不能为空");
        }
        String action = req.getDispatchAction() == null ? "" : req.getDispatchAction().trim().toUpperCase(Locale.ROOT);
        if (!DISPATCH_ACTIONS.contains(action)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "调度动作 dispatchAction 只能为 ASSIGN/CONFIRM/RELEASE");
        }
        String operator = currentOperator();
        String prevStatus = lastDispatchStatus(req.getResourceCode().trim());

        FacTyphoonDispatchOrder entity = new FacTyphoonDispatchOrder();
        entity.setOrderNo(nextOrderNo());
        entity.setResourceCode(req.getResourceCode().trim());
        entity.setResourceName(req.getResourceName());
        entity.setDispatchAction(action);
        entity.setPrevStatus(prevStatus);
        entity.setCurrStatus(dispatchTargetStatus(action));
        entity.setAssignee(req.getAssignee());
        entity.setQuantity(req.getQuantity());
        entity.setRemark(req.getRemark());
        entity.setOperator(operator);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        dispatchOrderMapper.insert(entity);

        audit.record("typhoon", "typhoon.dispatch.create", detail(
                "id", entity.getId(),
                "orderNo", entity.getOrderNo(),
                "resourceCode", entity.getResourceCode(),
                "dispatchAction", action,
                "prevStatus", String.valueOf(prevStatus),
                "currStatus", entity.getCurrStatus(),
                "operator", operator));
        return toDispatchView(entity);
    }

    /** 台风资源调度单列表（按 id 倒序，最多 200 条）。 */
    public List<TyphoonDispatchOrderView> listDispatchOrders() {
        IPage<FacTyphoonDispatchOrder> page = dispatchOrderMapper.selectPage(
                new Page<>(1, MAX_LIST_SIZE, false),
                new LambdaQueryWrapper<FacTyphoonDispatchOrder>()
                        .orderByDesc(FacTyphoonDispatchOrder::getId));
        return page.getRecords().stream().map(BusinessWriteService::toDispatchView).toList();
    }

    /* ==================== 3) 巡更执行上报 ==================== */

    /** 登记一条巡更执行记录（打卡与结果）。 */
    public PatrolExecutionView createPatrolExecution(PatrolExecutionWriteRequest req) {
        if (req == null || !StringUtils.hasText(req.getPatrolDate())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "巡查日期 patrolDate 不能为空");
        }
        if (!StringUtils.hasText(req.getDutyPerson())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "巡查责任人 dutyPerson 不能为空");
        }
        String result = req.getExecResult() == null ? "" : req.getExecResult().trim().toUpperCase(Locale.ROOT);
        if (!PATROL_RESULTS.contains(result)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "执行结果 execResult 只能为 NORMAL/ABNORMAL");
        }
        String operator = currentOperator();

        FacPatrolExecution entity = new FacPatrolExecution();
        entity.setPatrolDate(req.getPatrolDate().trim());
        entity.setShiftName(req.getShiftName());
        entity.setDutyPerson(req.getDutyPerson().trim());
        entity.setPatrolCount(req.getPatrolCount());
        entity.setLocation(req.getLocation());
        entity.setExecResult(result);
        entity.setFinding(req.getFinding());
        entity.setWorkOrderNo(req.getWorkOrderNo());
        entity.setOperator(operator);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        patrolExecutionMapper.insert(entity);

        audit.record("fire", "fire.patrol.create", detail(
                "id", entity.getId(),
                "patrolDate", entity.getPatrolDate(),
                "dutyPerson", entity.getDutyPerson(),
                "execResult", result,
                "operator", operator));
        return toPatrolView(entity);
    }

    /** 巡更执行记录列表（按 id 倒序，最多 200 条）。 */
    public List<PatrolExecutionView> listPatrolExecutions() {
        IPage<FacPatrolExecution> page = patrolExecutionMapper.selectPage(
                new Page<>(1, MAX_LIST_SIZE, false),
                new LambdaQueryWrapper<FacPatrolExecution>()
                        .orderByDesc(FacPatrolExecution::getId));
        return page.getRecords().stream().map(BusinessWriteService::toPatrolView).toList();
    }

    /* ==================== 4) 值班签到 ==================== */

    /** 登记一条值班签到 / 签退记录，signTime 为空时按当前时间填充。 */
    public DutySignInView createDutySignIn(DutySignInWriteRequest req) {
        if (req == null || !StringUtils.hasText(req.getDutyDate())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "值班日期 dutyDate 不能为空");
        }
        if (!StringUtils.hasText(req.getPersonName())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "值班人员 personName 不能为空");
        }
        String action = req.getSignAction() == null ? "" : req.getSignAction().trim().toUpperCase(Locale.ROOT);
        if (!SIGN_ACTIONS.contains(action)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "签到动作 signAction 只能为 SIGN_IN/SIGN_OUT");
        }
        String operator = currentOperator();

        FacDutySignIn entity = new FacDutySignIn();
        entity.setDutyDate(req.getDutyDate().trim());
        entity.setShiftName(req.getShiftName());
        entity.setDepartment(req.getDepartment());
        entity.setPersonName(req.getPersonName().trim());
        entity.setSignAction(action);
        // 契约未开放 signTime 入参，一律由服务端按当前时间填充（避免客户端伪造签到时间）
        entity.setSignTime(LocalDateTime.now().format(SIGN_TIME_FMT));
        entity.setRemark(req.getRemark());
        entity.setOperator(operator);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(0);
        dutySignInMapper.insert(entity);

        audit.record("emergency", "emergency.duty.sign", detail(
                "id", entity.getId(),
                "dutyDate", entity.getDutyDate(),
                "personName", entity.getPersonName(),
                "signAction", action,
                "operator", operator));
        return toDutyView(entity);
    }

    /** 值班签到记录列表（按 id 倒序，最多 200 条）。 */
    public List<DutySignInView> listDutySignIns() {
        IPage<FacDutySignIn> page = dutySignInMapper.selectPage(
                new Page<>(1, MAX_LIST_SIZE, false),
                new LambdaQueryWrapper<FacDutySignIn>()
                        .orderByDesc(FacDutySignIn::getId));
        return page.getRecords().stream().map(BusinessWriteService::toDutyView).toList();
    }

    /* ==================== 内部工具 ==================== */

    /**
     * 审计明细构造器：允许 null 值（{@code Map.of} 遇 null 直接 NPE，而落库前 id 可能尚未回填）。
     */
    private static Map<String, Object> detail(Object... kv) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return m;
    }

    private String currentOperator() {
        String u = UserContext.username();
        return (u == null || u.isBlank()) ? "unknown" : u;
    }

    /** 取某指令上一条记录的 currStatus（作为本次 prevStatus），无历史返回 null。 */
    private String lastCommandStatus(String commandCode) {
        IPage<FacEmergencyCommandRecord> page = commandRecordMapper.selectPage(
                new Page<>(1, 1, false),
                new LambdaQueryWrapper<FacEmergencyCommandRecord>()
                        .eq(FacEmergencyCommandRecord::getCommandCode, commandCode)
                        .orderByDesc(FacEmergencyCommandRecord::getId));
        List<FacEmergencyCommandRecord> rows = page.getRecords();
        return rows.isEmpty() ? null : rows.get(0).getCurrStatus();
    }

    /** 取某资源上一条调度单的 currStatus（作为本次 prevStatus），无历史返回 null。 */
    private String lastDispatchStatus(String resourceCode) {
        IPage<FacTyphoonDispatchOrder> page = dispatchOrderMapper.selectPage(
                new Page<>(1, 1, false),
                new LambdaQueryWrapper<FacTyphoonDispatchOrder>()
                        .eq(FacTyphoonDispatchOrder::getResourceCode, resourceCode)
                        .orderByDesc(FacTyphoonDispatchOrder::getId));
        List<FacTyphoonDispatchOrder> rows = page.getRecords();
        return rows.isEmpty() ? null : rows.get(0).getCurrStatus();
    }

    /** 调度单号：TD-年月日-四位序号（序号按当日已有单数 +1）。 */
    private String nextOrderNo() {
        long seq = dispatchOrderMapper.selectCount(new LambdaQueryWrapper<FacTyphoonDispatchOrder>()
                .ge(FacTyphoonDispatchOrder::getCreatedAt, LocalDateTime.now().toLocalDate().atStartOfDay())) + 1;
        return String.format("TD-%s-%04d", LocalDateTime.now().toLocalDate()
                .format(DateTimeFormatter.BASIC_ISO_DATE), seq);
    }

    /** 调度动作 → 推进后资源状态。 */
    private static String dispatchTargetStatus(String action) {
        return switch (action) {
            case "ASSIGN" -> "已指派";
            case "CONFIRM" -> "已出动";
            case "RELEASE" -> "已释放";
            default -> action;
        };
    }

    private static EmergencyCommandRecordView toCommandView(FacEmergencyCommandRecord e) {
        EmergencyCommandRecordView v = new EmergencyCommandRecordView();
        v.setId(e.getId());
        v.setCommandCode(e.getCommandCode());
        v.setCommandName(e.getCommandName());
        v.setCommandKind(e.getCommandKind());
        v.setPrevStatus(e.getPrevStatus());
        v.setCurrStatus(e.getCurrStatus());
        v.setDispatchMode(e.getDispatchMode());
        v.setTarget(e.getTarget());
        v.setRemark(e.getRemark());
        v.setOperator(e.getOperator());
        v.setCreatedAt(e.getCreatedAt());
        return v;
    }

    private static TyphoonDispatchOrderView toDispatchView(FacTyphoonDispatchOrder e) {
        TyphoonDispatchOrderView v = new TyphoonDispatchOrderView();
        v.setId(e.getId());
        v.setOrderNo(e.getOrderNo());
        v.setResourceCode(e.getResourceCode());
        v.setResourceName(e.getResourceName());
        v.setDispatchAction(e.getDispatchAction());
        v.setPrevStatus(e.getPrevStatus());
        v.setCurrStatus(e.getCurrStatus());
        v.setAssignee(e.getAssignee());
        v.setQuantity(e.getQuantity());
        v.setRemark(e.getRemark());
        v.setOperator(e.getOperator());
        v.setCreatedAt(e.getCreatedAt());
        return v;
    }

    private static PatrolExecutionView toPatrolView(FacPatrolExecution e) {
        PatrolExecutionView v = new PatrolExecutionView();
        v.setId(e.getId());
        v.setPatrolDate(e.getPatrolDate());
        v.setShiftName(e.getShiftName());
        v.setDutyPerson(e.getDutyPerson());
        v.setPatrolCount(e.getPatrolCount());
        v.setLocation(e.getLocation());
        v.setExecResult(e.getExecResult());
        v.setFinding(e.getFinding());
        v.setWorkOrderNo(e.getWorkOrderNo());
        v.setOperator(e.getOperator());
        v.setCreatedAt(e.getCreatedAt());
        return v;
    }

    private static DutySignInView toDutyView(FacDutySignIn e) {
        DutySignInView v = new DutySignInView();
        v.setId(e.getId());
        v.setDutyDate(e.getDutyDate());
        v.setShiftName(e.getShiftName());
        v.setDepartment(e.getDepartment());
        v.setPersonName(e.getPersonName());
        v.setSignAction(e.getSignAction());
        v.setSignTime(e.getSignTime());
        v.setRemark(e.getRemark());
        v.setOperator(e.getOperator());
        v.setCreatedAt(e.getCreatedAt());
        return v;
    }
}
