package com.jiostar.watchlist.exception;

public class WatchlistFullException extends RuntimeException {

	private final int maxSize;

	public WatchlistFullException(int maxSize) {
		super("Watch list has reached the maximum size of " + maxSize);
		this.maxSize = maxSize;
	}

	public int getMaxSize() {
		return maxSize;
	}

}
