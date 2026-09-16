package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.CommunicationRecordList;
import com.sinopec.mmsecurity.service.CommRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通讯通知记录（短信 / 电话通话 / 广播播报 / APP推送 / 语音对讲）只读接口，数据源为 V57 fac_comm_record 真实表。
 *
 * <p>与 {@link CommDeviceController} 共用 /api/v1/communication 前缀：设备侧提供 devices，
 * 记录侧提供 records，两者互不重叠。
 */
@RestController
@RequestMapping("/api/v1/communication")
@RequiredArgsConstructor
public class CommRecordController {

    private final CommRecordService commRecordService;

    /**
     * 通讯通知记录列表。
     *
     * <p>type 缺省时返回全部五类记录；传入 sms/call/broadcast/push/intercom 时仅返回该类型。
     */
    @GetMapping("/records")
    public Result<CommunicationRecordList> records(
            @RequestParam(value = "type", required = false) String type) {
        return Result.ok(commRecordService.list(type));
    }
}
