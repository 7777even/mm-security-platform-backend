package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 应急辅助信息统计集合（GET /api/v1/emergency/assist-stats）。 */
@Data
public class EmergencyAssistStatSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<EmergencyAssistStat> items;
}
