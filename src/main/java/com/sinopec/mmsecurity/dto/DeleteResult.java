package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除结果，与前端 {@code alarm.openapi.json#/DeleteResult} 对齐。
 */
@Data
public class DeleteResult implements Serializable {

    /** 删除是否成功（逻辑删除命中行数 > 0 为 true） */
    private Boolean ok;
}
