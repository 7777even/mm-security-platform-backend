package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 录像截图采集入库结果。 */
@Data
public class TvSnapshotIngestResult {

    private Long id;
    private String monitorCode;
    private String captureTime;
    /** PENDING 待确认（落库默认态）。 */
    private String reviewStatus;
    private String createdAt;
}
