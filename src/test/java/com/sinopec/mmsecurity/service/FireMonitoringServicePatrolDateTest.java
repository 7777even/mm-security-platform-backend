package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.FirePatrolRecord;
import com.sinopec.mmsecurity.entity.FacFirePatrol;
import com.sinopec.mmsecurity.mapper.FacBrigadeTeamMapper;
import com.sinopec.mmsecurity.mapper.FacFireFacilityMonitorMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolItemDefMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolItemResultMapper;
import com.sinopec.mmsecurity.mapper.FacFirePatrolMapper;
import com.sinopec.mmsecurity.mapper.FacRescueEquipmentMapper;
import com.sinopec.mmsecurity.mapper.FacRescuePersonnelMapper;
import com.sinopec.mmsecurity.mapper.FacRescueVehicleMapper;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationStatMapper;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationTicketMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 防火巡查演示数据「相对今天」重锚定回归测试：无论库内种子日期有多陈旧，
 * patrols() 都应把最新种子日期映射到今天、其余按天递减，使大屏「今日巡查」始终有数。
 */
@ExtendWith(MockitoExtension.class)
class FireMonitoringServicePatrolDateTest {

    @Mock
    private FacSpecialOperationStatMapper specialOperationStatMapper;
    @Mock
    private FacSpecialOperationTicketMapper specialOperationTicketMapper;
    @Mock
    private FacFireFacilityMonitorMapper fireFacilityMonitorMapper;
    @Mock
    private FacBrigadeTeamMapper brigadeTeamMapper;
    @Mock
    private FacRescuePersonnelMapper rescuePersonnelMapper;
    @Mock
    private FacRescueEquipmentMapper rescueEquipmentMapper;
    @Mock
    private FacRescueVehicleMapper rescueVehicleMapper;
    @Mock
    private FacFirePatrolMapper firePatrolMapper;
    @Mock
    private FacFirePatrolItemDefMapper patrolItemDefMapper;
    @Mock
    private FacFirePatrolItemResultMapper patrolItemResultMapper;

    @InjectMocks
    private FireMonitoringService service;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static FacFirePatrol row(Long id, String date) {
        FacFirePatrol p = new FacFirePatrol();
        p.setId(id);
        p.setPatrolDate(date);
        p.setShiftName("上午");
        p.setDutyPerson("张三");
        p.setPatrolCount("第1次");
        p.setLocations("1#联合装置,中央控制室");
        p.setCompleted(true);
        p.setWorkOrderNo(null);
        return p;
    }

    @Test
    void patrols_rebaseStaleSeedDatesToToday() {
        // 模拟 V75 很久以前跑过、种子日期已严重陈旧（与今天无关）
        when(firePatrolMapper.selectList(any())).thenReturn(List.of(
                row(1L, "2020-01-04"),
                row(2L, "2020-01-03"),
                row(3L, "2020-01-02"),
                row(4L, "2020-01-01")
        ));
        when(patrolItemDefMapper.selectList(any())).thenReturn(List.of());
        when(patrolItemResultMapper.selectList(any())).thenReturn(List.of());

        List<FirePatrolRecord> recs = service.patrols();
        assertEquals(4, recs.size());

        LocalDate today = LocalDate.now();
        List<String> expected = List.of(
                today.minusDays(3).format(DTF),
                today.minusDays(2).format(DTF),
                today.minusDays(1).format(DTF),
                today.format(DTF)
        );
        List<String> actual = recs.stream().map(FirePatrolRecord::getPatrolDate)
                .sorted().toList();
        assertEquals(expected, actual, "patrols() 必须把种子日期重锚定到今天及前 3 天");
    }
}
