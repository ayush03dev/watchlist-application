package com.jiostar.watchlist.api.dto;

import java.time.Instant;

import com.jiostar.watchlist.domain.AddWatchlistItemResult;

public record AddWatchlistItemResponse(int userId, String contentId, Instant addedAt, boolean bumped) {

	public static AddWatchlistItemResponse from(AddWatchlistItemResult result) {
		return new AddWatchlistItemResponse(result.userId(), result.contentId(), result.addedAt(), result.bumped());
	}

}
