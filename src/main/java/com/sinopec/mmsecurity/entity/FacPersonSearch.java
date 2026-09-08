package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 人员识别检索实体。数据来自真实表 fac_person_search；keyword 检索在服务端按姓名/卡口/状态过滤。
 */
@Data
@TableName("fac_person_search")
public class FacPersonSearch {
    private Long id;
    private String name;
    private String gate;
    private String status;
    private String date;
}
