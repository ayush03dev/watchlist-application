package com.jiostar.watchlist.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItemEntity, WatchlistItemId> {

	long countByIdUserId(int userId);

	boolean existsByIdUserIdAndIdContentId(int userId, String contentId);

	Page<WatchlistItemEntity> findByIdUserIdOrderByAddedAtDesc(int userId, Pageable pageable);

	void deleteByIdUserIdAndIdContentId(int userId, String contentId);

}
