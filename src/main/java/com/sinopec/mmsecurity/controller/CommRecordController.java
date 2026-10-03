package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.CommRecordWriteRequest;
import com.sinopec.mmsecurity.dto.CommunicationRecord;
import com.sinopec.mmsecurity.dto.CommunicationRecordList;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.CommRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /** 新建通讯通知记录（id 由服务端分配，recordNo 为业务自然键）。 */
    @PostMapping("/records")
    @RequireAuth(role = "ADMIN")
    public Result<CommunicationRecord> createRecord(@Valid @RequestBody CommRecordWriteRequest payload) {
        return Result.ok(commRecordService.createRecord(payload));
    }

    /** 更新通讯通知记录（按 recordNo）；未命中时 data 为 null。 */
    @PutMapping("/records/{recordNo}")
    @RequireAuth(role = "ADMIN")
    public Result<CommunicationRecord> updateRecord(@PathVariable String recordNo,
                                                    @Valid @RequestBody CommRecordWriteRequest payload) {
        return Result.ok(commRecordService.updateRecord(recordNo, payload));
    }

    /** 删除通讯通知记录（按 recordNo）；未命中 ok=false。 */
    @DeleteMapping("/records/{recordNo}")
    @RequireAuth(role = "ADMIN")
    public Result<DeleteResult> deleteRecord(@PathVariable String recordNo) {
        return Result.ok(commRecordService.deleteRecord(recordNo));
    }
}
