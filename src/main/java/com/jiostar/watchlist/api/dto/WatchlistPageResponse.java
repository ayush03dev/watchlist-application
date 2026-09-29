package com.jiostar.watchlist.api.dto;

import java.util.List;

import com.jiostar.watchlist.domain.WatchlistPage;

public record WatchlistPageResponse(int userId, int page, int size, long totalItems, int totalPages,
		List<WatchlistItemResponse> items) {

	public static WatchlistPageResponse from(WatchlistPage page) {
		List<WatchlistItemResponse> items = page.items().stream().map(WatchlistItemResponse::from).toList();
		return new WatchlistPageResponse(page.userId(), page.page(), page.size(), page.totalItems(), page.totalPages(),
				items);
	}

}
