package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.BlacklistSummary;
import com.sinopec.mmsecurity.entity.FacBlacklistEntry;
import com.sinopec.mmsecurity.mapper.FacBlacklistEntryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** 安防黑名单服务逻辑校验（纯 Mockito，不起 Spring 上下文、不连 DB）。 */
@ExtendWith(MockitoExtension.class)
class BlacklistServiceTest {

    @Mock
    private FacBlacklistEntryMapper blacklistEntryMapper;

    @InjectMocks
    private BlacklistService service;

    private static FacBlacklistEntry entry(Long id, String kind, String subject, String idCard,
                                           String reason, String time, String status, int sortNo) {
        FacBlacklistEntry entry = new FacBlacklistEntry();
        entry.setId(id);
        entry.setEntryKind(kind);
        entry.setSubjectName(subject);
        entry.setIdCard(idCard);
        entry.setReasonText(reason);
        entry.setEventTime(time);
        entry.setEntryStatus(status);
        entry.setSortNo(sortNo);
        return entry;
    }

    @Test
    void blacklist_splitsVehiclesAndPersons() {
        when(blacklistEntryMapper.selectList(any())).thenReturn(List.of(
                entry(1L, "VEHICLE", "粤K·A4543", null, "违规闯入生产区", "2026-08-05 14:20:11", "生效中", 1),
                entry(3L, "VEHICLE", "粤K·C6610", null, "逾期未出厂", "2026-07-28 18:40:02", "已解除", 3),
                entry(4L, "PERSON", "张**", "4409**********1234", "未佩戴安全帽进入高危区", "2026-08-06 10:02:45", "生效中", 1)));

        BlacklistSummary summary = service.blacklist();

        assertEquals(2, summary.getVehicles().size());
        assertEquals("粤K·A4543", summary.getVehicles().get(0).getPlate());
        assertEquals("违规闯入生产区", summary.getVehicles().get(0).getReason());
        assertEquals("2026-08-05 14:20:11", summary.getVehicles().get(0).getTime());
        assertEquals("已解除", summary.getVehicles().get(1).getStatus());
        assertEquals(1, summary.getPersons().size());
        assertEquals("张**", summary.getPersons().get(0).getName());
        assertEquals("4409**********1234", summary.getPersons().get(0).getIdCard());
        assertEquals(4L, summary.getPersons().get(0).getId());
    }

    @Test
    void blacklist_handlesEmptyTable() {
        when(blacklistEntryMapper.selectList(any())).thenReturn(List.of());

        BlacklistSummary summary = service.blacklist();

        assertEquals(0, summary.getVehicles().size());
        assertEquals(0, summary.getPersons().size());
    }

    @Test
    void blacklist_mapsVehicleFieldsWithoutIdCard() {
        when(blacklistEntryMapper.selectList(any())).thenReturn(List.of(
                entry(2L, "VEHICLE", "粤K·B2871", null, "超速行驶", "2026-08-02 09:15:33", "生效中", 2)));

        BlacklistSummary summary = service.blacklist();

        assertEquals(1, summary.getVehicles().size());
        assertEquals(2L, summary.getVehicles().get(0).getId());
        assertEquals("粤K·B2871", summary.getVehicles().get(0).getPlate());
        assertEquals("超速行驶", summary.getVehicles().get(0).getReason());
        assertEquals("生效中", summary.getVehicles().get(0).getStatus());
        assertEquals(0, summary.getPersons().size());
    }

    @Test
    void removeVehicle_okWhenVehicleEntryExists() {
        when(blacklistEntryMapper.selectById(1L))
                .thenReturn(entry(1L, "VEHICLE", "粤K·A4543", null, "违规闯入生产区",
                        "2026-08-05 14:20:11", "生效中", 1));
        when(blacklistEntryMapper.deleteById(1L)).thenReturn(1);

        assertTrue(service.removeVehicle(1L).getOk());
    }

    @Test
    void removeVehicle_falseWhenKindMismatch() {
        when(blacklistEntryMapper.selectById(4L))
                .thenReturn(entry(4L, "PERSON", "张**", "4409**********1234", "未佩戴安全帽",
                        "2026-08-06 10:02:45", "生效中", 1));

        assertFalse(service.removeVehicle(4L).getOk());
    }

    @Test
    void removePerson_falseWhenEntryMissing() {
        when(blacklistEntryMapper.selectById(99L)).thenReturn(null);

        assertFalse(service.removePerson(99L).getOk());
    }
}
