package com.jiostar.watchlist.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

@SpringJUnitConfig(initializers = ConfigDataApplicationContextInitializer.class)
@EnableConfigurationProperties(WatchlistProperties.class)
class WatchlistPropertiesTest {

	@Autowired
	private WatchlistProperties properties;

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void bindsFromApplicationYaml() {
		assertThat(properties.getMaxSize()).isEqualTo(500);
		assertThat(properties.getMaxPageSize()).isEqualTo(50);
		assertThat(properties.getAllowedUserIds()).containsExactly(1, 2, 3, 4);
	}

	@Test
	void isAllowedUserId() {
		assertThat(properties.isAllowedUserId(1)).isTrue();
		assertThat(properties.isAllowedUserId(4)).isTrue();
		assertThat(properties.isAllowedUserId(99)).isFalse();
	}

	@Test
	void rejectsNonPositiveMaxSize() {
		WatchlistProperties invalid = new WatchlistProperties();
		invalid.setMaxSize(0);
		invalid.setMaxPageSize(20);
		invalid.setAllowedUserIds(List.of(1));

		Set<ConstraintViolation<WatchlistProperties>> violations = validator.validate(invalid);

		assertThat(violations).isNotEmpty();
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("maxSize"));
	}

	@Test
	void rejectsEmptyAllowedUserIds() {
		WatchlistProperties invalid = new WatchlistProperties();
		invalid.setMaxSize(100);
		invalid.setMaxPageSize(20);
		invalid.setAllowedUserIds(List.of());

		Set<ConstraintViolation<WatchlistProperties>> violations = validator.validate(invalid);

		assertThat(violations).isNotEmpty();
		assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("allowedUserIds"));
	}

}
