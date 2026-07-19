package com.guardianbaby.service;

import com.guardianbaby.dto.ApprovalRequestCreateDto;
import com.guardianbaby.dto.ApprovalRequestResponse;

import java.util.List;

public interface ApprovalRequestService {

    /** 被保护用户发起请求 */
    ApprovalRequestResponse submit(ApprovalRequestCreateDto dto);

    /** 监护人查看所有待审批请求 */
    List<ApprovalRequestResponse> listPending(Long guardianId);

    /** 被保护用户查看自己发起的请求历史 */
    List<ApprovalRequestResponse> listMyRequests(Long requesterId);

    /** 监护人同意请求 */
    ApprovalRequestResponse approve(Long requestId, Long reviewerId, String message);

    /** 监护人拒绝请求 */
    ApprovalRequestResponse reject(Long requestId, Long reviewerId, String reason);

    /** 待审批数量 */
    long pendingCount();
}
