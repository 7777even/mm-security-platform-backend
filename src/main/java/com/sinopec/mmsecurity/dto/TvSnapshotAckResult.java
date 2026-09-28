package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 录像截图确认结果。 */
@Data
public class TvSnapshotAckResult {

    private Long id;
    /** ACKED 已确认（PENDING→ACKED）。 */
    private String reviewStatus;
}
