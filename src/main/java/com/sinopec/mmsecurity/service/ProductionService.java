package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.OverviewGridItem;
import com.sinopec.mmsecurity.dto.PersonnelMarker;
import com.sinopec.mmsecurity.dto.PersonnelSlice;
import com.sinopec.mmsecurity.dto.ProductionAlarmItem;
import com.sinopec.mmsecurity.dto.ProductionAreaDetail;
import com.sinopec.mmsecurity.dto.ProductionAreaMetric;
import com.sinopec.mmsecurity.dto.ProductionAreaZone;
import com.sinopec.mmsecurity.dto.ProductionDeviceItem;
import com.sinopec.mmsecurity.dto.ProductionDevicePage;
import com.sinopec.mmsecurity.dto.ProductionOverview;
import com.sinopec.mmsecurity.dto.RiskSummary;
import com.sinopec.mmsecurity.dto.RiskWarningItem;
import com.sinopec.mmsecurity.dto.StatOverviewItem;
import com.sinopec.mmsecurity.entity.FacProductionAlarm;
import com.sinopec.mmsecurity.entity.FacProductionAreaMetric;
import com.sinopec.mmsecurity.entity.FacProductionAreaZone;
import com.sinopec.mmsecurity.entity.FacProductionDevice;
import com.sinopec.mmsecurity.entity.FacProductionDeviceCategory;
import com.sinopec.mmsecurity.entity.FacProductionFacility;
import com.sinopec.mmsecurity.entity.FacProductionPersonnel;
import com.sinopec.mmsecurity.entity.FacProductionRiskWarning;
import com.sinopec.mmsecurity.entity.FacProductionStat;
import com.sinopec.mmsecurity.mapper.FacProductionAlarmMapper;
import com.sinopec.mmsecurity.mapper.FacProductionAreaMetricMapper;
import com.sinopec.mmsecurity.mapper.FacProductionAreaZoneMapper;
import com.sinopec.mmsecurity.mapper.FacProductionDeviceCategoryMapper;
import com.sinopec.mmsecurity.mapper.FacProductionDeviceMapper;
import com.sinopec.mmsecurity.mapper.FacProductionFacilityMapper;
import com.sinopec.mmsecurity.mapper.FacProductionPersonnelMapper;
import com.sinopec.mmsecurity.mapper.FacProductionRiskWarningMapper;
import com.sinopec.mmsecurity.mapper.FacProductionStatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 生产应急监测大屏（fm-production / fm-production-area）服务。
 *
 * <p>数据来源为 V13 落地的 fac_production_* 真实表，取代前端硬编码的 productionMock /
 * productionAreaMock / productionDeviceMock。风险汇总（riskSummary）由 fac_production_risk_warning
 * 按 level_code 实时聚合，不单独建表；装置区人员构成沿用前端既有的按设施 id 推导规则，
 * 保证同一设施多次打开数值稳定。地图覆盖层与控件为前端静态几何，不在此处理。
 */
@Service
@RequiredArgsConstructor
public class ProductionService {

    /** 设备状态下拉的「全部」选项，前端原样回传时按不过滤处理 */
    private static final String STATUS_ALL = "全部状态";

    /** 人员构成类别与配色（本厂人员 / 承包商 / 访客），沿用 productionAreaMock */
    private static final String[] SLICE_NAMES = {"本厂人员", "承包商", "访客"};
    private static final String[] SLICE_COLORS = {"#3ec6ff", "#f0c429", "#8aa4c4"};

    private final FacProductionFacilityMapper facilityMapper;
    private final FacProductionDeviceCategoryMapper deviceCategoryMapper;
    private final FacProductionStatMapper statMapper;
    private final FacProductionAlarmMapper alarmMapper;
    private final FacProductionRiskWarningMapper riskWarningMapper;
    private final FacProductionPersonnelMapper personnelMapper;
    private final FacProductionDeviceMapper deviceMapper;
    private final FacProductionAreaMetricMapper areaMetricMapper;
    private final FacProductionAreaZoneMapper areaZoneMapper;

    /** 首屏总览：设施卡片 + 设备分类卡片 + 统计概览条 + 风险汇总。 */
    public ProductionOverview overview() {
        ProductionOverview dto = new ProductionOverview();
        dto.setFacilities(facilityMapper.selectList(
                new LambdaQueryWrapper<FacProductionFacility>()
                        .orderByAsc(FacProductionFacility::getSortNo)).stream()
                .map(this::toGridItem).collect(Collectors.toList()));
        dto.setDevices(deviceCategoryMapper.selectList(
                new LambdaQueryWrapper<FacProductionDeviceCategory>()
                        .orderByAsc(FacProductionDeviceCategory::getSortNo)).stream()
                .map(this::toGridItem).collect(Collectors.toList()));
        dto.setStats(statMapper.selectList(
                new LambdaQueryWrapper<FacProductionStat>()
                        .orderByAsc(FacProductionStat::getSortNo)).stream()
                .map(this::toStat).collect(Collectors.toList()));
        dto.setRiskSummary(riskSummary());
        return dto;
    }

    /** 生产报警列表：facilityId 为空返回全部，否则只返回该设施（装置区）的报警。上限 500 行（方言安全的 Page 限流，返回型不变）。 */
    public List<ProductionAlarmItem> alarms(Long facilityId) {
        Page<FacProductionAlarm> page = new Page<>(1, 500, false);
        return alarmMapper.selectPage(page, alarmQuery(facilityId)).getRecords().stream()
                .map(this::toAlarm).collect(Collectors.toList());
    }

    /** 风险预警列表：按红/橙/黄三级着色，取自 fac_production_risk_warning。 */
    public List<RiskWarningItem> riskWarnings() {
        return riskWarningMapper.selectList(
                new LambdaQueryWrapper<FacProductionRiskWarning>()
                        .orderByAsc(FacProductionRiskWarning::getSortNo)).stream()
                .map(this::toRiskWarning).collect(Collectors.toList());
    }

    /** 人员定位标记：left/top 为舞台百分比，经纬度为 WGS84 真实坐标。 */
    public List<PersonnelMarker> personnel() {
        return personnelMapper.selectList(
                new LambdaQueryWrapper<FacProductionPersonnel>()
                        .orderByAsc(FacProductionPersonnel::getSortNo)).stream()
                .map(this::toPersonnel).collect(Collectors.toList());
    }

    /**
     * 装置区二级页聚合详情。
     *
     * @return 设施未命中时返回 null，由 Controller 转 NOT_FOUND 业务异常
     */
    public ProductionAreaDetail areaDetail(Long facilityId) {
        FacProductionFacility facility = facilityId == null ? null : facilityMapper.selectById(facilityId);
        if (facility == null) {
            return null;
        }
        String facilityName = facility.getName();

        ProductionAreaDetail dto = new ProductionAreaDetail();
        dto.setFacilityId(facility.getId());
        dto.setFacilityName(facilityName);
        dto.setZones(areaZoneMapper.selectList(new LambdaQueryWrapper<FacProductionAreaZone>()
                        .eq(FacProductionAreaZone::getFacilityId, facilityId)
                        .orderByAsc(FacProductionAreaZone::getSortNo))
                .stream().map(this::toAreaZone).collect(Collectors.toList()));
        dto.setMetrics(areaMetricMapper.selectList(new LambdaQueryWrapper<FacProductionAreaMetric>()
                        .eq(FacProductionAreaMetric::getFacilityId, facilityId)
                        .orderByAsc(FacProductionAreaMetric::getSortNo))
                .stream().map(this::toAreaMetric).collect(Collectors.toList()));

        List<PersonnelSlice> slices = personnelSlices(facility.getId());
        dto.setPersonnelSlices(slices);
        dto.setPersonnelTotal(slices.stream().mapToInt(PersonnelSlice::getValue).sum());

        // 装置区场景：位置统一改写为「<设施名>区域」，描述中的 A装置 替换为设施名（沿用前端既有行为）
        dto.setAlarms(alarms(facilityId).stream().map(a -> {
            a.setLocation(facilityName + "区域");
            if (a.getDescription() != null) {
                a.setDescription(a.getDescription().replace("A装置", facilityName));
            }
            return a;
        }).collect(Collectors.toList()));
        return dto;
    }

    /**
     * 设备清单分页：category / status 为可选过滤，空串或「全部状态」表示不过滤；
     * page 从 1 起，size 默认 10。数据量固定（35 台），在内存中切片，避免逐页回库。
     */
    public ProductionDevicePage devices(String category, String status, Integer page, Integer size) {
        int pageNo = page == null || page < 1 ? 1 : page;
        int pageSize = size == null || size < 1 ? 10 : size;

        String categoryFilter = blankToNull(category);
        String statusFilter = blankToNull(status);
        if (STATUS_ALL.equals(statusFilter)) {
            statusFilter = null;
        }

        LambdaQueryWrapper<FacProductionDevice> qw = new LambdaQueryWrapper<>();
        if (categoryFilter != null) qw.eq(FacProductionDevice::getCategory, categoryFilter);
        if (statusFilter != null) qw.eq(FacProductionDevice::getStatusName, statusFilter);
        qw.orderByAsc(FacProductionDevice::getSortNo);

        List<FacProductionDevice> rows = deviceMapper.selectList(qw);
        int from = Math.min((pageNo - 1) * pageSize, rows.size());
        int to = Math.min(from + pageSize, rows.size());

        ProductionDevicePage dto = new ProductionDevicePage();
        dto.setPage(pageNo);
        dto.setSize(pageSize);
        dto.setTotal((long) rows.size());
        dto.setItems(rows.subList(from, to).stream()
                .map(this::toDevice).collect(Collectors.toList()));
        return dto;
    }

    /** 风险汇总：按 level_code 聚合 red / orange / yellow 计数。 */
    private RiskSummary riskSummary() {
        List<FacProductionRiskWarning> rows = riskWarningMapper.selectList(null);
        RiskSummary summary = new RiskSummary();
        int red = 0;
        int orange = 0;
        int yellow = 0;
        for (FacProductionRiskWarning r : rows) {
            String level = r.getLevelCode();
            if ("red".equals(level)) red++;
            else if ("orange".equals(level)) orange++;
            else if ("yellow".equals(level)) yellow++;
        }
        summary.setRed(red);
        summary.setOrange(orange);
        summary.setYellow(yellow);
        return summary;
    }

    /** 人员构成：沿用 productionAreaMock 的按设施 id 推导规则，保证数值稳定（本厂人员/承包商/访客）。 */
    private List<PersonnelSlice> personnelSlices(Long facilityId) {
        long seed = facilityId == null ? 0 : facilityId;
        int employee = 20 + (int) (seed % 8);
        int contractor = 8 + (int) (seed % 5);
        int visitor = 3 + (int) (seed % 4);
        int[] values = {employee, contractor, visitor};

        List<PersonnelSlice> slices = new ArrayList<>();
        for (int i = 0; i < SLICE_NAMES.length; i++) {
            PersonnelSlice slice = new PersonnelSlice();
            slice.setName(SLICE_NAMES[i]);
            slice.setValue(values[i]);
            slice.setColor(SLICE_COLORS[i]);
            slices.add(slice);
        }
        return slices;
    }

    private LambdaQueryWrapper<FacProductionAlarm> alarmQuery(Long facilityId) {
        LambdaQueryWrapper<FacProductionAlarm> qw = new LambdaQueryWrapper<>();
        if (facilityId != null) qw.eq(FacProductionAlarm::getFacilityId, facilityId);
        return qw.orderByAsc(FacProductionAlarm::getSortNo);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private OverviewGridItem toGridItem(FacProductionFacility e) {
        OverviewGridItem d = new OverviewGridItem();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setCount(e.getItemCount());
        d.setImage(e.getImage());
        return d;
    }

    private OverviewGridItem toGridItem(FacProductionDeviceCategory e) {
        OverviewGridItem d = new OverviewGridItem();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setCount(e.getItemCount());
        d.setImage(e.getImage());
        return d;
    }

    private StatOverviewItem toStat(FacProductionStat e) {
        StatOverviewItem d = new StatOverviewItem();
        d.setId(e.getId());
        d.setLabel(e.getLabel());
        d.setValue(e.getValueText());
        d.setUnit(e.getUnit());
        d.setTrend(e.getTrend());
        d.setTrendUp(e.getTrendUp());
        d.setIconIndex(e.getIconIndex());
        return d;
    }

    private ProductionAlarmItem toAlarm(FacProductionAlarm e) {
        ProductionAlarmItem d = new ProductionAlarmItem();
        d.setId(e.getId());
        d.setTitle(e.getTitle());
        d.setTitleColor(e.getTitleColor());
        d.setLocation(e.getLocation());
        d.setTime(e.getOccurredAt());
        d.setDescription(e.getDescription());
        d.setStatus(e.getStatusName());
        d.setIconIndex(e.getIconIndex());
        d.setThumb(e.getThumb());
        return d;
    }

    private RiskWarningItem toRiskWarning(FacProductionRiskWarning e) {
        RiskWarningItem d = new RiskWarningItem();
        d.setId(e.getId());
        d.setLocation(e.getLocation());
        d.setType(e.getTypeName());
        d.setTime(e.getOccurredAt());
        d.setPerson(e.getPerson());
        d.setPhone(e.getPhone());
        d.setLevel(e.getLevelCode());
        d.setLevelLabel(e.getLevelLabel());
        return d;
    }

    private PersonnelMarker toPersonnel(FacProductionPersonnel e) {
        PersonnelMarker d = new PersonnelMarker();
        d.setId(e.getId());
        d.setLeft(e.getLeftRatio());
        d.setTop(e.getTopRatio());
        d.setLongitude(e.getLongitude());
        d.setLatitude(e.getLatitude());
        d.setLocation(e.getLocation());
        d.setCount(e.getPersonCount());
        d.setMarkerIcon(e.getMarkerIcon());
        d.setPopupBg(e.getPopupBg());
        d.setMarkerDot(e.getMarkerDot());
        d.setMarkerLine(e.getMarkerLine());
        return d;
    }

    private ProductionDeviceItem toDevice(FacProductionDevice e) {
        ProductionDeviceItem d = new ProductionDeviceItem();
        d.setId(e.getId());
        d.setName(e.getName());
        d.setType(e.getTypeName());
        d.setCategory(e.getCategory());
        d.setArea(e.getArea());
        d.setStatus(e.getStatusName());
        d.setLongitude(e.getLongitude());
        d.setLatitude(e.getLatitude());
        return d;
    }

    private ProductionAreaZone toAreaZone(FacProductionAreaZone e) {
        ProductionAreaZone d = new ProductionAreaZone();
        d.setId(e.getZoneCode());
        d.setName(e.getName());
        d.setAlarmCount(e.getAlarmCount());
        d.setZoneIndex(e.getZoneIndex());
        return d;
    }

    private ProductionAreaMetric toAreaMetric(FacProductionAreaMetric e) {
        ProductionAreaMetric d = new ProductionAreaMetric();
        d.setId(e.getId());
        d.setLabel(e.getLabel());
        d.setValue(e.getValueText());
        return d;
    }
}
