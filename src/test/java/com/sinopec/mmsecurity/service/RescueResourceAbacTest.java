package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sinopec.mmsecurity.dto.FireBrigadeList;
import com.sinopec.mmsecurity.entity.FacBrigadeTeam;
import com.sinopec.mmsecurity.mapper.FacBrigadeTeamMapper;
import com.sinopec.mmsecurity.mapper.FacRescueEquipmentMapper;
import com.sinopec.mmsecurity.mapper.FacRescueOptionMapper;
import com.sinopec.mmsecurity.mapper.FacRescuePersonnelMapper;
import com.sinopec.mmsecurity.mapper.FacRescueVehicleMapper;
import com.sinopec.mmsecurity.security.DataScopeResolver;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 救援队伍域 data_scope 行级 ABAC 接入验证（纯 Mockito）：
 * 校验 brigades() 将 DataScopeResolver 解析结果正确注入查询条件。
 */
@ExtendWith(MockitoExtension.class)
class RescueResourceAbacTest {

    @Mock
    private FacBrigadeTeamMapper brigadeTeamMapper;
    @Mock
    private FacRescueVehicleMapper vehicleMapper;
    @Mock
    private FacRescuePersonnelMapper personnelMapper;
    @Mock
    private FacRescueEquipmentMapper equipmentMapper;
    @Mock
    private FacRescueOptionMapper optionMapper;
    @Mock
    private DataScopeResolver dataScopeResolver;
    @Mock
    private EmergencyService emergencyService;

    @InjectMocks
    private RescueResourceService service;

    /**
     * 纯 Mockito 不起 Spring，MyBatis-Plus 的 TableInfo 缓存未初始化，
     * selfScope 用例解析 IN 列段的 lambda 列时报 "can not find lambda cache"。
     * 全量套件下依赖其它 Spring 上下文测试类先初始化才碰巧通过；此处显式注入，单独跑也稳定。
     */
    @BeforeAll
    static void initTableInfoCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        TableInfoHelper.initTableInfo(assistant, FacBrigadeTeam.class);
    }

    private static FacBrigadeTeam team(String area) {
        FacBrigadeTeam t = new FacBrigadeTeam();
        t.setId(1L);
        t.setTeamName(area + "中队");
        t.setArea(area);
        return t;
    }

    @Test
    void allScope_noExtraFilter() {
        when(dataScopeResolver.resolveZones()).thenReturn(null);
        when(brigadeTeamMapper.selectList(any())).thenReturn(List.of(team("乙烯区")));
        when(vehicleMapper.selectList(any())).thenReturn(List.of());
        when(personnelMapper.selectList(any())).thenReturn(List.of());
        when(equipmentMapper.selectList(any())).thenReturn(List.of());
        when(optionMapper.selectList(any())).thenReturn(List.of());

        FireBrigadeList list = service.brigades(null);

        ArgumentCaptor<LambdaQueryWrapper<FacBrigadeTeam>> cap = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(brigadeTeamMapper).selectList(cap.capture());
        assertThat(cap.getValue().getCustomSqlSegment()).doesNotContain("IN").doesNotContain("1=0");
        assertThat(list.getItems()).hasSize(1);
    }

    @Test
    void selfScope_withZones_injectsInClause() {
        Set<String> zones = Set.of("乙烯区", "罐区");
        when(dataScopeResolver.resolveZones()).thenReturn(zones);
        when(brigadeTeamMapper.selectList(any())).thenReturn(List.of(team("乙烯区"), team("罐区"), team("仓储区")));
        when(vehicleMapper.selectList(any())).thenReturn(List.of());
        when(personnelMapper.selectList(any())).thenReturn(List.of());
        when(equipmentMapper.selectList(any())).thenReturn(List.of());
        when(optionMapper.selectList(any())).thenReturn(List.of());

        service.brigades(null);

        ArgumentCaptor<LambdaQueryWrapper<FacBrigadeTeam>> cap = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(brigadeTeamMapper).selectList(cap.capture());
        LambdaQueryWrapper<FacBrigadeTeam> qw = cap.getValue();
        assertThat(qw.getCustomSqlSegment()).contains("IN");
        boolean found = zones.stream().allMatch(z ->
                qw.getParamNameValuePairs().values().stream().anyMatch(z::equals));
        assertThat(found).isTrue();
    }

    @Test
    void selfScope_emptyZones_injectsOneEqualsZero() {
        when(dataScopeResolver.resolveZones()).thenReturn(Set.of());
        when(brigadeTeamMapper.selectList(any())).thenReturn(List.of());
        when(optionMapper.selectList(any())).thenReturn(List.of());

        service.brigades(null);

        ArgumentCaptor<LambdaQueryWrapper<FacBrigadeTeam>> cap = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(brigadeTeamMapper).selectList(cap.capture());
        assertThat(cap.getValue().getCustomSqlSegment()).contains("1=0");
    }
}
