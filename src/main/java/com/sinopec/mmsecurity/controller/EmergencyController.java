package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.ClosedCaseList;
import com.sinopec.mmsecurity.dto.DutyRoster;
import com.sinopec.mmsecurity.dto.EmergencyPhoneBook;
import com.sinopec.mmsecurity.dto.EmergencyStrength;
import com.sinopec.mmsecurity.dto.KnowledgeList;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/emergency")
@RequireAuth
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;

    @GetMapping("/strength")
    public Result<EmergencyStrength> strength() {
        return Result.ok(emergencyService.strength());
    }

    @GetMapping("/closed-cases")
    public Result<ClosedCaseList> closedCases() {
        return Result.ok(emergencyService.closedCases());
    }

    @GetMapping("/duty")
    public Result<DutyRoster> duty() {
        return Result.ok(emergencyService.duty());
    }

    @GetMapping("/phones")
    public Result<EmergencyPhoneBook> phones() {
        return Result.ok(emergencyService.phones());
    }

    @GetMapping("/knowledge")
    public Result<KnowledgeList> knowledge() {
        return Result.ok(emergencyService.knowledge());
    }
}
