package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/** 流程节点处置动作项，与前端 {@code ProcessAction} 对齐（契约 #/ProcessAction）。 */
@Data
public class ProcessAction implements Serializable {

    /** 动作 id */
    private String id;

    /** 动作文案 */
    private String label;

    /** 是否已完成 */
    private Boolean done;

    /** 动作强调色：primary / danger / warning；无则不着色 */
    private String type;
}
