package com.jiostar.watchlist.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jiostar.watchlist.api.WatchlistController;
import com.jiostar.watchlist.api.validation.AllowedWatchlistUserValidator;
import com.jiostar.watchlist.config.WatchlistProperties;
import com.jiostar.watchlist.domain.WatchlistService;

@WebMvcTest(WatchlistController.class)
@Import({ GlobalExceptionHandlerTest.TestConfig.class, GlobalExceptionHandler.class, AllowedWatchlistUserValidator.class })
class GlobalExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WatchlistService watchlistService;

	@Test
	void returnsInternalErrorEnvelope() throws Exception {
		org.mockito.Mockito.when(watchlistService.addOrBump(1, "x"))
				.thenThrow(new RuntimeException("database down"));

		mockMvc.perform(put("/api/v1/watchlist/items").contentType(MediaType.APPLICATION_JSON)
				.content("{\"userId\":1,\"contentId\":\"x\"}"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
				.andExpect(jsonPath("$.message").value("Unexpected error"))
				.andExpect(jsonPath("$.path").exists())
				.andExpect(jsonPath("$.timestamp").exists());
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
