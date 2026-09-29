package com.jiostar.watchlist.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jiostar.watchlist.api.dto.AddWatchlistItemResponse;
import com.jiostar.watchlist.api.dto.WatchlistItemRequest;
import com.jiostar.watchlist.api.dto.WatchlistPageResponse;
import com.jiostar.watchlist.api.validation.AllowedWatchlistUser;
import com.jiostar.watchlist.domain.AddWatchlistItemResult;
import com.jiostar.watchlist.domain.WatchlistPage;
import com.jiostar.watchlist.domain.WatchlistService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1/watchlist/items")
@Validated
public class WatchlistController {

	private final WatchlistService watchlistService;

	public WatchlistController(WatchlistService watchlistService) {
		this.watchlistService = watchlistService;
	}

	@PutMapping
	public ResponseEntity<AddWatchlistItemResponse> addOrBump(@Valid @RequestBody WatchlistItemRequest request) {
		AddWatchlistItemResult result = watchlistService.addOrBump(request.getUserId(), request.getContentId());
		HttpStatus status = result.bumped() ? HttpStatus.OK : HttpStatus.CREATED;
		return ResponseEntity.status(status).body(AddWatchlistItemResponse.from(result));
	}

	@DeleteMapping
	public ResponseEntity<Void> remove(@Valid @RequestBody WatchlistItemRequest request) {
		watchlistService.remove(request.getUserId(), request.getContentId());
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public WatchlistPageResponse list(
			@RequestParam @NotNull @AllowedWatchlistUser Integer userId,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) int size) {
		WatchlistPage result = watchlistService.list(userId, page, size);
		return WatchlistPageResponse.from(result);
	}

}
