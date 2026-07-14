package com.guardianbaby.service.impl;

import com.guardianbaby.common.exception.BusinessException;
import com.guardianbaby.dto.BlockItemResponse;
import com.guardianbaby.entity.BlockItem;
import com.guardianbaby.entity.User;
import com.guardianbaby.repository.BlockItemRepository;
import com.guardianbaby.repository.UserRepository;
import com.guardianbaby.service.BlockListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockListServiceImpl implements BlockListService {

    private final BlockItemRepository blockItemRepository;
    private final UserRepository userRepository;

    @Override
    public List<BlockItemResponse> listByType(Long userId, String type) {
        BlockItem.BlockType blockType = BlockItem.BlockType.valueOf(type.toUpperCase());
        return blockItemRepository.findByUserIdAndBlockType(userId, blockType).stream()
                .map(BlockItemResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public BlockItemResponse addItem(Long userId, String type, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        BlockItem.BlockType blockType = BlockItem.BlockType.valueOf(type.toUpperCase());

        BlockItem item = BlockItem.builder()
                .blockType(blockType)
                .name(name)
                .user(user)
                .build();

        blockItemRepository.save(item);
        return BlockItemResponse.fromEntity(item);
    }

    @Override
    @Transactional
    public void removeItem(Long itemId, Long userId) {
        BlockItem item = blockItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException("禁止项不存在"));

        if (!item.getUser().getId().equals(userId)) {
            throw new BusinessException("无权操作该禁止项");
        }

        blockItemRepository.delete(item);
    }
}
