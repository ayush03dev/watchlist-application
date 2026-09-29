package com.jiostar.watchlist.domain;

import java.util.List;

public record WatchlistPage(int userId, int page, int size, long totalItems, int totalPages,
		List<WatchlistItem> items) {

}
