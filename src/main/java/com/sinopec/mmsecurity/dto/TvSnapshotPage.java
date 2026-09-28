package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/** 录像截图分页列表。 */
@Data
public class TvSnapshotPage {

    private long total;
    private int page;
    private int size;
    private int pages;
    private List<TvSnapshotItem> list;
}
