package com.sinopec.mmsecurity.websocket;

import com.sinopec.mmsecurity.config.AbacZoneMappingProperties;
import com.sinopec.mmsecurity.dto.AlarmItem;
import com.sinopec.mmsecurity.dto.BollardItem;
import com.sinopec.mmsecurity.dto.CommunicationDevice;
import com.sinopec.mmsecurity.dto.EmergencyCaseItem;
import com.sinopec.mmsecurity.dto.EmergencyEventItem;
import com.sinopec.mmsecurity.dto.FireAlarmItem;
import com.sinopec.mmsecurity.dto.FireBrigadeTeam;
import com.sinopec.mmsecurity.dto.FireFacilityLedgerItem;
import com.sinopec.mmsecurity.dto.GateControlItem;
import com.sinopec.mmsecurity.dto.PatrolExecutionView;
import com.sinopec.mmsecurity.dto.PerimeterAlarmDetail;
import com.sinopec.mmsecurity.dto.PersonSearchDetail;
import com.sinopec.mmsecurity.dto.ProductionAlarmItem;
import com.sinopec.mmsecurity.dto.RescueEquipmentItem;
import com.sinopec.mmsecurity.dto.RescueVehicleItem;
import com.sinopec.mmsecurity.dto.SpecialOperationItem;
import com.sinopec.mmsecurity.dto.TvMonitorSummary;
import com.sinopec.mmsecurity.dto.VideoCameraItem;
import com.sinopec.mmsecurity.security.ZoneMappingResolver;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ABAC 实时广播防区收紧 —— 写方法返回 DTO 的 {@link ZoneAware} 接线单测。
 *
 * <p>为什么测 DTO 而不是实体：{@code RealtimeSyncAspect} 的防区扩展点取的是
 * <b>写方法的返回值</b>，而绝大多数写方法返回的是 DTO / View（不是实体）。因此只有返回值类型
 * 实现 {@code ZoneAware} 才能真正收紧广播范围（此前仅 {@code FacDevice} 生效）。</p>
 *
 * <p>本测试锁两件事：① 各 DTO 暴露的位置 / 防区字段与预期一致（防字段名漂移）；
 * ② 未配置映射时 {@code ZoneMappingResolver} 返回 null → fail-open（不得把「有 location」误判成「有防区」）。</p>
 */
class ZoneAwareDtoWiringTest {

    private static final String LOC = "厂区南门";

    /** 每个 DTO：构造器 + 位置/防区字段 setter + 期望的暴露方式。 */
    private record Case(String name, java.util.function.Supplier<ZoneAware> factory,
                        Consumer<Object> setter, String expectLocation, String expectZoneName) {
    }

    private static Case loc(String name, java.util.function.Supplier<ZoneAware> f, Consumer<Object> setter) {
        return new Case(name, f, setter, LOC, null);
    }

    private static Case zone(String name, java.util.function.Supplier<ZoneAware> f, Consumer<Object> setter) {
        return new Case(name, f, setter, null, "炼油区");
    }

    private static List<Case> cases() {
        return List.of(
                loc("AlarmItem", AlarmItem::new, o -> ((AlarmItem) o).setLocation(LOC)),
                loc("PatrolExecutionView", PatrolExecutionView::new, o -> ((PatrolExecutionView) o).setLocation(LOC)),
                loc("CommunicationDevice", CommunicationDevice::new, o -> ((CommunicationDevice) o).setLocation(LOC)),
                loc("EmergencyEventItem", EmergencyEventItem::new, o -> ((EmergencyEventItem) o).setLocation(LOC)),
                loc("EmergencyCaseItem", EmergencyCaseItem::new, o -> ((EmergencyCaseItem) o).setLocation(LOC)),
                loc("FireAlarmItem", FireAlarmItem::new, o -> ((FireAlarmItem) o).setLocation(LOC)),
                loc("FireFacilityLedgerItem", FireFacilityLedgerItem::new,
                        o -> ((FireFacilityLedgerItem) o).setLocation(LOC)),
                loc("ProductionAlarmItem", ProductionAlarmItem::new, o -> ((ProductionAlarmItem) o).setLocation(LOC)),
                loc("FireBrigadeTeam", FireBrigadeTeam::new, o -> ((FireBrigadeTeam) o).setLocation(LOC)),
                loc("RescueVehicleItem", RescueVehicleItem::new, o -> ((RescueVehicleItem) o).setParkingLocation(LOC)),
                loc("RescueEquipmentItem", RescueEquipmentItem::new,
                        o -> ((RescueEquipmentItem) o).setStorageLocation(LOC)),
                loc("PersonSearchDetail", PersonSearchDetail::new, o -> ((PersonSearchDetail) o).setOperationArea(LOC)),
                loc("GateControlItem", GateControlItem::new, o -> ((GateControlItem) o).setLocation(LOC)),
                loc("PerimeterAlarmDetail", PerimeterAlarmDetail::new, o -> ((PerimeterAlarmDetail) o).setLocation(LOC)),
                loc("SpecialOperationItem", SpecialOperationItem::new, o -> ((SpecialOperationItem) o).setLocation(LOC)),
                loc("VideoCameraItem", VideoCameraItem::new, o -> ((VideoCameraItem) o).setLocation(LOC)),
                zone("BollardItem", BollardItem::new, o -> ((BollardItem) o).setZone("炼油区")),
                zone("TvMonitorSummary", TvMonitorSummary::new, o -> ((TvMonitorSummary) o).setZoneName("炼油区"))
        );
    }

    @Test
    void allWriteReturnDtosImplementZoneAware() {
        for (Case c : cases()) {
            ZoneAware z = c.factory().get();
            assertTrue(z instanceof ZoneAware, c.name() + " 应实现 ZoneAware");
        }
    }

    @Test
    void exposedFieldMatchesExpectation() {
        for (Case c : cases()) {
            ZoneAware z = c.factory().get();
            c.setter().accept(z);
            if (c.expectZoneName() != null) {
                assertEquals(c.expectZoneName(), z.getZoneName(), c.name() + " 的 getZoneName() 字段漂移");
            } else {
                assertEquals(c.expectLocation(), z.getLocation(), c.name() + " 的 getLocation() 字段漂移");
                assertNull(z.getZoneName(), c.name() + " 未直接持有防区名时应返回 null（改走 location 映射）");
            }
        }
    }

    @Test
    void unmappedLocationResolvesToNull_failOpen() {
        // 未配置任何 location→防区 映射：有 location 也不得凭空产生防区（必须 fail-open）
        ZoneMappingResolver resolver = new ZoneMappingResolver(new AbacZoneMappingProperties());
        for (Case c : cases()) {
            ZoneAware z = c.factory().get();
            c.setter().accept(z);
            if (c.expectLocation() != null) {
                assertNull(resolver.resolveZonesByLocation(z.getLocation()),
                        c.name() + "：未配置映射时不得产生防区");
            }
        }
    }

    @Test
    void mappedLocationResolvesToZones() {
        // 产品填入映射后：location 命中即解析出防区集合（无需改代码）
        AbacZoneMappingProperties props = new AbacZoneMappingProperties();
        props.setLocationToZones(Map.of(LOC, List.of("炼油区", "罐区")));
        ZoneMappingResolver resolver = new ZoneMappingResolver(props);
        assertEquals(Set.of("炼油区", "罐区"), resolver.resolveZonesByLocation(LOC));
    }

    @Test
    void blankLocationFailsOpen() {
        ZoneMappingResolver resolver = new ZoneMappingResolver(new AbacZoneMappingProperties());
        assertNull(resolver.resolveZonesByLocation("   "));
        assertNull(resolver.resolveZonesByLocation(null));
    }
}
