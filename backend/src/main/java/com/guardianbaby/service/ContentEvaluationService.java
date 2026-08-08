package com.guardianbaby.service;

import java.util.List;
import java.util.Map;

public interface ContentEvaluationService {
    record EvalResult(String action, String matchedRule, String matchedCategory, boolean blocked) {}
    EvalResult evaluate(Long userId, String content);
}
