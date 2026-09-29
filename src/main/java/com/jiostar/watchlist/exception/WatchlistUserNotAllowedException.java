package com.jiostar.watchlist.exception;

public class WatchlistUserNotAllowedException extends RuntimeException {

	private final int userId;

	public WatchlistUserNotAllowedException(int userId) {
		super("userId " + userId + " is not allowed");
		this.userId = userId;
	}

	public int getUserId() {
		return userId;
	}

}
