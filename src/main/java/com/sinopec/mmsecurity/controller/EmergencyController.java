package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.Result;
import com.sinopec.mmsecurity.dto.ClosedCaseList;
import com.sinopec.mmsecurity.dto.DutyRoster;
import com.sinopec.mmsecurity.dto.EmergencyPhoneBook;
import com.sinopec.mmsecurity.dto.EmergencyStrength;
import com.sinopec.mmsecurity.dto.CommandActionDetail;
import com.sinopec.mmsecurity.dto.EmergencyCommandGroup;
import com.sinopec.mmsecurity.dto.KnowledgeList;
import com.sinopec.mmsecurity.dto.NodePhaseConfig;
import com.sinopec.mmsecurity.security.RequireAuth;
import com.sinopec.mmsecurity.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @GetMapping("/commands")
    public Result<List<EmergencyCommandGroup>> commands(
            @RequestParam(required = false) String tab) {
        return Result.ok(emergencyService.commandGroups(tab));
    }

    @GetMapping("/commands/{commandId}")
    public Result<CommandActionDetail> commandDetail(@PathVariable String commandId) {
        return Result.ok(emergencyService.commandDetail(commandId));
    }

    /** 应急流程节点联动配置列表（节点切换时的镜头锚点 / 面板显隐 / 值班排班）。 */
    @GetMapping("/process/node-configs")
    public Result<List<NodePhaseConfig>> nodePhaseConfigs() {
        return Result.ok(emergencyService.nodePhaseConfigs());
    }

    /** 保存流程节点联动配置（按 nodeId 整体 upsert），返回落库后的全量列表。 */
    @PutMapping("/process/node-configs")
    @RequireAuth(role = "ADMIN")
    public Result<List<NodePhaseConfig>> saveNodePhaseConfigs(
            @RequestBody List<NodePhaseConfig> payload) {
        return Result.ok(emergencyService.saveNodePhaseConfigs(payload));
    }
}
