package com.jiostar.watchlist.domain;

import java.time.Instant;

public record WatchlistItem(String contentId, Instant addedAt) {

}
