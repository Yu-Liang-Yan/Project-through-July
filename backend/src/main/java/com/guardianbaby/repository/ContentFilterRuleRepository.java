package com.guardianbaby.repository;

import com.guardianbaby.entity.ContentFilterRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContentFilterRuleRepository extends JpaRepository<ContentFilterRule, Long> {

    List<ContentFilterRule> findByUserId(Long userId);

    List<ContentFilterRule> findByUserIdAndCategory(Long userId, ContentFilterRule.FilterCategory category);

    List<ContentFilterRule> findByUserIdAndEnabledTrue(Long userId);
}
