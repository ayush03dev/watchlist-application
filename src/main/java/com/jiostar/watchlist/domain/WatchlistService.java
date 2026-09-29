package com.jiostar.watchlist.domain;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jiostar.watchlist.config.WatchlistProperties;
import com.jiostar.watchlist.exception.WatchlistFullException;
import com.jiostar.watchlist.exception.WatchlistUserNotAllowedException;
import com.jiostar.watchlist.persistence.WatchlistItemEntity;
import com.jiostar.watchlist.persistence.WatchlistItemId;
import com.jiostar.watchlist.persistence.WatchlistItemRepository;

@Service
public class WatchlistService {

	private static final Logger log = LoggerFactory.getLogger(WatchlistService.class);

	private final WatchlistItemRepository repository;
	private final WatchlistProperties properties;

	public WatchlistService(WatchlistItemRepository repository, WatchlistProperties properties) {
		this.repository = repository;
		this.properties = properties;
	}

	@Transactional
	public AddWatchlistItemResult addOrBump(int userId, String contentId) {
		validateUserId(userId);

		WatchlistItemId id = new WatchlistItemId(userId, contentId);
		Instant now = Instant.now();

		return repository.findById(id).map(existing -> {
			existing.setAddedAt(now);
			repository.save(existing);
			log.info("Bumped watchlist item userId={} contentId={}", userId, contentId);
			return new AddWatchlistItemResult(userId, contentId, now, true);
		}).orElseGet(() -> {
			if (repository.countByIdUserId(userId) >= properties.getMaxSize()) {
				log.warn("Watchlist full for userId={} maxSize={}", userId, properties.getMaxSize());
				throw new WatchlistFullException(properties.getMaxSize());
			}
			repository.save(new WatchlistItemEntity(id, now));
			log.info("Added watchlist item userId={} contentId={}", userId, contentId);
			return new AddWatchlistItemResult(userId, contentId, now, false);
		});
	}

	@Transactional
	public void remove(int userId, String contentId) {
		validateUserId(userId);
		repository.deleteByIdUserIdAndIdContentId(userId, contentId);
		log.info("Removed watchlist item userId={} contentId={}", userId, contentId);
	}

	@Transactional(readOnly = true)
	public WatchlistPage list(int userId, int page, int size) {
		validateUserId(userId);
		if (page < 0) {
			throw new IllegalArgumentException("page must be >= 0");
		}
		if (size < 1) {
			throw new IllegalArgumentException("size must be >= 1");
		}

		int effectiveSize = Math.min(size, properties.getMaxPageSize());
		Page<WatchlistItemEntity> result = repository.findByIdUserIdOrderByAddedAtDesc(userId,
				PageRequest.of(page, effectiveSize));

		List<WatchlistItem> items = result.getContent().stream()
				.map(e -> new WatchlistItem(e.getId().getContentId(), e.getAddedAt()))
				.toList();

		log.debug("Listed watchlist userId={} page={} size={} totalItems={}", userId, page, effectiveSize,
				result.getTotalElements());

		return new WatchlistPage(userId, page, effectiveSize, result.getTotalElements(), result.getTotalPages(), items);
	}

	private void validateUserId(int userId) {
		if (!properties.isAllowedUserId(userId)) {
			throw new WatchlistUserNotAllowedException(userId);
		}
	}

}
