package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinopec.mmsecurity.common.BusinessException;
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
import com.sinopec.mmsecurity.security.LoginUser;
import com.sinopec.mmsecurity.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A2 业务写侧服务校验（纯 Mockito，不起 Spring 上下文、不连 DB）。
 *
 * <p>重点锁定三件事：① 状态推进类落 prev→curr 前后值；② 操作人与时间戳由服务端填充；
 * ③ 参数校验与审计旁路都命中。物理触发不在本层（红线由 HardControlPaths 拦截）。</p>
 */
class BusinessWriteServiceTest {

    private final FacEmergencyCommandRecordMapper commandRecordMapper =
            Mockito.mock(FacEmergencyCommandRecordMapper.class);
    private final FacTyphoonDispatchOrderMapper dispatchOrderMapper =
            Mockito.mock(FacTyphoonDispatchOrderMapper.class);
    private final FacPatrolExecutionMapper patrolExecutionMapper =
            Mockito.mock(FacPatrolExecutionMapper.class);
    private final FacDutySignInMapper dutySignInMapper = Mockito.mock(FacDutySignInMapper.class);
    private final SystemAuditHelper audit = Mockito.mock(SystemAuditHelper.class);

    private final BusinessWriteService service = new BusinessWriteService(
            commandRecordMapper, dispatchOrderMapper, patrolExecutionMapper, dutySignInMapper, audit);

    @BeforeEach
    void setUp() {
        UserContext.set(new LoginUser(1L, "admin", "ADMIN"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    /* ==================== 应急指令 ==================== */

    @Test
    void createCommandRecord_fillsPrevStatusFromLastRecord() {
        // 上一条记录 currStatus=待执行 → 本次 prevStatus 应为「待执行」
        stubCommandPage( List.of(record(9L, "w1", "待执行")));
        stubInsert(commandRecordMapper);

        EmergencyCommandRecordWriteRequest req = new EmergencyCommandRecordWriteRequest();
        req.setCommandCode("w1");
        req.setCommandName("发布防台防汛预警");
        req.setCurrStatus("执行中");

        EmergencyCommandRecordView v = service.createCommandRecord(req);
        assertEquals("待执行", v.getPrevStatus());
        assertEquals("执行中", v.getCurrStatus());
        assertEquals("admin", v.getOperator());
        assertNotNull(v.getCreatedAt());

        Mockito.verify(audit).record(ArgumentMatchers.eq("emergency"),
                ArgumentMatchers.eq("emergency.command.create"), ArgumentMatchers.any());
    }

    @Test
    void createCommandRecord_firstDispatchHasNullPrevStatus() {
        stubCommandPage( List.of());
        stubInsert(commandRecordMapper);

        EmergencyCommandRecordWriteRequest req = new EmergencyCommandRecordWriteRequest();
        req.setCommandCode("w1");
        req.setCurrStatus("执行中");

        assertNull(service.createCommandRecord(req).getPrevStatus());
    }

    /**
     * V47 里 {@code command_name} 是 NOT NULL，而契约中它是可选字段。
     * 「仅推进状态」的调用不传 commandName，服务端必须继承上一条记录的指令名，
     * 否则直接撞数据库非空约束（线上表现为 500）。
     */
    @Test
    void createCommandRecord_inheritsCommandNameWhenOmitted() {
        FacEmergencyCommandRecord prev = new FacEmergencyCommandRecord();
        prev.setId(9L);
        prev.setCommandCode("w1");
        prev.setCommandName("发布防台防汛预警");
        prev.setCurrStatus("待执行");
        stubCommandPage(List.of(prev));
        stubInsert(commandRecordMapper);

        EmergencyCommandRecordWriteRequest req = new EmergencyCommandRecordWriteRequest();
        req.setCommandCode("w1");
        req.setCurrStatus("执行中"); // 刻意不传 commandName

        EmergencyCommandRecordView v = service.createCommandRecord(req);
        assertEquals("发布防台防汛预警", v.getCommandName());
        assertEquals("待执行", v.getPrevStatus());
    }

    /** 无历史记录且未传 commandName 时落空串，保证 command_name 非空约束成立。 */
    @Test
    void createCommandRecord_fallsBackToEmptyNameWithoutHistory() {
        stubCommandPage(List.of());
        stubInsert(commandRecordMapper);

        EmergencyCommandRecordWriteRequest req = new EmergencyCommandRecordWriteRequest();
        req.setCommandCode("w1");
        req.setCurrStatus("执行中");
        assertEquals("", service.createCommandRecord(req).getCommandName());
    }

    @Test
    void createCommandRecord_rejectsBlankCommandCodeOrStatus() {
        EmergencyCommandRecordWriteRequest noCode = new EmergencyCommandRecordWriteRequest();
        noCode.setCurrStatus("执行中");
        assertThrows(BusinessException.class, () -> service.createCommandRecord(noCode));
        assertThrows(BusinessException.class, () -> service.createCommandRecord(null));

        EmergencyCommandRecordWriteRequest noStatus = new EmergencyCommandRecordWriteRequest();
        noStatus.setCommandCode("w1");
        assertThrows(BusinessException.class, () -> service.createCommandRecord(noStatus));
    }

    @Test
    void listCommandRecords_mapsViews() {
        stubCommandPage( List.of(record(2L, "w1", "已完成"), record(1L, "w1", "执行中")));
        List<EmergencyCommandRecordView> list = service.listCommandRecords();
        assertEquals(2, list.size());
        assertEquals(2L, list.get(0).getId());
        assertEquals("已完成", list.get(0).getCurrStatus());
    }

    /* ==================== 台风调度 ==================== */

    @Test
    void createDispatchOrder_generatesOrderNoAndMapsTargetStatus() {
        stubDispatchPage( List.of());
        Mockito.doAnswer(inv -> {
            FacTyphoonDispatchOrder e = inv.getArgument(0);
            e.setId(7L);
            return 1;
        }).when(dispatchOrderMapper).insert(ArgumentMatchers.any(FacTyphoonDispatchOrder.class));
        Mockito.when(dispatchOrderMapper.selectCount(ArgumentMatchers.any(Wrapper.class))).thenReturn(0L);

        TyphoonDispatchOrderWriteRequest req = new TyphoonDispatchOrderWriteRequest();
        req.setResourceCode("TEAM-FX-01");
        req.setResourceName("炼油防汛抢险一组");
        req.setDispatchAction("assign"); // 大小写不敏感
        req.setAssignee("高策");
        req.setQuantity(1);

        TyphoonDispatchOrderView v = service.createDispatchOrder(req);
        assertEquals("ASSIGN", v.getDispatchAction());
        assertEquals("已指派", v.getCurrStatus());
        assertNull(v.getPrevStatus());
        assertTrue(v.getOrderNo().matches("TD-\\d{8}-\\d{4}"), "单号格式应为 TD-年月日-四位序号，实际：" + v.getOrderNo());
        assertEquals("admin", v.getOperator());

        Mockito.verify(audit).record(ArgumentMatchers.eq("typhoon"),
                ArgumentMatchers.eq("typhoon.dispatch.create"), ArgumentMatchers.any());
    }

    @Test
    void createDispatchOrder_rejectsBadActionOrBlankResource() {
        TyphoonDispatchOrderWriteRequest badAction = new TyphoonDispatchOrderWriteRequest();
        badAction.setResourceCode("TEAM-FX-01");
        badAction.setDispatchAction("DELETE");
        assertThrows(BusinessException.class, () -> service.createDispatchOrder(badAction));

        TyphoonDispatchOrderWriteRequest noResource = new TyphoonDispatchOrderWriteRequest();
        noResource.setDispatchAction("ASSIGN");
        assertThrows(BusinessException.class, () -> service.createDispatchOrder(noResource));
    }

    /* ==================== 巡更执行 ==================== */

    @Test
    void createPatrolExecution_normalizesResult() {
        stubInsert(patrolExecutionMapper);

        PatrolExecutionWriteRequest req = new PatrolExecutionWriteRequest();
        req.setPatrolDate("2026-09-13");
        req.setDutyPerson("李强");
        req.setExecResult("abnormal");
        req.setFinding("罐区A 消防栓被遮挡");

        PatrolExecutionView v = service.createPatrolExecution(req);
        assertEquals("ABNORMAL", v.getExecResult());
        assertEquals("admin", v.getOperator());
        Mockito.verify(audit).record(ArgumentMatchers.eq("fire"),
                ArgumentMatchers.eq("fire.patrol.create"), ArgumentMatchers.any());
    }

    @Test
    void createPatrolExecution_rejectsBadResultOrBlankPerson() {
        PatrolExecutionWriteRequest badResult = new PatrolExecutionWriteRequest();
        badResult.setPatrolDate("2026-09-13");
        badResult.setDutyPerson("李强");
        badResult.setExecResult("UNKNOWN");
        assertThrows(BusinessException.class, () -> service.createPatrolExecution(badResult));

        PatrolExecutionWriteRequest noPerson = new PatrolExecutionWriteRequest();
        noPerson.setPatrolDate("2026-09-13");
        noPerson.setExecResult("NORMAL");
        assertThrows(BusinessException.class, () -> service.createPatrolExecution(noPerson));
    }

    /* ==================== 值班签到 ==================== */

    @Test
    void createDutySignIn_fillsSignTimeByServer() {
        stubInsert(dutySignInMapper);

        DutySignInWriteRequest req = new DutySignInWriteRequest();
        req.setDutyDate("2026-09-13");
        req.setShiftName("白班");
        req.setPersonName("杨恒朋");
        req.setSignAction("sign_in");

        DutySignInView v = service.createDutySignIn(req);
        assertEquals("SIGN_IN", v.getSignAction());
        assertNotNull(v.getSignTime());
        assertTrue(v.getSignTime().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                "签到时间应由服务端填充，实际：" + v.getSignTime());
        assertEquals("admin", v.getOperator());
        Mockito.verify(audit).record(ArgumentMatchers.eq("emergency"),
                ArgumentMatchers.eq("emergency.duty.sign"), ArgumentMatchers.any());
    }

    @Test
    void createDutySignIn_rejectsBadAction() {
        DutySignInWriteRequest req = new DutySignInWriteRequest();
        req.setDutyDate("2026-09-13");
        req.setPersonName("杨恒朋");
        req.setSignAction("CHECK");
        assertThrows(BusinessException.class, () -> service.createDutySignIn(req));
    }

    /** 无登录态（如定时任务 / 集成调用）时操作人落 unknown，不因审计字段阻断业务。 */
    @Test
    void operatorFallsBackToUnknownWithoutUserContext() {
        UserContext.clear();
        stubCommandPage( List.of());
        stubInsert(commandRecordMapper);

        EmergencyCommandRecordWriteRequest req = new EmergencyCommandRecordWriteRequest();
        req.setCommandCode("w1");
        req.setCurrStatus("执行中");
        assertEquals("unknown", service.createCommandRecord(req).getOperator());
    }

    /* ==================== update / delete 用例（任务：BusinessWriteService update/delete 单测） ==================== */

    /* ---- 应急指令 update / delete ---- */
    @Test
    void updateCommandRecord_overwritesOnlyNonBlankFieldsAndAudits() {
        FacEmergencyCommandRecord existing = new FacEmergencyCommandRecord();
        existing.setId(9L);
        existing.setCommandCode("w1");
        existing.setCommandName("旧指令名");
        existing.setCurrStatus("待执行");
        Mockito.when(commandRecordMapper.selectById(9L)).thenReturn(existing);
        Mockito.when(commandRecordMapper.updateById(ArgumentMatchers.any(FacEmergencyCommandRecord.class))).thenReturn(1);

        EmergencyCommandRecordWriteRequest req = new EmergencyCommandRecordWriteRequest();
        req.setCurrStatus("执行中"); // 只改状态，不传指令名
        EmergencyCommandRecordView v = service.updateCommandRecord(9L, req);
        assertEquals("执行中", v.getCurrStatus());
        assertEquals("旧指令名", v.getCommandName(), "未传入的字段不应被覆盖");
        assertEquals("w1", v.getCommandCode());
        Mockito.verify(commandRecordMapper).updateById(ArgumentMatchers.any(FacEmergencyCommandRecord.class));
        Mockito.verify(audit).record(ArgumentMatchers.eq("emergency"),
                ArgumentMatchers.eq("emergency.command.update"), ArgumentMatchers.any());
    }

    @Test
    void updateCommandRecord_notFound_throwsNotFound() {
        Mockito.when(commandRecordMapper.selectById(99L)).thenReturn(null);
        EmergencyCommandRecordWriteRequest req = new EmergencyCommandRecordWriteRequest();
        req.setCurrStatus("执行中");
        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateCommandRecord(99L, req));
        assertTrue(ex.getMessage().contains("不存在"));
    }

    @Test
    void updateCommandRecord_nullId_throwsParamInvalid() {
        EmergencyCommandRecordWriteRequest req = new EmergencyCommandRecordWriteRequest();
        req.setCurrStatus("执行中");
        assertThrows(BusinessException.class, () -> service.updateCommandRecord(null, req));
    }

    @Test
    void deleteCommandRecord_callsDeleteAndAudits() {
        FacEmergencyCommandRecord existing = new FacEmergencyCommandRecord();
        existing.setId(9L);
        Mockito.when(commandRecordMapper.selectById(9L)).thenReturn(existing);
        Mockito.when(commandRecordMapper.deleteById(9L)).thenReturn(1);
        service.deleteCommandRecord(9L);
        Mockito.verify(commandRecordMapper).deleteById(9L);
        Mockito.verify(audit).record(ArgumentMatchers.eq("emergency"),
                ArgumentMatchers.eq("emergency.command.delete"), ArgumentMatchers.any());
    }

    @Test
    void deleteCommandRecord_notFound_throwsNotFound() {
        Mockito.when(commandRecordMapper.selectById(99L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteCommandRecord(99L));
        assertTrue(ex.getMessage().contains("不存在"));
    }

    /* ---- 台风调度 update / delete ---- */
    @Test
    void updateDispatchOrder_overwritesFieldsAndAudits() {
        FacTyphoonDispatchOrder existing = new FacTyphoonDispatchOrder();
        existing.setId(7L);
        existing.setResourceCode("TEAM-FX-01");
        existing.setResourceName("旧名");
        existing.setDispatchAction("ASSIGN");
        Mockito.when(dispatchOrderMapper.selectById(7L)).thenReturn(existing);
        Mockito.when(dispatchOrderMapper.updateById(ArgumentMatchers.any(FacTyphoonDispatchOrder.class))).thenReturn(1);

        TyphoonDispatchOrderWriteRequest req = new TyphoonDispatchOrderWriteRequest();
        req.setResourceName("炼油防汛抢险一组");
        req.setDispatchAction("CONFIRM");
        TyphoonDispatchOrderView v = service.updateDispatchOrder(7L, req);
        assertEquals("炼油防汛抢险一组", v.getResourceName());
        assertEquals("CONFIRM", v.getDispatchAction());
        assertEquals("TEAM-FX-01", v.getResourceCode(), "未传入的字段不应被覆盖");
        Mockito.verify(audit).record(ArgumentMatchers.eq("typhoon"),
                ArgumentMatchers.eq("typhoon.dispatch.update"), ArgumentMatchers.any());
    }

    @Test
    void updateDispatchOrder_rejectsIllegalAction() {
        FacTyphoonDispatchOrder existing = new FacTyphoonDispatchOrder();
        existing.setId(7L);
        Mockito.when(dispatchOrderMapper.selectById(7L)).thenReturn(existing);
        TyphoonDispatchOrderWriteRequest req = new TyphoonDispatchOrderWriteRequest();
        req.setDispatchAction("DELETE"); // 不在 ASSIGN/CONFIRM/RELEASE
        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateDispatchOrder(7L, req));
        assertTrue(ex.getMessage().contains("非法调度动作"));
    }

    @Test
    void updateDispatchOrder_notFound_throwsNotFound() {
        Mockito.when(dispatchOrderMapper.selectById(99L)).thenReturn(null);
        TyphoonDispatchOrderWriteRequest req = new TyphoonDispatchOrderWriteRequest();
        req.setDispatchAction("CONFIRM");
        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateDispatchOrder(99L, req));
        assertTrue(ex.getMessage().contains("不存在"));
    }

    @Test
    void deleteDispatchOrder_callsDeleteAndAudits() {
        FacTyphoonDispatchOrder existing = new FacTyphoonDispatchOrder();
        existing.setId(7L);
        Mockito.when(dispatchOrderMapper.selectById(7L)).thenReturn(existing);
        Mockito.when(dispatchOrderMapper.deleteById(7L)).thenReturn(1);
        service.deleteDispatchOrder(7L);
        Mockito.verify(dispatchOrderMapper).deleteById(7L);
        Mockito.verify(audit).record(ArgumentMatchers.eq("typhoon"),
                ArgumentMatchers.eq("typhoon.dispatch.delete"), ArgumentMatchers.any());
    }

    @Test
    void deleteDispatchOrder_notFound_throwsNotFound() {
        Mockito.when(dispatchOrderMapper.selectById(99L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteDispatchOrder(99L));
        assertTrue(ex.getMessage().contains("不存在"));
    }

    /* ---- 巡更执行 update / delete ---- */
    @Test
    void updatePatrolExecution_overwritesFieldsAndAudits() {
        FacPatrolExecution existing = new FacPatrolExecution();
        existing.setId(3L);
        existing.setDutyPerson("李强");
        existing.setExecResult("NORMAL");
        Mockito.when(patrolExecutionMapper.selectById(3L)).thenReturn(existing);
        Mockito.when(patrolExecutionMapper.updateById(ArgumentMatchers.any(FacPatrolExecution.class))).thenReturn(1);

        PatrolExecutionWriteRequest req = new PatrolExecutionWriteRequest();
        req.setExecResult("ABNORMAL");
        req.setFinding("罐区A 消防栓被遮挡");
        PatrolExecutionView v = service.updatePatrolExecution(3L, req);
        assertEquals("ABNORMAL", v.getExecResult());
        assertEquals("罐区A 消防栓被遮挡", v.getFinding());
        assertEquals("李强", v.getDutyPerson(), "未传入的字段不应被覆盖");
        Mockito.verify(audit).record(ArgumentMatchers.eq("fire"),
                ArgumentMatchers.eq("fire.patrol.update"), ArgumentMatchers.any());
    }

    @Test
    void updatePatrolExecution_rejectsIllegalResult() {
        FacPatrolExecution existing = new FacPatrolExecution();
        existing.setId(3L);
        Mockito.when(patrolExecutionMapper.selectById(3L)).thenReturn(existing);
        PatrolExecutionWriteRequest req = new PatrolExecutionWriteRequest();
        req.setExecResult("UNKNOWN");
        BusinessException ex = assertThrows(BusinessException.class, () -> service.updatePatrolExecution(3L, req));
        assertTrue(ex.getMessage().contains("非法巡更结果"));
    }

    @Test
    void updatePatrolExecution_notFound_throwsNotFound() {
        Mockito.when(patrolExecutionMapper.selectById(99L)).thenReturn(null);
        PatrolExecutionWriteRequest req = new PatrolExecutionWriteRequest();
        req.setExecResult("NORMAL");
        BusinessException ex = assertThrows(BusinessException.class, () -> service.updatePatrolExecution(99L, req));
        assertTrue(ex.getMessage().contains("不存在"));
    }

    @Test
    void deletePatrolExecution_callsDeleteAndAudits() {
        FacPatrolExecution existing = new FacPatrolExecution();
        existing.setId(3L);
        Mockito.when(patrolExecutionMapper.selectById(3L)).thenReturn(existing);
        Mockito.when(patrolExecutionMapper.deleteById(3L)).thenReturn(1);
        service.deletePatrolExecution(3L);
        Mockito.verify(patrolExecutionMapper).deleteById(3L);
        Mockito.verify(audit).record(ArgumentMatchers.eq("fire"),
                ArgumentMatchers.eq("fire.patrol.delete"), ArgumentMatchers.any());
    }

    @Test
    void deletePatrolExecution_notFound_throwsNotFound() {
        Mockito.when(patrolExecutionMapper.selectById(99L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.deletePatrolExecution(99L));
        assertTrue(ex.getMessage().contains("不存在"));
    }

    /* ---- 值班签到 update / delete ---- */
    @Test
    void updateDutySignIn_overwritesFieldsAndAudits() {
        FacDutySignIn existing = new FacDutySignIn();
        existing.setId(5L);
        existing.setPersonName("杨恒朋");
        existing.setSignAction("SIGN_IN");
        Mockito.when(dutySignInMapper.selectById(5L)).thenReturn(existing);
        Mockito.when(dutySignInMapper.updateById(ArgumentMatchers.any(FacDutySignIn.class))).thenReturn(1);

        DutySignInWriteRequest req = new DutySignInWriteRequest();
        req.setPersonName("杨恒朋");
        req.setSignAction("SIGN_OUT");
        DutySignInView v = service.updateDutySignIn(5L, req);
        assertEquals("SIGN_OUT", v.getSignAction());
        assertEquals("杨恒朋", v.getPersonName());
        Mockito.verify(audit).record(ArgumentMatchers.eq("emergency"),
                ArgumentMatchers.eq("emergency.duty.update"), ArgumentMatchers.any());
    }

    @Test
    void updateDutySignIn_rejectsIllegalAction() {
        FacDutySignIn existing = new FacDutySignIn();
        existing.setId(5L);
        Mockito.when(dutySignInMapper.selectById(5L)).thenReturn(existing);
        DutySignInWriteRequest req = new DutySignInWriteRequest();
        req.setSignAction("CHECK");
        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateDutySignIn(5L, req));
        assertTrue(ex.getMessage().contains("非法签到动作"));
    }

    @Test
    void updateDutySignIn_notFound_throwsNotFound() {
        Mockito.when(dutySignInMapper.selectById(99L)).thenReturn(null);
        DutySignInWriteRequest req = new DutySignInWriteRequest();
        req.setSignAction("SIGN_IN");
        BusinessException ex = assertThrows(BusinessException.class, () -> service.updateDutySignIn(99L, req));
        assertTrue(ex.getMessage().contains("不存在"));
    }

    @Test
    void deleteDutySignIn_callsDeleteAndAudits() {
        FacDutySignIn existing = new FacDutySignIn();
        existing.setId(5L);
        Mockito.when(dutySignInMapper.selectById(5L)).thenReturn(existing);
        Mockito.when(dutySignInMapper.deleteById(5L)).thenReturn(1);
        service.deleteDutySignIn(5L);
        Mockito.verify(dutySignInMapper).deleteById(5L);
        Mockito.verify(audit).record(ArgumentMatchers.eq("emergency"),
                ArgumentMatchers.eq("emergency.duty.delete"), ArgumentMatchers.any());
    }

    @Test
    void deleteDutySignIn_notFound_throwsNotFound() {
        Mockito.when(dutySignInMapper.selectById(99L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteDutySignIn(99L));
        assertTrue(ex.getMessage().contains("不存在"));
    }

    /* ==================== 工具 ==================== */

    private void stubCommandPage(List<FacEmergencyCommandRecord> rows) {
        Mockito.doAnswer(inv -> {
            IPage<FacEmergencyCommandRecord> page = inv.getArgument(0);
            page.setRecords(rows);
            return page;
        }).when(commandRecordMapper).selectPage(
                ArgumentMatchers.<IPage<FacEmergencyCommandRecord>>any(),
                ArgumentMatchers.<Wrapper<FacEmergencyCommandRecord>>any());
    }

    private void stubDispatchPage(List<FacTyphoonDispatchOrder> rows) {
        Mockito.doAnswer(inv -> {
            IPage<FacTyphoonDispatchOrder> page = inv.getArgument(0);
            page.setRecords(rows);
            return page;
        }).when(dispatchOrderMapper).selectPage(
                ArgumentMatchers.<IPage<FacTyphoonDispatchOrder>>any(),
                ArgumentMatchers.<Wrapper<FacTyphoonDispatchOrder>>any());
    }

    private static void stubInsert(FacEmergencyCommandRecordMapper mapper) {
        Mockito.doAnswer(inv -> {
            FacEmergencyCommandRecord e = inv.getArgument(0);
            e.setId(1L);
            return 1;
        }).when(mapper).insert(ArgumentMatchers.any(FacEmergencyCommandRecord.class));
    }

    private static void stubInsert(FacPatrolExecutionMapper mapper) {
        Mockito.doAnswer(inv -> {
            FacPatrolExecution e = inv.getArgument(0);
            e.setId(1L);
            return 1;
        }).when(mapper).insert(ArgumentMatchers.any(FacPatrolExecution.class));
    }

    private static void stubInsert(FacDutySignInMapper mapper) {
        Mockito.doAnswer(inv -> {
            FacDutySignIn e = inv.getArgument(0);
            e.setId(1L);
            return 1;
        }).when(mapper).insert(ArgumentMatchers.any(FacDutySignIn.class));
    }

    private static FacEmergencyCommandRecord record(Long id, String code, String currStatus) {
        FacEmergencyCommandRecord e = new FacEmergencyCommandRecord();
        e.setId(id);
        e.setCommandCode(code);
        e.setCurrStatus(currStatus);
        return e;
    }
}
