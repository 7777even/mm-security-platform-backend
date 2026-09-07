package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 应急生产安全知识列表，与前端 {@code emergency.openapi.json#/KnowledgeList} 对齐。
 * 属应急资源静态参考配置。
 */
@Data
public class KnowledgeList implements Serializable {

    /** 知识条目列表 */
    private List<KnowledgeItem> items;
}
