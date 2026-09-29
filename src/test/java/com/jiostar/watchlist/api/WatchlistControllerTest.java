package com.jiostar.watchlist.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jiostar.watchlist.api.validation.AllowedWatchlistUserValidator;
import com.jiostar.watchlist.config.WatchlistProperties;
import com.jiostar.watchlist.domain.AddWatchlistItemResult;
import com.jiostar.watchlist.domain.WatchlistItem;
import com.jiostar.watchlist.domain.WatchlistPage;
import com.jiostar.watchlist.domain.WatchlistService;
import com.jiostar.watchlist.exception.GlobalExceptionHandler;
import com.jiostar.watchlist.exception.WatchlistFullException;

@WebMvcTest(WatchlistController.class)
@Import({ WatchlistControllerTest.TestConfig.class, AllowedWatchlistUserValidator.class, GlobalExceptionHandler.class })
class WatchlistControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WatchlistService watchlistService;

	@Test
	void putReturns201WhenNewItem() throws Exception {
		Instant addedAt = Instant.parse("2026-09-29T10:00:00Z");
		when(watchlistService.addOrBump(1, "movie-1"))
				.thenReturn(new AddWatchlistItemResult(1, "movie-1", addedAt, false));

		mockMvc.perform(put("/api/v1/watchlist/items").contentType(MediaType.APPLICATION_JSON)
				.content("{\"userId\":1,\"contentId\":\"movie-1\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userId").value(1))
				.andExpect(jsonPath("$.contentId").value("movie-1"))
				.andExpect(jsonPath("$.bumped").value(false));
	}

	@Test
	void putReturns200WhenBumped() throws Exception {
		when(watchlistService.addOrBump(2, "movie-2"))
				.thenReturn(new AddWatchlistItemResult(2, "movie-2", Instant.now(), true));

		mockMvc.perform(put("/api/v1/watchlist/items").contentType(MediaType.APPLICATION_JSON)
				.content("{\"userId\":2,\"contentId\":\"movie-2\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bumped").value(true));
	}

	@Test
	void putReturns409WhenFull() throws Exception {
		when(watchlistService.addOrBump(1, "new")).thenThrow(new WatchlistFullException(500));

		mockMvc.perform(put("/api/v1/watchlist/items").contentType(MediaType.APPLICATION_JSON)
				.content("{\"userId\":1,\"contentId\":\"new\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("WATCHLIST_FULL"))
				.andExpect(jsonPath("$.maxSize").value(500))
				.andExpect(jsonPath("$.path").value("/api/v1/watchlist/items"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void deleteReturns204() throws Exception {
		mockMvc.perform(delete("/api/v1/watchlist/items").contentType(MediaType.APPLICATION_JSON)
				.content("{\"userId\":3,\"contentId\":\"x\"}"))
				.andExpect(status().isNoContent());

		verify(watchlistService).remove(3, "x");
	}

	@Test
	void getReturnsPage() throws Exception {
		WatchlistPage page = new WatchlistPage(1, 0, 20, 1, 1,
				List.of(new WatchlistItem("a", Instant.parse("2026-01-01T00:00:00Z"))));
		when(watchlistService.list(eq(1), eq(0), eq(20))).thenReturn(page);

		mockMvc.perform(get("/api/v1/watchlist/items").param("userId", "1").param("page", "0").param("size", "20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalItems").value(1))
				.andExpect(jsonPath("$.items[0].contentId").value("a"));
	}

	@Test
	void rejectsDisallowedUserIdOnPut() throws Exception {
		mockMvc.perform(put("/api/v1/watchlist/items").contentType(MediaType.APPLICATION_JSON)
				.content("{\"userId\":99,\"contentId\":\"a\"}"))
				.andExpect(status().isBadRequest());
	}

	@TestConfiguration
	static class TestConfig {

		@Bean
		WatchlistService watchlistService() {
			return org.mockito.Mockito.mock(WatchlistService.class);
		}

		@Bean
		WatchlistProperties watchlistProperties() {
			WatchlistProperties properties = new WatchlistProperties();
			properties.setAllowedUserIds(List.of(1, 2, 3, 4));
			return properties;
		}

	}

}
