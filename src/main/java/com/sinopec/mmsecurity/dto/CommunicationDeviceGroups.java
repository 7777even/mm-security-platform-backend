package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/**
 * 通讯设备 - 三个页签的分组集合，对应前端 mock 的 Record&lt;CommunicationTab, CommunicationGroup[]&gt;。
 * 一次返回广播/电话/对讲三组，前端按页签切换展示，无需二次请求。
 */
@Data
public class CommunicationDeviceGroups {
    private List<CommunicationGroup> broadcast;
    private List<CommunicationGroup> phone;
    private List<CommunicationGroup> intercom;
}
