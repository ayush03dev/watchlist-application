package com.jiostar.watchlist.persistence;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class WatchlistItemId implements Serializable {

	@Column(name = "user_id", nullable = false)
	private int userId;

	@Column(name = "content_id", nullable = false, length = 128)
	private String contentId;

	public WatchlistItemId() {
	}

	public WatchlistItemId(int userId, String contentId) {
		this.userId = userId;
		this.contentId = contentId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof WatchlistItemId that)) {
			return false;
		}
		return userId == that.userId && Objects.equals(contentId, that.contentId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, contentId);
	}

}
