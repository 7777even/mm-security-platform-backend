package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.annotation.RealtimeSync;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.PersonSearchDetail;
import com.sinopec.mmsecurity.dto.PersonSearchWriteRequest;
import com.sinopec.mmsecurity.dto.VehicleSearchDetail;
import com.sinopec.mmsecurity.dto.VehicleSearchWriteRequest;
import com.sinopec.mmsecurity.entity.FacPerimeterAlarm;
import com.sinopec.mmsecurity.entity.FacPersonSearch;
import com.sinopec.mmsecurity.entity.FacVehicleSearch;
import com.sinopec.mmsecurity.mapper.FacPerimeterAlarmMapper;
import com.sinopec.mmsecurity.mapper.FacPersonSearchMapper;
import com.sinopec.mmsecurity.mapper.FacVehicleSearchMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecuritySearchCrudServiceTest {

    @Mock
    private FacPersonSearchMapper personSearchMapper;

    @Mock
    private FacVehicleSearchMapper vehicleSearchMapper;

    @Mock
    private FacPerimeterAlarmMapper perimeterAlarmMapper;

    @InjectMocks
    private SecurityService service;

    @Test
    void createPerson_persistsAllFieldsAndVersion() {
        PersonSearchWriteRequest req = personRequest("张三");

        PersonSearchDetail result = service.createPerson(req);

        ArgumentCaptor<FacPersonSearch> captor = ArgumentCaptor.forClass(FacPersonSearch.class);
        verify(personSearchMapper).insert(captor.capture());
        FacPersonSearch saved = captor.getValue();
        assertEquals("张三", saved.getName());
        assertEquals("高处作业", saved.getSpecialOperation());
        assertEquals("炼油一区", saved.getOperationArea());
        assertEquals(0L, saved.getVersion());
        assertEquals("张三", result.getName());
    }

    @Test
    void updatePerson_updatesExistingRowAndKeepsVersion() {
        FacPersonSearch existing = new FacPersonSearch();
        existing.setId(11L);
        existing.setVersion(3L);
        existing.setName("旧姓名");
        when(personSearchMapper.selectById(11L)).thenReturn(existing);

        PersonSearchDetail result = service.updatePerson(11L, personRequest("新姓名"));

        ArgumentCaptor<FacPersonSearch> captor = ArgumentCaptor.forClass(FacPersonSearch.class);
        verify(personSearchMapper).updateById(captor.capture());
        assertEquals(11L, captor.getValue().getId());
        assertEquals(3L, captor.getValue().getVersion());
        assertEquals("新姓名", captor.getValue().getName());
        assertEquals("新姓名", result.getName());
    }

    @Test
    void updatePerson_notFound_throwsNotFound() {
        when(personSearchMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.updatePerson(99L, personRequest("张三")));

        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
        verify(personSearchMapper, never()).updateById(any());
    }

    @Test
    void deletePerson_existingRow_deletesById() {
        FacPersonSearch existing = new FacPersonSearch();
        existing.setId(11L);
        when(personSearchMapper.selectById(11L)).thenReturn(existing);

        service.deletePerson(11L);

        verify(personSearchMapper).deleteById(11L);
    }

    @Test
    void deletePerson_notFound_throwsNotFound() {
        when(personSearchMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.deletePerson(99L));

        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
        verify(personSearchMapper, never()).deleteById(any(Long.class));
    }

    @Test
    void createVehicle_persistsAllFieldsAndVersion() {
        VehicleSearchWriteRequest req = vehicleRequest("粤K12345");

        VehicleSearchDetail result = service.createVehicle(req);

        ArgumentCaptor<FacVehicleSearch> captor = ArgumentCaptor.forClass(FacVehicleSearch.class);
        verify(vehicleSearchMapper).insert(captor.capture());
        FacVehicleSearch saved = captor.getValue();
        assertEquals("粤K12345", saved.getPlate());
        assertEquals(98, saved.getConfidence());
        assertEquals("甲醇", saved.getCargo());
        assertEquals("炼油一区装卸点", saved.getDestination());
        assertEquals(0L, saved.getVersion());
        assertEquals("粤K12345", result.getPlate());
    }

    @Test
    void updateVehicle_updatesExistingRowAndKeepsVersion() {
        FacVehicleSearch existing = new FacVehicleSearch();
        existing.setId(21L);
        existing.setVersion(4L);
        existing.setPlate("旧车牌");
        when(vehicleSearchMapper.selectById(21L)).thenReturn(existing);

        VehicleSearchDetail result = service.updateVehicle(21L, vehicleRequest("粤K54321"));

        ArgumentCaptor<FacVehicleSearch> captor = ArgumentCaptor.forClass(FacVehicleSearch.class);
        verify(vehicleSearchMapper).updateById(captor.capture());
        assertEquals(21L, captor.getValue().getId());
        assertEquals(4L, captor.getValue().getVersion());
        assertEquals("粤K54321", captor.getValue().getPlate());
        assertEquals("粤K54321", result.getPlate());
    }

    @Test
    void updateVehicle_notFound_throwsNotFound() {
        when(vehicleSearchMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.updateVehicle(99L, vehicleRequest("粤K12345")));

        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
        verify(vehicleSearchMapper, never()).updateById(any());
    }

    @Test
    void deleteVehicle_existingRow_deletesById() {
        FacVehicleSearch existing = new FacVehicleSearch();
        existing.setId(21L);
        when(vehicleSearchMapper.selectById(21L)).thenReturn(existing);

        service.deleteVehicle(21L);

        verify(vehicleSearchMapper).deleteById(21L);
    }

    @Test
    void deleteVehicle_notFound_throwsNotFound() {
        when(vehicleSearchMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteVehicle(99L));

        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
        verify(vehicleSearchMapper, never()).deleteById(any(Long.class));
    }

    @Test
    void deletePerimeterAlarm_existingRow_deletesById() {
        FacPerimeterAlarm existing = new FacPerimeterAlarm();
        existing.setId(31L);
        when(perimeterAlarmMapper.selectById(31L)).thenReturn(existing);

        service.deletePerimeterAlarm(31L);

        verify(perimeterAlarmMapper).deleteById(31L);
    }

    @Test
    void deletePerimeterAlarm_notFound_throwsNotFound() {
        when(perimeterAlarmMapper.selectById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.deletePerimeterAlarm(99L));

        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
        verify(perimeterAlarmMapper, never()).deleteById(any(Long.class));
    }

    @Test
    void writeMethods_useExpectedRealtimeDomains() throws Exception {
        assertRealtimeDomain("createPerson", new Class<?>[] { PersonSearchWriteRequest.class }, "security.person-search");
        assertRealtimeDomain("updatePerson", new Class<?>[] { Long.class, PersonSearchWriteRequest.class }, "security.person-search");
        assertRealtimeDomain("deletePerson", new Class<?>[] { Long.class }, "security.person-search");
        assertRealtimeDomain("createVehicle", new Class<?>[] { VehicleSearchWriteRequest.class }, "security.vehicle-search");
        assertRealtimeDomain("updateVehicle", new Class<?>[] { Long.class, VehicleSearchWriteRequest.class }, "security.vehicle-search");
        assertRealtimeDomain("deleteVehicle", new Class<?>[] { Long.class }, "security.vehicle-search");
        assertRealtimeDomain("deletePerimeterAlarm", new Class<?>[] { Long.class }, "security.perimeter-alarm");
    }

    private void assertRealtimeDomain(String name, Class<?>[] parameterTypes, String domain) throws Exception {
        Method method = SecurityService.class.getMethod(name, parameterTypes);
        assertEquals(domain, method.getAnnotation(RealtimeSync.class).domain());
    }

    private static PersonSearchWriteRequest personRequest(String name) {
        PersonSearchWriteRequest req = new PersonSearchWriteRequest();
        req.setName(name);
        req.setGate("东门");
        req.setStatus("入厂");
        req.setDate("2026-10-03");
        req.setGender("男");
        req.setPhone("13800000000");
        req.setCompany("茂名石化检修公司");
        req.setIdNumber("440900000000000000");
        req.setAppointmentNo("AP-001");
        req.setAppointmentTime("2026-10-03 09:00:00");
        req.setVisitPurpose("设备检修");
        req.setSpecialOperation("高处作业");
        req.setOperationArea("炼油一区");
        return req;
    }

    private static VehicleSearchWriteRequest vehicleRequest(String plate) {
        VehicleSearchWriteRequest req = new VehicleSearchWriteRequest();
        req.setPlate(plate);
        req.setConfidence(98);
        req.setGate("南门");
        req.setStatus("入厂");
        req.setTime("2026-10-03 09:10:00");
        req.setVehicleType("危化品运输车");
        req.setDriverName("李师傅");
        req.setDriverPhone("13900000000");
        req.setCompany("运输公司");
        req.setAppointmentNo("VA-001");
        req.setAppointmentTime("2026-10-03 09:00:00");
        req.setVisitPurpose("物料运输");
        req.setWaybillNo("WB-001");
        req.setCargo("甲醇");
        req.setDestination("炼油一区装卸点");
        return req;
    }
}
