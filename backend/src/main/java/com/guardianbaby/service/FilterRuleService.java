package com.guardianbaby.service;

import com.guardianbaby.dto.FilterRuleResponse;
import java.util.List;

public interface FilterRuleService {
    FilterRuleResponse add(Long userId, String category, String pattern, String action, Integer priority);
    List<FilterRuleResponse> listByUser(Long userId);
    List<FilterRuleResponse> listByUserAndCategory(Long userId, String category);
    void delete(Long ruleId);
}
