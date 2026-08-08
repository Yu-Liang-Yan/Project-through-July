package com.guardianbaby.controller;

import com.guardianbaby.dto.*;
import com.guardianbaby.service.ContentEvaluationService;
import com.guardianbaby.service.FilterRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/filters")
@RequiredArgsConstructor
public class FilterRuleController {

    private final FilterRuleService filterRuleService;
    private final ContentEvaluationService evaluationService;

    @PostMapping
    public ApiResponse<FilterRuleResponse> add(@RequestBody Map<String, String> body) {
        return ApiResponse.ok("过滤规则已添加", filterRuleService.add(
                Long.valueOf(body.get("userId")),
                body.get("category"),
                body.get("pattern"),
                body.get("action"),
                body.containsKey("priority") ? Integer.valueOf(body.get("priority")) : 1
        ));
    }

    @GetMapping
    public ApiResponse<List<FilterRuleResponse>> list(@RequestParam Long userId,
                                                       @RequestParam(required = false) String category) {
        if (category != null) {
            return ApiResponse.ok(filterRuleService.listByUserAndCategory(userId, category));
        }
        return ApiResponse.ok(filterRuleService.listByUser(userId));
    }

    @PostMapping("/evaluate")
    public ApiResponse<Map<String, Object>> evaluate(@RequestBody Map<String, Object> body) {
        Long userId = body.get("userId") instanceof Number
                ? ((Number) body.get("userId")).longValue() : null;
        String content = (String) body.get("content");

        if (userId == null || content == null) {
            return ApiResponse.fail("请提供 userId 和 content");
        }

        var result = evaluationService.evaluate(userId, content);

        return ApiResponse.ok("评估完成", Map.of(
            "action", result.action(),
            "matchedRule", result.matchedRule() != null ? result.matchedRule() : "",
            "matchedCategory", result.matchedCategory() != null ? result.matchedCategory() : "",
            "blocked", result.blocked()
        ));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        filterRuleService.delete(id);
        return ApiResponse.ok("过滤规则已删除", null);
    }
}
