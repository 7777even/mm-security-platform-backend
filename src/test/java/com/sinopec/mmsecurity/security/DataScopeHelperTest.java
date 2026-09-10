package com.sinopec.mmsecurity.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.entity.SysUser;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/** DataScopeHelper.apply 三态语义（纯工具，无依赖）。 */
class DataScopeHelperTest {

    private static String whereSql(LambdaQueryWrapper<SysUser> qw) {
        return qw.getCustomSqlSegment();
    }

    private static boolean hasZoneValues(LambdaQueryWrapper<SysUser> qw, Set<String> expected) {
        // MP 将 IN 集合拆成多个独立具名参数（ewparam0/1/...），需把 param 值打平后逐一比对。
        return expected.stream().allMatch(z ->
                qw.getParamNameValuePairs().values().stream().anyMatch(z::equals));
    }

    @Test
    void nullZones_noCondition() {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        DataScopeHelper.apply(qw, SysUser::getUsername, null);

        assertThat(whereSql(qw)).doesNotContain("IN").doesNotContain("1=0");
    }

    @Test
    void emptyZones_appliesOneEqualsZero() {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        DataScopeHelper.apply(qw, SysUser::getUsername, Set.of());

        assertThat(whereSql(qw)).contains("1=0");
    }

    @Test
    void nonEmptyZones_appliesInWithValues() {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        Set<String> zones = new LinkedHashSet<>();
        zones.add("炼油区");
        zones.add("罐区");
        DataScopeHelper.apply(qw, SysUser::getUsername, zones);

        assertThat(whereSql(qw)).contains("IN");
        assertThat(hasZoneValues(qw, zones)).isTrue();
    }
}
