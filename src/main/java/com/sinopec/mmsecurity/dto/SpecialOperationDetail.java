package com.sinopec.mmsecurity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/** 特殊作业 - 作业票详情（列表行 + 现场视频/气体检测点/作业人员子表）。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpecialOperationDetail extends SpecialOperationItem {
    private List<SpecialOperationVideoItem> videos;
    private List<SpecialOperationGasPoint> gasPoints;
    private List<SpecialOperationPersonItem> personnel;
}
