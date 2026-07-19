package com.guardianbaby.service;

import com.guardianbaby.dto.AlertResponse;
import java.util.List;

public interface AlertService {
    AlertResponse create(Long protectedUserId, String type, String severity, String title, String message);
    List<AlertResponse> listForGuardian(Long guardianId);
    List<AlertResponse> listForProtectedUser(Long protectedUserId);
    AlertResponse markRead(Long alertId);
    AlertResponse resolve(Long alertId);
    long unreadCount(Long guardianId);
}
