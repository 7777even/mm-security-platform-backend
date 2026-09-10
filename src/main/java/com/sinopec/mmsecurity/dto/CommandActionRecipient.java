package com.sinopec.mmsecurity.dto;

import lombok.Data;

/** 指令派发对象（通讯录/值班人员），契约源：前端 CommandActionRecipient。 */
@Data
public class CommandActionRecipient {

    private String id;
    private String role;
    private String name;
    private String phone;
}
