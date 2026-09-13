package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.AccidentRescueIncident;
import com.sinopec.mmsecurity.entity.FacAccidentAuxStat;
import com.sinopec.mmsecurity.entity.FacAccidentDetailField;
import com.sinopec.mmsecurity.entity.FacAccidentDispatchResource;
import com.sinopec.mmsecurity.entity.FacAccidentDutyPerson;
import com.sinopec.mmsecurity.entity.FacAccidentDynamic;
import com.sinopec.mmsecurity.entity.FacAccidentIncident;
import com.sinopec.mmsecurity.mapper.FacAccidentAuxStatMapper;
import com.sinopec.mmsecurity.mapper.FacAccidentDetailFieldMapper;
import com.sinopec.mmsecurity.mapper.FacAccidentDispatchResourceMapper;
import com.sinopec.mmsecurity.mapper.FacAccidentDutyPersonMapper;
import com.sinopec.mmsecurity.mapper.FacAccidentDynamicMapper;
import com.sinopec.mmsecurity.mapper.FacAccidentIncidentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccidentRescueServiceTest {

    @Mock
    private FacAccidentIncidentMapper incidentMapper;
    @Mock
    private FacAccidentDetailFieldMapper detailFieldMapper;
    @Mock
    private FacAccidentDispatchResourceMapper dispatchResourceMapper;
    @Mock
    private FacAccidentDutyPersonMapper dutyPersonMapper;
    @Mock
    private FacAccidentAuxStatMapper auxStatMapper;
    @Mock
    private FacAccidentDynamicMapper dynamicMapper;

    @InjectMocks
    private AccidentRescueService service;

    private static FacAccidentIncident inc(Long id, Long eventId) {
        FacAccidentIncident e = new FacAccidentIncident();
        e.setId(id);
        e.setEventId(eventId);
        e.setTitle("事件");
        e.setLocation("位置");
        return e;
    }

    @Test
    void incident_byId_assemblesAllTables() {
        when(incidentMapper.selectOne(any())).thenReturn(inc(1L, 4L));
        when(detailFieldMapper.selectList(any())).thenReturn(List.of(new FacAccidentDetailField()));
        when(dispatchResourceMapper.selectList(any())).thenReturn(List.of(new FacAccidentDispatchResource()));
        when(dutyPersonMapper.selectList(any())).thenReturn(List.of(new FacAccidentDutyPerson()));
        when(auxStatMapper.selectList(any())).thenReturn(List.of(new FacAccidentAuxStat()));
        when(dynamicMapper.selectList(any())).thenReturn(List.of(new FacAccidentDynamic()));

        AccidentRescueIncident dto = service.incident(1L);
        assertNotNull(dto);
        assertEquals(4L, dto.getEventId());
        assertEquals(1, dto.getDetailFields().size());
        assertEquals(1, dto.getDispatchResources().size());
        assertEquals(1, dto.getDutyPersons().size());
        assertEquals(1, dto.getAuxiliaryStats().size());
        assertEquals(1, dto.getDynamics().size());
    }

    @Test
    void incident_nullEventId_fallsBackToDefault() {
        when(incidentMapper.selectOne(any())).thenReturn(inc(9L, 4L));
        when(detailFieldMapper.selectList(any())).thenReturn(List.of());
        when(dispatchResourceMapper.selectList(any())).thenReturn(List.of());
        when(dutyPersonMapper.selectList(any())).thenReturn(List.of());
        when(auxStatMapper.selectList(any())).thenReturn(List.of());
        when(dynamicMapper.selectList(any())).thenReturn(List.of());

        AccidentRescueIncident dto = service.incident(null);
        assertNotNull(dto);
        assertEquals(4L, dto.getEventId());
    }

    @Test
    void incident_noData_returnsNull() {
        when(incidentMapper.selectOne(any())).thenReturn(null, null);
        assertNull(service.incident(5L));
    }

    @Test
    void incident_emptyReferenceTables_ok() {
        when(incidentMapper.selectOne(any())).thenReturn(inc(1L, 4L));
        when(detailFieldMapper.selectList(any())).thenReturn(List.of());
        when(dispatchResourceMapper.selectList(any())).thenReturn(List.of());
        when(dutyPersonMapper.selectList(any())).thenReturn(List.of());
        when(auxStatMapper.selectList(any())).thenReturn(List.of());
        when(dynamicMapper.selectList(any())).thenReturn(List.of());

        AccidentRescueIncident dto = service.incident(1L);
        assertNotNull(dto);
        assertTrue(dto.getDetailFields().isEmpty());
        assertTrue(dto.getDispatchResources().isEmpty());
    }
}
