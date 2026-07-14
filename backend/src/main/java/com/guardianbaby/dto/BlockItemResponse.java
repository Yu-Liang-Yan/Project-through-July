package com.guardianbaby.dto;

import com.guardianbaby.entity.BlockItem;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BlockItemResponse {

    private Long id;
    private String type;
    private String name;
    private LocalDateTime createdAt;

    public static BlockItemResponse fromEntity(BlockItem item) {
        return BlockItemResponse.builder()
                .id(item.getId())
                .type(item.getBlockType().name().toLowerCase())
                .name(item.getName())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
