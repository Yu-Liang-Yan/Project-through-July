package com.guardianbaby.service;

import com.guardianbaby.dto.BlockItemResponse;

import java.util.List;

public interface BlockListService {

    List<BlockItemResponse> listByType(Long userId, String type);

    BlockItemResponse addItem(Long userId, String type, String name);

    void removeItem(Long itemId, Long userId);
}
