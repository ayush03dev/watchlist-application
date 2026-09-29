package com.jiostar.watchlist.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.jiostar.watchlist.domain.AddWatchlistItemResult;
import com.jiostar.watchlist.domain.WatchlistPage;
import com.jiostar.watchlist.domain.WatchlistService;
import com.jiostar.watchlist.exception.WatchlistFullException;

@SpringBootTest
@ActiveProfiles("integration")
@Tag("integration")
@TestPropertySource(properties = { "watchlist.max-size=2", "watchlist.max-page-size=10" })
@Transactional
class WatchlistServiceIntegrationTest {

	@Autowired
	private WatchlistService watchlistService;

	@Test
	void addAndListOrdersByMostRecentlyAdded() {
		watchlistService.addOrBump(1, "first");
		watchlistService.addOrBump(1, "second");

		WatchlistPage page = watchlistService.list(1, 0, 10);

		assertThat(page.items()).extracting(item -> item.contentId()).containsExactly("second", "first");
	}

	@Test
	void bumpMovesItemToTopWithoutUsingCapacitySlot() {
		watchlistService.addOrBump(2, "a");
		watchlistService.addOrBump(2, "b");

		AddWatchlistItemResult bumped = watchlistService.addOrBump(2, "a");

		assertThat(bumped.bumped()).isTrue();
		WatchlistPage page = watchlistService.list(2, 0, 10);
		assertThat(page.totalItems()).isEqualTo(2);
		assertThat(page.items()).extracting(item -> item.contentId()).startsWith("a");
	}

	@Test
	void rejectsNewItemWhenAtCapacityButAllowsBump() {
		watchlistService.addOrBump(3, "one");
		watchlistService.addOrBump(3, "two");

		assertThatThrownBy(() -> watchlistService.addOrBump(3, "three")).isInstanceOf(WatchlistFullException.class);

		AddWatchlistItemResult bumped = watchlistService.addOrBump(3, "one");
		assertThat(bumped.bumped()).isTrue();
		assertThat(watchlistService.list(3, 0, 10).totalItems()).isEqualTo(2);
	}

}
