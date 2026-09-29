package com.jiostar.watchlist.api.dto;

import java.time.Instant;

import com.jiostar.watchlist.domain.WatchlistItem;

public record WatchlistItemResponse(String contentId, Instant addedAt) {

	public static WatchlistItemResponse from(WatchlistItem item) {
		return new WatchlistItemResponse(item.contentId(), item.addedAt());
	}

}
