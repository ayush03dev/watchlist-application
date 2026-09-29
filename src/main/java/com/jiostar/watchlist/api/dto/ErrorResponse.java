package com.jiostar.watchlist.api.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jiostar.watchlist.domain.WatchlistErrorCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String code, String message, Instant timestamp, String path, Integer maxSize) {

	public static ErrorResponse of(WatchlistErrorCode code, String message, String path, Integer maxSize) {
		return new ErrorResponse(code.name(), message, Instant.now(), path, maxSize);
	}

}
