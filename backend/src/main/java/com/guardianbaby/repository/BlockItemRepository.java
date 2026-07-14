package com.guardianbaby.repository;

import com.guardianbaby.entity.BlockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockItemRepository extends JpaRepository<BlockItem, Long> {

    List<BlockItem> findByUserIdAndBlockType(Long userId, BlockItem.BlockType blockType);
}
