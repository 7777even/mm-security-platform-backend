package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.util.function.Function;

/**
 * 台账主键 / 排序号分配器。
 *
 * <p><b>为什么需要它</b>：多个 Flyway 迁移（V19 / V62 / V66 …）在种子数据里
 * <b>显式指定了 id</b>（如 {@code INSERT ... (id, ...) VALUES (1, ...)}），
 * 而 H2 / PostgreSQL / 达梦的自增序列（identity / serial / IDENTITY）都只在
 * 「不指定 id」时推进，显式插入<b>不会</b>同步序列。于是后续新增行仍从 id=1 起跳、
 * 撞主键 → {@code DataIntegrityViolationException} → 前端看到 409「数据冲突」，
 * 表现为「新增按钮永远保存不了」。</p>
 *
 * <p>修法有三条，本类选第二条：</p>
 * <ol>
 *   <li>新增迁移对齐序列：三方言语法各异且需动态取 max(id)+1，静态 SQL 难写、易撞号；</li>
 *   <li><b>代码显式分配 max(id)+1</b>：三方言行为一致、零迁移、对既有多态库都生效；</li>
 *   <li>改种子不显式插 id：已执行的迁移不可改（Flyway checksum），对既有库无效。</li>
 * </ol>
 *
 * <p><b>代价</b>：并发插入可能算出同一个 id 而撞主键。台账是低频人工维护，
 * 且失败返回明确 409 提示重试，优于「每次新增都必失败」的现状。</p>
 *
 * <p>已接入：{@link FormRecordService}、{@link RescueResourceService}。
 * 后续新增台账写端点请一律走本分配器，不要再依赖数据库自增。</p>
 */
public final class LedgerIdSupport {

    private LedgerIdSupport() {
    }

    /**
     * 取下一个主键：max(id) + 1（空表从 1 起）。
     *
     * @param mapper 实体 mapper
     * @param orderRef 用于排序的 id 方法引用（如 {@code FacRescueVehicle::getId}）
     * @param getter 从实体取 id 值的函数（与 orderRef 同一列）
     */
    public static <T> long nextId(BaseMapper<T> mapper,
                                  SFunction<T, ?> orderRef,
                                  Function<T, Long> getter) {
        T last = mapper.selectOne(new LambdaQueryWrapper<T>()
                .orderByDesc(orderRef)
                .last("LIMIT 1"));
        Long id = last == null ? null : getter.apply(last);
        return id == null ? 1L : id + 1;
    }

    /**
     * 取下一个排序号：max(sort_no) + 1（空表从 1 起）。
     * 与 {@link #nextId} 同理，保证新行排在台账末尾而不是最前。
     */
    public static <T> int nextSortNo(BaseMapper<T> mapper,
                                     SFunction<T, ?> orderRef,
                                     Function<T, Integer> getter) {
        T last = mapper.selectOne(new LambdaQueryWrapper<T>()
                .orderByDesc(orderRef)
                .last("LIMIT 1"));
        Integer sortNo = last == null ? null : getter.apply(last);
        return sortNo == null ? 1 : sortNo + 1;
    }
}
