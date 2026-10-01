package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.annotation.RealtimeSync;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.FormRecordCreateRequest;
import com.sinopec.mmsecurity.dto.FormRecordItem;
import com.sinopec.mmsecurity.dto.FormRecordPageResult;
import com.sinopec.mmsecurity.dto.FormRecordUpdateRequest;
import com.sinopec.mmsecurity.entity.FacFormRecord;
import com.sinopec.mmsecurity.mapper.FacFormRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 流程填报记录服务（mgmt /form 流程填报向导后端化）。
 *
 * <p>列表按 id 降序（最新在前）；详情未命中抛 B3 NOT_FOUND；新增校验必填项；
 * 更新为 read-modify-write + @Version 乐观锁，版本冲突转 B3 CONFLICT(409)。
 */
@Service
@RequiredArgsConstructor
public class FormRecordService {

    private static final Set<String> VALID_STATUS = Set.of("DRAFT", "SUBMITTED", "REVIEWED");
    private static final Set<String> VALID_FORM_TYPE = Set.of("隐患排查", "设备巡检", "值班交接", "其他");
    private static final DateTimeFormatter FILL_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORM_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final FacFormRecordMapper formRecordMapper;

    /** 分页列表（按 id 降序，最新在前）。 */
    public FormRecordPageResult list(long page, long size) {
        IPage<FacFormRecord> p = formRecordMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<FacFormRecord>().orderByDesc(FacFormRecord::getId));
        FormRecordPageResult result = new FormRecordPageResult();
        result.setList(p.getRecords().stream().map(this::toItem).toList());
        result.setTotal(p.getTotal());
        result.setPage(p.getCurrent());
        result.setSize(p.getSize());
        return result;
    }

    /** 详情；未命中抛 B3 NOT_FOUND。 */
    public FormRecordItem get(long id) {
        FacFormRecord e = formRecordMapper.selectById(id);
        if (e == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "填报记录不存在：" + id);
        }
        return toItem(e);
    }

    /** 新增填报（写端点）。 */
    @RealtimeSync(domain = "form.record")
    public FormRecordItem create(FormRecordCreateRequest req) {
        if (req.getFormType() == null || req.getFormType().isBlank()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "填报类型不能为空");
        }
        if (!VALID_FORM_TYPE.contains(req.getFormType())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "非法填报类型：" + req.getFormType());
        }
        if (req.getReporter() == null || req.getReporter().isBlank()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "填报人不能为空");
        }
        if (req.getDetailJson() == null || req.getDetailJson().isBlank()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "填报内容不能为空");
        }
        FacFormRecord e = new FacFormRecord();
        e.setFormNo(req.getFormNo() != null && !req.getFormNo().isBlank()
                ? req.getFormNo() : generateFormNo());
        e.setFormType(req.getFormType());
        e.setTitle(req.getTitle());
        e.setReporter(req.getReporter());
        e.setDepartment(req.getDepartment());
        e.setFillAt(req.getFillAt() != null && !req.getFillAt().isBlank()
                ? req.getFillAt() : LocalDateTime.now().format(FILL_FMT));
        e.setDetailJson(req.getDetailJson());
        e.setStatus(normalizeStatus(req.getStatus(), "SUBMITTED"));
        e.setRemark(req.getRemark());
        e.setVersion(0L);
        // V66 种子以显式 id（1、2）插入，而 H2 / PG / 达梦的自增序列都不会因显式插入而推进，
        // 于是新增行仍从 id=1 起跳、撞主键 → create 恒定 409（dev 环境「新建填报」彻底不可用）。
        // 显式分配 max(id)+1 规避，三种方言行为一致，不依赖各库序列的滞后状态。
        e.setId(nextId());
        formRecordMapper.insert(e);
        return toItem(e);
    }

    /** 局部更新（状态流转等）。read-modify-write + @Version 乐观锁。 */
    @RealtimeSync(domain = "form.record")
    public FormRecordItem update(long id, FormRecordUpdateRequest req) {
        FacFormRecord e = formRecordMapper.selectById(id);
        if (e == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "填报记录不存在：" + id);
        }
        if (req.getFormType() != null) {
            if (!VALID_FORM_TYPE.contains(req.getFormType())) {
                throw new BusinessException(ResultCode.PARAM_INVALID, "非法填报类型：" + req.getFormType());
            }
            e.setFormType(req.getFormType());
        }
        if (req.getTitle() != null) e.setTitle(req.getTitle());
        if (req.getReporter() != null) e.setReporter(req.getReporter());
        if (req.getDepartment() != null) e.setDepartment(req.getDepartment());
        if (req.getFillAt() != null) e.setFillAt(req.getFillAt());
        if (req.getDetailJson() != null) e.setDetailJson(req.getDetailJson());
        if (req.getStatus() != null) e.setStatus(normalizeStatus(req.getStatus(), e.getStatus()));
        if (req.getRemark() != null) e.setRemark(req.getRemark());
        if (req.getVersion() != null) e.setVersion(req.getVersion());
        try {
            formRecordMapper.updateById(e);
        } catch (OptimisticLockingFailureException ex) {
            throw new BusinessException(ResultCode.CONFLICT, "记录已被他人修改，请刷新后重试");
        }
        return toItem(e);
    }

    /** 取下一个主键：max(id)+1（见 create 内关于自增序列滞后的说明）。 */
    private long nextId() {
        FacFormRecord last = formRecordMapper.selectOne(new LambdaQueryWrapper<FacFormRecord>()
                .orderByDesc(FacFormRecord::getId)
                .last("LIMIT 1"));
        return last == null || last.getId() == null ? 1L : last.getId() + 1;
    }

    /**
     * 删除流程填报记录（物理删除：fac_form_record 无 deleted 列，不做逻辑删）。
     *
     * <p>记录不存在抛 B3 NOT_FOUND（重复删除同样返回 NOT_FOUND 而非静默成功，便于前端识别）。
     * 标记 {@code @RealtimeSync(domain="form.record")}，管理端与已订阅该域的页面自动重拉。</p>
     */
    @RealtimeSync(domain = "form.record")
    public void delete(long id) {
        FacFormRecord e = formRecordMapper.selectById(id);
        if (e == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "填报记录不存在：" + id);
        }
        formRecordMapper.deleteById(id);
    }

    private String normalizeStatus(String raw, String fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        if (!VALID_STATUS.contains(raw)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "非法状态：" + raw);
        }
        return raw;
    }

    private String generateFormNo() {
        String ts = LocalDateTime.now().format(FORM_NO_FMT);
        int r = ThreadLocalRandom.current().nextInt(1000);
        return "FR-" + ts + "-" + String.format("%03d", r);
    }

    private FormRecordItem toItem(FacFormRecord e) {
        FormRecordItem d = new FormRecordItem();
        d.setId(e.getId());
        d.setFormNo(e.getFormNo());
        d.setFormType(e.getFormType());
        d.setTitle(e.getTitle());
        d.setReporter(e.getReporter());
        d.setDepartment(e.getDepartment());
        d.setFillAt(e.getFillAt());
        d.setDetailJson(e.getDetailJson());
        d.setStatus(e.getStatus());
        d.setRemark(e.getRemark());
        d.setVersion(e.getVersion());
        return d;
    }
}
