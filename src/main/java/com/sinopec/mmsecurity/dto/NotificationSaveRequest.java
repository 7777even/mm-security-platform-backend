package com.sinopec.mmsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/** 新增系统通知入参（POST /notifications，ADMIN 广播或定向推送）。 */
@Data
public class NotificationSaveRequest implements Serializable {
    /** 消息分类：alarm / event / task / system */
    @NotBlank(message = "消息分类不能为空")
    @Size(max = 16, message = "消息分类长度不能超过 16")
    private String category;

    /** 标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过 200")
    private String title;

    /** 摘要 */
    @Size(max = 500, message = "摘要长度不能超过 500")
    private String summary;

    /** 业务对象类型（可选） */
    @Size(max = 16, message = "业务类型长度不能超过 16")
    private String targetType;

    /** 业务对象 ID（可选） */
    @Size(max = 64, message = "业务对象 ID 长度不能超过 64")
    private String targetId;

    /** 接收人（null = 全员广播） */
    @Size(max = 64, message = "接收人长度不能超过 64")
    private String recipient;
}
