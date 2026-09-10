package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.BollardItem;
import com.sinopec.mmsecurity.dto.GateControlItem;
import com.sinopec.mmsecurity.dto.PatrolCameraItem;
import com.sinopec.mmsecurity.dto.PersonSearchDetail;
import com.sinopec.mmsecurity.dto.PersonSearchResult;
import com.sinopec.mmsecurity.dto.SecurityEvent;
import com.sinopec.mmsecurity.dto.SecurityTrackSummary;
import com.sinopec.mmsecurity.dto.SecurityTrackTimelineItem;
import com.sinopec.mmsecurity.dto.VehicleSearchDetail;
import com.sinopec.mmsecurity.dto.VehicleSearchResult;
import com.sinopec.mmsecurity.entity.FacBollard;
import com.sinopec.mmsecurity.entity.FacGateControl;
import com.sinopec.mmsecurity.entity.FacPatrolCamera;
import com.sinopec.mmsecurity.entity.FacPersonSearch;
import com.sinopec.mmsecurity.entity.FacSecurityEvent;
import com.sinopec.mmsecurity.entity.FacSecurityTrack;
import com.sinopec.mmsecurity.entity.FacSecurityTrackMeta;
import com.sinopec.mmsecurity.entity.FacVehicleSearch;
import com.sinopec.mmsecurity.mapper.FacBollardMapper;
import com.sinopec.mmsecurity.mapper.FacGateControlMapper;
import com.sinopec.mmsecurity.mapper.FacPatrolCameraMapper;
import com.sinopec.mmsecurity.mapper.FacPersonSearchMapper;
import com.sinopec.mmsecurity.mapper.FacSecurityEventMapper;
import com.sinopec.mmsecurity.mapper.FacSecurityTrackMapper;
import com.sinopec.mmsecurity.mapper.FacSecurityTrackMetaMapper;
import com.sinopec.mmsecurity.mapper.FacVehicleSearchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

/**
 * 安全防恐业务服务：巡逻摄像机 / 道闸 / 防恐柱 / 车辆·人员识别检索 / 门禁事件。
 * 全部来自真实表，不再返回前端本地占位数据。检索端点支持 keyword 在服务端按车牌/卡口/状态(车辆)、
 * 姓名/卡口/状态(人员)做大小写不敏感模糊匹配；keyword 为空时返回全量。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final FacPatrolCameraMapper patrolCameraMapper;
    private final FacGateControlMapper gateControlMapper;
    private final FacBollardMapper bollardMapper;
    private final FacVehicleSearchMapper vehicleSearchMapper;
    private final FacPersonSearchMapper personSearchMapper;
    private final FacSecurityEventMapper securityEventMapper;
    private final FacSecurityTrackMapper trackMapper;
    private final FacSecurityTrackMetaMapper trackMetaMapper;

    public List<PatrolCameraItem> listPatrolCameras() {
        return patrolCameraMapper.selectList(null).stream().map(this::toCamera).toList();
    }

    public List<GateControlItem> listGateControls() {
        return gateControlMapper.selectList(null).stream().map(this::toGate).toList();
    }

    public List<BollardItem> listBollards() {
        return bollardMapper.selectList(null).stream().map(this::toBollard).toList();
    }

    public List<VehicleSearchResult> searchVehicles(String keyword) {
        Predicate<FacVehicleSearch> p = containsAny(keyword,
                v -> v.getPlate(), v -> v.getGate(), v -> v.getStatus());
        return vehicleSearchMapper.selectList(null).stream()
                .filter(p)
                .map(this::toVehicle)
                .toList();
    }

    public List<PersonSearchResult> searchPersons(String keyword) {
        Predicate<FacPersonSearch> p = containsAny(keyword,
                v -> v.getName(), v -> v.getGate(), v -> v.getStatus());
        return personSearchMapper.selectList(null).stream()
                .filter(p)
                .map(this::toPerson)
                .toList();
    }

    public List<SecurityEvent> listSecurityEvents() {
        return securityEventMapper.selectList(null).stream().map(this::toEvent).toList();
    }

    /** 轨迹时间轴：按模式 + 实体查询；该实体无记录时回落到该模式的默认轨迹（entity_id IS NULL）。 */
    public List<SecurityTrackTimelineItem> trackTimeline(String mode, Long entityId) {
        List<FacSecurityTrack> rows = queryTrack(mode, entityId);
        if (rows.isEmpty() && entityId != null) {
            rows = queryTrack(mode, null);
        }
        return rows.stream().map(this::toTrackItem).toList();
    }

    private List<FacSecurityTrack> queryTrack(String mode, Long entityId) {
        LambdaQueryWrapper<FacSecurityTrack> q = new LambdaQueryWrapper<FacSecurityTrack>()
                .eq(FacSecurityTrack::getTrackMode, mode);
        if (entityId == null) {
            q.isNull(FacSecurityTrack::getEntityId);
        } else {
            q.eq(FacSecurityTrack::getEntityId, entityId);
        }
        return trackMapper.selectList(q.orderByAsc(FacSecurityTrack::getSeqNo));
    }

    /** 轨迹概要：起止点标签来自 fac_security_track_meta，时间范围由时间轴首尾推导。 */
    public SecurityTrackSummary trackSummary(String mode, Long entityId) {
        List<SecurityTrackTimelineItem> timeline = trackTimeline(mode, entityId);
        FacSecurityTrackMeta meta = trackMetaMapper.selectById(mode);
        SecurityTrackSummary s = new SecurityTrackSummary();
        s.setStartLabel(meta == null ? null : meta.getStartLabel());
        s.setEndLabel(meta == null ? null : meta.getEndLabel());
        s.setTimeRange(timeline.isEmpty() ? "—"
                : timeline.get(0).getTime() + " - " + timeline.get(timeline.size() - 1).getTime());
        return s;
    }

    /** 车辆识别检索详情；未找到返回 null。 */
    public VehicleSearchDetail vehicleDetail(Long id) {
        if (id == null) return null;
        FacVehicleSearch e = vehicleSearchMapper.selectById(id);
        return e == null ? null : toVehicleDetail(e);
    }

    /** 人员识别检索详情；未找到返回 null。 */
    public PersonSearchDetail personDetail(Long id) {
        if (id == null) return null;
        FacPersonSearch e = personSearchMapper.selectById(id);
        return e == null ? null : toPersonDetail(e);
    }

    private SecurityTrackTimelineItem toTrackItem(FacSecurityTrack e) {
        SecurityTrackTimelineItem d = new SecurityTrackTimelineItem();
        d.setId(e.getId());
        d.setLocation(e.getLocation());
        d.setStatus(e.getStatus());
        d.setStatusTone(e.getStatusTone());
        d.setTime(e.getTrackTime());
        d.setCaptureHint(e.getCaptureHint());
        return d;
    }

    private VehicleSearchDetail toVehicleDetail(FacVehicleSearch e) {
        VehicleSearchDetail d = new VehicleSearchDetail();
        d.setId(e.getId());
        d.setPlate(e.getPlate());
        d.setConfidence(e.getConfidence());
        d.setGate(e.getGate());
        d.setStatus(e.getStatus());
        d.setTime(e.getTime());
        d.setVehicleType(e.getVehicleType());
        d.setDriverName(e.getDriverName());
        d.setDriverPhone(e.getDriverPhone());
        d.setCompany(e.getCompany());
        d.setAppointmentNo(e.getAppointmentNo());
        d.setAppointmentTime(e.getAppointmentTime());
        d.setVisitPurpose(e.getVisitPurpose());
        d.setWaybillNo(e.getWaybillNo());
        d.setCargo(e.getCargo());
        d.setDestination(e.getDestination());
        return d;
    }

    private PersonSearchDetail toPersonDetail(FacPersonSearch e) {
        PersonSearchDetail d = new PersonSearchDetail();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setGate(e.getGate());
        d.setStatus(e.getStatus());
        d.setDate(e.getDate());
        d.setGender(e.getGender());
        d.setPhone(e.getPhone());
        d.setCompany(e.getCompany());
        d.setIdNumber(e.getIdNumber());
        d.setAppointmentNo(e.getAppointmentNo());
        d.setAppointmentTime(e.getAppointmentTime());
        d.setVisitPurpose(e.getVisitPurpose());
        d.setSpecialOperation(e.getSpecialOperation());
        d.setOperationArea(e.getOperationArea());
        return d;
    }

    /** 构造「keyword 为空→恒真；否则任一字段包含 keyword(忽略大小写)」的谓词 */
    @SafeVarargs
    private <T> Predicate<T> containsAny(String keyword, java.util.function.Function<T, String>... getters) {
        if (keyword == null || keyword.isBlank()) {
            return t -> true;
        }
        String k = keyword.toLowerCase();
        return t -> {
            for (var g : getters) {
                String v = g.apply(t);
                if (v != null && v.toLowerCase().contains(k)) return true;
            }
            return false;
        };
    }

    private PatrolCameraItem toCamera(FacPatrolCamera e) {
        PatrolCameraItem d = new PatrolCameraItem();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setZone(e.getZone());
        d.setStatus(e.getStatus());
        d.setLongitude(e.getLongitude());
        d.setLatitude(e.getLatitude());
        return d;
    }

    private GateControlItem toGate(FacGateControl e) {
        GateControlItem d = new GateControlItem();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setLocation(e.getLocation());
        d.setStatus(e.getStatus());
        d.setLongitude(e.getLongitude());
        d.setLatitude(e.getLatitude());
        return d;
    }

    private BollardItem toBollard(FacBollard e) {
        BollardItem d = new BollardItem();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setZone(e.getZone());
        d.setStatus(e.getStatus());
        d.setLongitude(e.getLongitude());
        d.setLatitude(e.getLatitude());
        return d;
    }

    private VehicleSearchResult toVehicle(FacVehicleSearch e) {
        VehicleSearchResult d = new VehicleSearchResult();
        d.setId(e.getId());
        d.setPlate(e.getPlate());
        d.setConfidence(e.getConfidence());
        d.setGate(e.getGate());
        d.setStatus(e.getStatus());
        d.setTime(e.getTime());
        return d;
    }

    private PersonSearchResult toPerson(FacPersonSearch e) {
        PersonSearchResult d = new PersonSearchResult();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setGate(e.getGate());
        d.setStatus(e.getStatus());
        d.setDate(e.getDate());
        return d;
    }

    private SecurityEvent toEvent(FacSecurityEvent e) {
        SecurityEvent d = new SecurityEvent();
        d.setEventId(e.getEventId());
        d.setPerson(e.getPerson());
        d.setChannel(e.getChannel());
        d.setCardId(e.getCardId());
        d.setVehicle(e.getVehicle());
        d.setDirection(e.getDirection());
        d.setLevel(e.getLevel());
        d.setTs(e.getTs());
        return d;
    }
}
