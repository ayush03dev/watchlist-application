package com.jiostar.watchlist.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "watchlist_item")
public class WatchlistItemEntity {

	@EmbeddedId
	private WatchlistItemId id;

	@Column(name = "added_at", nullable = false)
	private Instant addedAt;

	public WatchlistItemEntity() {
	}

	public WatchlistItemEntity(WatchlistItemId id, Instant addedAt) {
		this.id = id;
		this.addedAt = addedAt;
	}

	public WatchlistItemId getId() {
		return id;
	}

	public void setId(WatchlistItemId id) {
		this.id = id;
	}

	public Instant getAddedAt() {
		return addedAt;
	}

	public void setAddedAt(Instant addedAt) {
		this.addedAt = addedAt;
	}

}
