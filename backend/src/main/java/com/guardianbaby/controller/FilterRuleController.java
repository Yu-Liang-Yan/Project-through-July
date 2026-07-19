package com.guardianbaby.controller;

import com.guardianbaby.dto.*;
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

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        filterRuleService.delete(id);
        return ApiResponse.ok("过滤规则已删除", null);
    }
}
