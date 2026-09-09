package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.SpecialOperationDetail;
import com.sinopec.mmsecurity.dto.SpecialOperationGasPoint;
import com.sinopec.mmsecurity.dto.SpecialOperationItem;
import com.sinopec.mmsecurity.dto.SpecialOperationPage;
import com.sinopec.mmsecurity.dto.SpecialOperationPersonItem;
import com.sinopec.mmsecurity.dto.SpecialOperationVideoItem;
import com.sinopec.mmsecurity.entity.FacSpecialOperationGas;
import com.sinopec.mmsecurity.entity.FacSpecialOperationPerson;
import com.sinopec.mmsecurity.entity.FacSpecialOperationTicket;
import com.sinopec.mmsecurity.entity.FacSpecialOperationVideo;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationGasMapper;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationPersonMapper;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationTicketMapper;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationVideoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 特殊作业大屏（壳内特殊作业面板 + 消防特殊作业弹窗）服务。
 *
 * <p>数据来源为 V16 落地的 fac_special_operation_* 真实表，取代前端硬编码的
 * specialOperationMock。与 /fire/special-operations 统计端点（V10）职责不同：本域为作业票明细。
 * 筛选参数沿用前端下拉文案，「全部xx」前缀按不过滤处理。
 */
@Service
@RequiredArgsConstructor
public class SpecialOperationService {

    private final FacSpecialOperationTicketMapper ticketMapper;
    private final FacSpecialOperationVideoMapper videoMapper;
    private final FacSpecialOperationGasMapper gasMapper;
    private final FacSpecialOperationPersonMapper personMapper;

    /** 作业票分页（支持类型/区域/等级/状态筛选，「全部xx」按不过滤处理）。 */
    public SpecialOperationPage list(int page, int size, String type, String area, String level, String status) {
        LambdaQueryWrapper<FacSpecialOperationTicket> qw = new LambdaQueryWrapper<>();
        qw.eq(notBlankFilter(type), FacSpecialOperationTicket::getOpType, type)
          .eq(notBlankFilter(area), FacSpecialOperationTicket::getTicketArea, area)
          .eq(notBlankFilter(level), FacSpecialOperationTicket::getOpLevel, level)
          .eq(notBlankFilter(status), FacSpecialOperationTicket::getTicketStatus, status)
          .orderByAsc(FacSpecialOperationTicket::getSortNo);
        Page<FacSpecialOperationTicket> p = ticketMapper.selectPage(new Page<>(page, size), qw);

        SpecialOperationPage result = new SpecialOperationPage();
        result.setTotal(p.getTotal());
        result.setPage(page);
        result.setSize(size);
        result.setPages((int) Math.ceil(p.getTotal() / (double) size));
        result.setList(p.getRecords().stream().map(this::toItem).collect(Collectors.toList()));
        return result;
    }

    /** 「全部xx」下拉文案与空串均视为不过滤。 */
    private boolean notBlankFilter(String value) {
        return value != null && !value.isBlank() && !value.startsWith("全部");
    }

    /** 作业票详情（含现场视频/气体检测点/作业人员）；未命中返回 null，由 Controller 转 404。 */
    public SpecialOperationDetail detail(Long id) {
        FacSpecialOperationTicket ticket = ticketMapper.selectById(id);
        if (ticket == null) {
            return null;
        }
        SpecialOperationDetail detail = new SpecialOperationDetail();
        copyScalars(ticket, detail);
        detail.setVideos(videoMapper.selectList(new LambdaQueryWrapper<FacSpecialOperationVideo>()
                        .eq(FacSpecialOperationVideo::getTicketId, id)
                        .orderByAsc(FacSpecialOperationVideo::getId)).stream()
                .map(v -> {
                    SpecialOperationVideoItem item = new SpecialOperationVideoItem();
                    item.setId(v.getId());
                    item.setName(v.getName());
                    item.setLocation(v.getLocation());
                    return item;
                }).collect(Collectors.toList()));
        detail.setGasPoints(gasMapper.selectList(new LambdaQueryWrapper<FacSpecialOperationGas>()
                        .eq(FacSpecialOperationGas::getTicketId, id)
                        .orderByAsc(FacSpecialOperationGas::getId)).stream()
                .map(g -> {
                    SpecialOperationGasPoint point = new SpecialOperationGasPoint();
                    point.setId(g.getId());
                    point.setName(g.getName());
                    point.setValue(g.getValueText());
                    point.setStatus(g.getStatusName());
                    return point;
                }).collect(Collectors.toList()));
        detail.setPersonnel(personMapper.selectList(new LambdaQueryWrapper<FacSpecialOperationPerson>()
                        .eq(FacSpecialOperationPerson::getTicketId, id)
                        .orderByAsc(FacSpecialOperationPerson::getId)).stream()
                .map(per -> {
                    SpecialOperationPersonItem item = new SpecialOperationPersonItem();
                    item.setId(per.getId());
                    item.setName(per.getName());
                    item.setRole(per.getRoleName());
                    item.setPhone(per.getPhone());
                    return item;
                }).collect(Collectors.toList()));
        return detail;
    }

    private SpecialOperationItem toItem(FacSpecialOperationTicket ticket) {
        SpecialOperationItem item = new SpecialOperationItem();
        copyScalars(ticket, item);
        return item;
    }

    private void copyScalars(FacSpecialOperationTicket t, SpecialOperationItem item) {
        item.setId(t.getId());
        item.setArea(t.getTicketArea());
        item.setType(t.getOpType());
        item.setLevel(t.getOpLevel());
        item.setStatus(t.getTicketStatus());
        item.setStartTime(t.getStartTime());
        item.setEndTime(t.getEndTime());
        item.setTimeRange(t.getTimeRange());
        item.setUnit(t.getWorkUnit());
        item.setApplyUnit(t.getApplyUnit());
        item.setOperationDate(t.getOperationDate());
        item.setLocation(t.getWorkLocation());
        item.setIsContractor(t.getIsContractor());
        item.setHazardType(t.getHazardType());
        item.setLeaderName(t.getLeaderName());
        item.setLeaderPhone(t.getLeaderPhone());
        item.setPosition(t.getPosition());
        item.setLongitude(t.getLongitude());
        item.setLatitude(t.getLatitude());
        item.setChangeReason(t.getChangeReason());
        item.setCancelReason(t.getCancelReason());
        item.setGuardianName(t.getGuardianName());
        item.setWorkers(t.getWorkers());
        item.setPermitNo(t.getPermitNo());
        item.setContent(t.getContent());
        item.setVideoCount(t.getVideoCount());
        item.setGasMonitorCount(t.getGasMonitorCount());
        item.setPersonnelCount(t.getPersonnelCount());
    }
}
