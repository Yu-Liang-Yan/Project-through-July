package com.guardianbaby.dto;

import com.guardianbaby.entity.ContentFilterRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterRuleResponse {
    private Long id;
    private Long userId;
    private String category;
    private String pattern;
    private String action;
    private Integer priority;
    private Boolean enabled;
    private LocalDateTime createdAt;

    public static FilterRuleResponse fromEntity(ContentFilterRule r) {
        return FilterRuleResponse.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .category(r.getCategory().name())
                .pattern(r.getPattern())
                .action(r.getAction().name())
                .priority(r.getPriority())
                .enabled(r.getEnabled())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
