package com.guardianbaby.controller;

import com.guardianbaby.dto.ApiResponse;
import com.guardianbaby.dto.BlockItemResponse;
import com.guardianbaby.service.BlockListService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/block-list")
@RequiredArgsConstructor
public class BlockListController {

    private final BlockListService blockListService;

    @GetMapping
    public ApiResponse<List<BlockItemResponse>> list(@RequestParam Long userId,
                                                      @RequestParam String type) {
        return ApiResponse.ok(blockListService.listByType(userId, type));
    }

    @PostMapping
    public ApiResponse<BlockItemResponse> add(@RequestParam Long userId,
                                               @RequestBody Map<String, String> body) {
        BlockItemResponse item = blockListService.addItem(
                userId, body.get("type"), body.get("name"));
        return ApiResponse.ok("已添加到禁止列表", item);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> remove(@PathVariable Long id,
                                     @RequestParam Long userId) {
        blockListService.removeItem(id, userId);
        return ApiResponse.ok("已从禁止列表移除", null);
    }
}
