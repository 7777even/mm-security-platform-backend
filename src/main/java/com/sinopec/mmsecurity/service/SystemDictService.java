package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.DictItemItem;
import com.sinopec.mmsecurity.dto.DictItemPageResult;
import com.sinopec.mmsecurity.dto.DictItemSaveRequest;
import com.sinopec.mmsecurity.dto.DictTypeItem;
import com.sinopec.mmsecurity.dto.DictTypePageResult;
import com.sinopec.mmsecurity.dto.DictTypeSaveRequest;
import com.sinopec.mmsecurity.entity.SysDictItem;
import com.sinopec.mmsecurity.entity.SysDictType;
import com.sinopec.mmsecurity.mapper.SysDictItemMapper;
import com.sinopec.mmsecurity.mapper.SysDictTypeMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 数据字典服务（字典类型 + 字典项两级）。
 *
 * <p>对外提供两类读取：</p>
 * <ul>
 *   <li><b>管理读取</b>（{@code /system/dict-types}、{@code /system/dict-items}）：分页、含停用项，供维护界面；</li>
 *   <li><b>业务读取</b>（{@code /system/dicts/{dictCode}}）：仅返回启用项、按 sort_order 排序，
 *       走 Caffeine 缓存（TTL 5min + 写时整表失效），供各业务下拉。字典是极少变更的引用数据，
 *       适合缓存；质量上仍以库表为准。</li>
 * </ul>
 *
 * <p>硬防护：内置字典类型（{@code built_in=1}）禁删除；字典项必须挂在已存在的字典类型下。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemDictService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictItemMapper dictItemMapper;
    private final SystemAuditHelper audit;

    private Cache<String, List<DictItemItem>> optionsCache;

    @PostConstruct
    void init() {
        this.optionsCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(256)
                .build();
    }

    // ---------------------------------------------------------------- 字典类型

    public DictTypePageResult typePage(long page, long size, String keyword) {
        LambdaQueryWrapper<SysDictType> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim();
            qw.and(w -> w.like(SysDictType::getDictCode, k).or().like(SysDictType::getDictName, k));
        }
        qw.orderByAsc(SysDictType::getId);

        Page<SysDictType> p = dictTypeMapper.selectPage(new Page<>(page, size), qw);
        DictTypePageResult r = new DictTypePageResult();
        r.setList(p.getRecords().stream().map(this::toTypeItem).toList());
        r.setTotal(p.getTotal());
        r.setPage(p.getCurrent());
        r.setSize(p.getSize());
        return r;
    }

    @Transactional
    public DictTypeItem createType(DictTypeSaveRequest req) {
        String code = normalizeCode(req.getDictCode());
        requireCodeFree(code, null);
        SysDictType t = new SysDictType();
        t.setDictCode(code);
        t.setDictName(req.getDictName());
        t.setDescription(req.getDescription());
        t.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        t.setBuiltIn(0);
        t.setDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        t.setCreatedAt(now);
        t.setUpdatedAt(now);
        dictTypeMapper.insert(t);

        invalidateOptions();
        audit.record("system.dict-type.create", Map.of("dictCode", code));
        return toTypeItem(t);
    }

    @Transactional
    public DictTypeItem updateType(Long id, DictTypeSaveRequest req) {
        SysDictType t = requireType(id);
        String code = normalizeCode(req.getDictCode());
        boolean builtIn = t.getBuiltIn() != null && t.getBuiltIn() == 1;
        if (builtIn && !code.equalsIgnoreCase(t.getDictCode())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "内置字典的标识不可修改");
        }
        if (!code.equalsIgnoreCase(t.getDictCode())) {
            requireCodeFree(code, id);
            // 字典标识变更时同步迁移字典项，避免产生孤儿项
            for (SysDictItem item : dictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
                    .eq(SysDictItem::getDictCode, t.getDictCode()))) {
                item.setDictCode(code);
                item.setUpdatedAt(LocalDateTime.now());
                dictItemMapper.updateById(item);
            }
        }
        t.setDictCode(code);
        t.setDictName(req.getDictName());
        t.setDescription(req.getDescription());
        if (req.getStatus() != null) {
            t.setStatus(req.getStatus());
        }
        t.setUpdatedAt(LocalDateTime.now());
        dictTypeMapper.updateById(t);

        invalidateOptions();
        audit.record("system.dict-type.update", Map.of("dictCode", code));
        return toTypeItem(t);
    }

    @Transactional
    public DeleteResult deleteType(Long id) {
        SysDictType t = requireType(id);
        if (t.getBuiltIn() != null && t.getBuiltIn() == 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "内置字典不可删除");
        }
        Long items = dictItemMapper.selectCount(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictCode, t.getDictCode()));
        if (items != null && items > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "该字典下仍有 " + items + " 个字典项，请先删除字典项");
        }
        dictTypeMapper.deleteById(id);

        invalidateOptions();
        audit.record("system.dict-type.delete", Map.of("dictCode", t.getDictCode()));
        DeleteResult d = new DeleteResult();
        d.setOk(true);
        return d;
    }

    // ---------------------------------------------------------------- 字典项

    public DictItemPageResult itemPage(long page, long size, String dictCode) {
        if (dictCode == null || dictCode.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "dictCode 不能为空");
        }
        LambdaQueryWrapper<SysDictItem> qw = new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictCode, normalizeCode(dictCode))
                .orderByAsc(SysDictItem::getSortOrder)
                .orderByAsc(SysDictItem::getId);

        Page<SysDictItem> p = dictItemMapper.selectPage(new Page<>(page, size), qw);
        DictItemPageResult r = new DictItemPageResult();
        r.setList(p.getRecords().stream().map(this::toItemDto).toList());
        r.setTotal(p.getTotal());
        r.setPage(p.getCurrent());
        r.setSize(p.getSize());
        return r;
    }

    @Transactional
    public DictItemItem createItem(DictItemSaveRequest req) {
        String code = normalizeCode(req.getDictCode());
        requireTypeByCode(code);
        SysDictItem item = new SysDictItem();
        applyItem(item, req, code);
        item.setDeleted(0);
        LocalDateTime now = LocalDateTime.now();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        dictItemMapper.insert(item);

        invalidateOptions();
        audit.record("system.dict-item.create", Map.of("dictCode", code, "itemValue", item.getItemValue()));
        return toItemDto(item);
    }

    @Transactional
    public DictItemItem updateItem(Long id, DictItemSaveRequest req) {
        SysDictItem item = dictItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "字典项不存在");
        }
        String code = normalizeCode(req.getDictCode());
        requireTypeByCode(code);
        applyItem(item, req, code);
        item.setUpdatedAt(LocalDateTime.now());
        dictItemMapper.updateById(item);

        invalidateOptions();
        audit.record("system.dict-item.update", Map.of("dictCode", code, "itemValue", item.getItemValue()));
        return toItemDto(item);
    }

    @Transactional
    public DeleteResult deleteItem(Long id) {
        SysDictItem item = dictItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "字典项不存在");
        }
        dictItemMapper.deleteById(id);

        invalidateOptions();
        audit.record("system.dict-item.delete",
                Map.of("dictCode", String.valueOf(item.getDictCode()), "itemValue", String.valueOf(item.getItemValue())));
        DeleteResult d = new DeleteResult();
        d.setOk(true);
        return d;
    }

    /** 业务读取：按字典标识取启用项（缓存）。 */
    public List<DictItemItem> options(String dictCode) {
        String code = normalizeCode(dictCode);
        return optionsCache.get(code, this::loadOptions);
    }

    // ---------------------------------------------------------------- 内部工具

    private List<DictItemItem> loadOptions(String code) {
        return dictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getDictCode, code)
                        .eq(SysDictItem::getStatus, 1)
                        .orderByAsc(SysDictItem::getSortOrder)
                        .orderByAsc(SysDictItem::getId)).stream()
                .map(this::toItemDto)
                .toList();
    }

    private void invalidateOptions() {
        if (optionsCache != null) {
            optionsCache.invalidateAll();
        }
    }

    private void applyItem(SysDictItem item, DictItemSaveRequest req, String code) {
        item.setDictCode(code);
        item.setItemValue(req.getItemValue());
        item.setItemLabel(req.getItemLabel());
        item.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        item.setStatus(req.getStatus() == null ? 1 : req.getStatus());
        item.setDescription(req.getDescription());
    }

    private SysDictType requireType(Long id) {
        SysDictType t = dictTypeMapper.selectById(id);
        if (t == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "字典类型不存在");
        }
        return t;
    }

    private void requireTypeByCode(String code) {
        Long n = dictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictCode, code));
        if (n == null || n == 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "字典类型不存在：" + code);
        }
    }

    private void requireCodeFree(String code, Long excludeId) {
        // 含逻辑删除行判重（唯一索引 + 逻辑删除的语义鸿沟，见 SysUserMapper#countUsernameIncludingDeleted）
        long dup = dictTypeMapper.countDictCodeIncludingDeleted(code, excludeId == null ? -1L : excludeId);
        if (dup > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "字典标识已存在（不可复用，含历史已删除字典）：" + code);
        }
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toLowerCase(Locale.ROOT);
    }

    private DictTypeItem toTypeItem(SysDictType t) {
        DictTypeItem item = new DictTypeItem();
        item.setId(t.getId());
        item.setDictCode(t.getDictCode());
        item.setDictName(t.getDictName());
        item.setDescription(t.getDescription());
        item.setStatus(t.getStatus());
        item.setBuiltIn(t.getBuiltIn() != null && t.getBuiltIn() == 1);
        item.setCreatedAt(t.getCreatedAt());
        return item;
    }

    private DictItemItem toItemDto(SysDictItem i) {
        DictItemItem d = new DictItemItem();
        d.setId(i.getId());
        d.setDictCode(i.getDictCode());
        d.setItemValue(i.getItemValue());
        d.setItemLabel(i.getItemLabel());
        d.setSortOrder(i.getSortOrder());
        d.setStatus(i.getStatus());
        d.setDescription(i.getDescription());
        return d;
    }
}
