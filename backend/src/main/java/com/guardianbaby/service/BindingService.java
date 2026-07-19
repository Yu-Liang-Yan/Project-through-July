package com.guardianbaby.service;

import com.guardianbaby.dto.BindingResponse;
import java.util.List;

public interface BindingService {
    BindingResponse bind(Long guardianId, Long protectedUserId);
    void unbind(Long bindingId);
    List<BindingResponse> listProtectedUsers(Long guardianId);
    List<BindingResponse> listGuardians(Long protectedUserId);
}
