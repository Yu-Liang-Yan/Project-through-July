package com.guardianbaby.controller;

import com.guardianbaby.dto.ApiResponse;
import com.guardianbaby.dto.ApprovalRequestCreateDto;
import com.guardianbaby.dto.ApprovalRequestResponse;
import com.guardianbaby.service.ApprovalRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalRequestController {

    private final ApprovalRequestService approvalRequestService;

    /** 被保护用户发起请求 */
    @PostMapping
    public ApiResponse<ApprovalRequestResponse> submit(@RequestBody ApprovalRequestCreateDto dto) {
        return ApiResponse.ok("请求已提交，等待监护人审批", approvalRequestService.submit(dto));
    }

    /** 监护人查看待审批列表 */
    @GetMapping("/pending")
    public ApiResponse<List<ApprovalRequestResponse>> pending(@RequestParam Long guardianId) {
        return ApiResponse.ok(approvalRequestService.listPending(guardianId));
    }

    /** 被保护用户查看自己的请求历史 */
    @GetMapping("/my")
    public ApiResponse<List<ApprovalRequestResponse>> myRequests(@RequestParam Long requesterId) {
        return ApiResponse.ok(approvalRequestService.listMyRequests(requesterId));
    }

    /** 监护人同意 */
    @PutMapping("/{id}/approve")
    public ApiResponse<ApprovalRequestResponse> approve(@PathVariable Long id,
                                                         @RequestParam Long reviewerId,
                                                         @RequestBody(required = false) Map<String, String> body) {
        String msg = body != null ? body.getOrDefault("message", "已同意") : "已同意";
        return ApiResponse.ok("已同意请求", approvalRequestService.approve(id, reviewerId, msg));
    }

    /** 监护人拒绝 */
    @PutMapping("/{id}/reject")
    public ApiResponse<ApprovalRequestResponse> reject(@PathVariable Long id,
                                                        @RequestParam Long reviewerId,
                                                        @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "已拒绝") : "已拒绝";
        return ApiResponse.ok("已拒绝请求", approvalRequestService.reject(id, reviewerId, reason));
    }

    /** 待审批数量 */
    @GetMapping("/pending-count")
    public ApiResponse<Long> pendingCount() {
        return ApiResponse.ok(approvalRequestService.pendingCount());
    }
}
