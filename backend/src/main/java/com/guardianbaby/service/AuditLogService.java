package com.guardianbaby.service;

import com.guardianbaby.dto.AuditLogResponse;

import java.util.List;

public interface AuditLogService {

    void log(Long userId, String action, String detail, String ip);

    List<AuditLogResponse> listByUser(Long userId);

    List<AuditLogResponse> listAll();
}
