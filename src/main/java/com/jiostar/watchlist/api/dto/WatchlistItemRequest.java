package com.jiostar.watchlist.api.dto;

import com.jiostar.watchlist.api.validation.AllowedWatchlistUser;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class WatchlistItemRequest {

	@NotNull
	@AllowedWatchlistUser
	private Integer userId;

	@NotBlank
	@Size(max = 128)
	private String contentId;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

}
