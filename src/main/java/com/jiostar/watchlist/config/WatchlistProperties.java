package com.jiostar.watchlist.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

@ConfigurationProperties(prefix = "watchlist")
@Validated
public class WatchlistProperties {

	@Min(1)
	private int maxSize = 500;

	@Min(1)
	private int maxPageSize = 50;

	@NotEmpty
	private List<@Positive Integer> allowedUserIds = List.of(1, 2, 3, 4);

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getMaxPageSize() {
		return maxPageSize;
	}

	public void setMaxPageSize(int maxPageSize) {
		this.maxPageSize = maxPageSize;
	}

	public List<Integer> getAllowedUserIds() {
		return allowedUserIds;
	}

	public void setAllowedUserIds(List<Integer> allowedUserIds) {
		this.allowedUserIds = allowedUserIds;
	}

	public boolean isAllowedUserId(int userId) {
		return allowedUserIds.contains(userId);
	}

}
