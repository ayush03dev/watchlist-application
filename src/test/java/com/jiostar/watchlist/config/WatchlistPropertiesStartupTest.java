package com.jiostar.watchlist.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

import com.jiostar.watchlist.WatchlistServiceApplication;

class WatchlistPropertiesStartupTest {

	@Test
	void contextFailsWhenMaxPageSizeInvalid() {
		assertThatThrownBy(() -> {
			SpringApplication app = new SpringApplication(WatchlistServiceApplication.class);
			app.setWebApplicationType(WebApplicationType.NONE);
			app.setAdditionalProfiles("invalid-watchlist");
			app.run();
		}).satisfies(ex -> assertThat(findCause(ex,
				org.springframework.boot.context.properties.ConfigurationPropertiesBindException.class)).isNotNull());
	}

	private static Throwable findCause(Throwable ex, Class<?> type) {
		for (Throwable current = ex; current != null; current = current.getCause()) {
			if (type.isInstance(current)) {
				return current;
			}
		}
		return null;
	}

}
