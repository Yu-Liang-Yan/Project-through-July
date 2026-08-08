package com.guardianbaby.service.impl;

import com.guardianbaby.entity.ContentFilterRule;
import com.guardianbaby.entity.ContentFilterRule.FilterAction;
import com.guardianbaby.entity.ContentFilterRule.FilterCategory;
import com.guardianbaby.repository.ContentFilterRuleRepository;
import com.guardianbaby.service.ContentEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentEvaluationServiceImpl implements ContentEvaluationService {

    private final ContentFilterRuleRepository repository;

    @Override
    public EvalResult evaluate(Long userId, String content) {
        if (content == null || content.isBlank()) {
            return new EvalResult("ALLOW", null, null, false);
        }

        List<ContentFilterRule> rules = repository.findByUserIdAndEnabledTrue(userId);
        // 按优先级降序排列（高优先级先匹配）
        rules.sort(Comparator.comparingInt(ContentFilterRule::getPriority).reversed());

        String lowerContent = content.toLowerCase().trim();

        for (ContentFilterRule rule : rules) {
            String pattern = rule.getPattern().toLowerCase().trim();
            FilterCategory category = rule.getCategory();

            boolean matched = switch (category) {
                case KEYWORD -> lowerContent.contains(pattern);
                case WEBSITE, DOMAIN -> {
                    String host = extractHost(lowerContent);
                    yield host.endsWith(pattern) || host.equals(pattern);
                }
                case APP -> lowerContent.equals(pattern);
            };

            if (matched) {
                return new EvalResult(
                    rule.getAction().name(),
                    rule.getPattern(),
                    category.name(),
                    rule.getAction() == FilterAction.BLOCK
                );
            }
        }

        return new EvalResult("ALLOW", null, null, false);
    }

    private String extractHost(String content) {
        // 从 URL 或纯文本中提取域名
        String s = content.replaceAll("^https?://", "")
                          .replaceAll("^www\\.", "")
                          .replaceAll("/.*$", "");
        return s;
    }
}
