package com.jiostar.watchlist.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class RequestLoggingFilterTest {

	private final RequestLoggingFilter filter = new RequestLoggingFilter();

	@Test
	void skipsActuatorPaths() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
		assertThat(filter.shouldNotFilter(request)).isTrue();
	}

	@Test
	void filtersApiPaths() {
		MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/v1/watchlist/items");
		assertThat(filter.shouldNotFilter(request)).isFalse();
	}

}
