package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.annotation.RealtimeSync;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.SysConfigNode;
import com.sinopec.mmsecurity.dto.SysConfigSaveRequest;
import com.sinopec.mmsecurity.entity.SysConfig;
import com.sinopec.mmsecurity.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 系统参数配置服务（运行期可配项自助管理）。
 *
 * <p>任何结构变更经 {@code @RealtimeSync(domain = "system.config")} 广播，
 * 前端配置中心订阅刷新。built_in=1 的内置配置不可删除（防误删系统级开关）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private static final Set<String> CONFIG_TYPES = Set.of("STRING", "NUMBER", "BOOLEAN", "JSON", "SELECT");

    private final SysConfigMapper configMapper;
    private final SystemAuditHelper audit;

    /** 配置列表（按 config_group 可选过滤，按 sort_order 升序）。 */
    public List<SysConfigNode> list(String group) {
        LambdaQueryWrapper<SysConfig> q = new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getDeleted, 0);
        if (group != null && !group.isBlank()) {
            q.eq(SysConfig::getConfigGroup, group);
        }
        q.orderByAsc(SysConfig::getSortOrder);
        List<SysConfig> all = configMapper.selectList(q);
        List<SysConfigNode> nodes = new ArrayList<>();
        for (SysConfig c : all) {
            nodes.add(toNode(c));
        }
        return nodes;
    }

    @Transactional
    @RealtimeSync(domain = "system.config")
    public SysConfigNode create(SysConfigSaveRequest req) {
        String key = req.getConfigKey().trim();
        Long dup = configMapper.selectCount(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key).eq(SysConfig::getDeleted, 0));
        if (dup != null && dup > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "配置键已存在：" + key);
        }
        SysConfig c = new SysConfig();
        applyRequest(c, req);
        c.setConfigKey(key);
        c.setBuiltIn(0);
        c.setDeleted(0);
        configMapper.insert(c);

        audit.record("system.config.create", Map.of("configKey", key));
        return toNode(c);
    }

    @Transactional
    @RealtimeSync(domain = "system.config")
    public SysConfigNode update(Long id, SysConfigSaveRequest req) {
        SysConfig c = require(id);
        String key = req.getConfigKey().trim();
        if (!key.equalsIgnoreCase(c.getConfigKey())) {
            Long dup = configMapper.selectCount(new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getConfigKey, key).eq(SysConfig::getDeleted, 0).ne(SysConfig::getId, id));
            if (dup != null && dup > 0) {
                throw new BusinessException(ResultCode.CONFLICT, "配置键已存在：" + key);
            }
        }
        applyRequest(c, req);
        c.setConfigKey(key);
        configMapper.updateById(c);

        audit.record("system.config.update", Map.of("configKey", key));
        return toNode(c);
    }

    /** 删除配置：内置配置（built_in=1）拒绝删除。 */
    @Transactional
    @RealtimeSync(domain = "system.config")
    public DeleteResult delete(Long id) {
        SysConfig c = require(id);
        if (c.getBuiltIn() != null && c.getBuiltIn() == 1) {
            throw new BusinessException(ResultCode.CONFLICT, "内置配置不可删除：" + c.getConfigKey());
        }
        configMapper.deleteById(id);

        audit.record("system.config.delete", Map.of("configKey", c.getConfigKey()));

        DeleteResult d = new DeleteResult();
        d.setOk(true);
        return d;
    }

    // ---------------------------------------------------------------- 内部工具

    private void applyRequest(SysConfig c, SysConfigSaveRequest req) {
        String type = req.getConfigType() == null ? "" : req.getConfigType().trim().toUpperCase(Locale.ROOT);
        if (!type.isBlank() && !CONFIG_TYPES.contains(type)) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "configType 只能为 STRING/NUMBER/BOOLEAN/JSON/SELECT");
        }
        c.setConfigValue(req.getConfigValue());
        c.setConfigName(req.getConfigName());
        c.setConfigGroup(req.getConfigGroup());
        c.setConfigType(type.isBlank() ? "STRING" : type);
        c.setOptions(req.getOptions());
        c.setRemark(req.getRemark());
        c.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());
        c.setStatus(req.getStatus() == null ? 1 : req.getStatus());
    }

    private SysConfig require(Long id) {
        SysConfig c = configMapper.selectById(id);
        if (c == null || (c.getDeleted() != null && c.getDeleted() == 1)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "配置不存在");
        }
        return c;
    }

    private SysConfigNode toNode(SysConfig c) {
        SysConfigNode n = new SysConfigNode();
        n.setId(c.getId());
        n.setConfigKey(c.getConfigKey());
        n.setConfigValue(c.getConfigValue());
        n.setConfigName(c.getConfigName());
        n.setConfigGroup(c.getConfigGroup());
        n.setConfigType(c.getConfigType());
        n.setOptions(c.getOptions());
        n.setRemark(c.getRemark());
        n.setSortOrder(c.getSortOrder());
        n.setStatus(c.getStatus());
        n.setBuiltIn(c.getBuiltIn());
        n.setCreatedAt(c.getCreatedAt());
        n.setUpdatedAt(c.getUpdatedAt());
        return n;
    }
}
