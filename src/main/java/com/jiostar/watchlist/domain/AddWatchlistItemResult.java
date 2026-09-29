package com.jiostar.watchlist.domain;

import java.time.Instant;

public record AddWatchlistItemResult(int userId, String contentId, Instant addedAt, boolean bumped) {

}
