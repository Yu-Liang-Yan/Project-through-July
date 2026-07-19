package com.guardianbaby.service.impl;

import com.guardianbaby.common.exception.BusinessException;
import com.guardianbaby.dto.FilterRuleResponse;
import com.guardianbaby.entity.ContentFilterRule;
import com.guardianbaby.entity.ContentFilterRule.FilterAction;
import com.guardianbaby.entity.ContentFilterRule.FilterCategory;
import com.guardianbaby.entity.User;
import com.guardianbaby.repository.ContentFilterRuleRepository;
import com.guardianbaby.repository.UserRepository;
import com.guardianbaby.service.FilterRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilterRuleServiceImpl implements FilterRuleService {

    private final ContentFilterRuleRepository filterRuleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FilterRuleResponse add(Long userId, String category, String pattern, String action, Integer priority) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        ContentFilterRule rule = ContentFilterRule.builder()
                .user(user)
                .category(FilterCategory.valueOf(category.toUpperCase()))
                .pattern(pattern)
                .action(FilterAction.valueOf(action.toUpperCase()))
                .priority(priority != null ? priority : 1)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
        return FilterRuleResponse.fromEntity(filterRuleRepository.save(rule));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FilterRuleResponse> listByUser(Long userId) {
        return filterRuleRepository.findByUserId(userId).stream()
                .map(FilterRuleResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FilterRuleResponse> listByUserAndCategory(Long userId, String category) {
        return filterRuleRepository.findByUserIdAndCategory(userId, FilterCategory.valueOf(category.toUpperCase())).stream()
                .map(FilterRuleResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long ruleId) {
        if (!filterRuleRepository.existsById(ruleId)) {
            throw new BusinessException("过滤规则不存在");
        }
        filterRuleRepository.deleteById(ruleId);
    }
}
