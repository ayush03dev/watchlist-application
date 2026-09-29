package com.jiostar.watchlist.api.validation;

import org.springframework.stereotype.Component;

import com.jiostar.watchlist.config.WatchlistProperties;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class AllowedWatchlistUserValidator implements ConstraintValidator<AllowedWatchlistUser, Integer> {

	private final WatchlistProperties properties;

	public AllowedWatchlistUserValidator(WatchlistProperties properties) {
		this.properties = properties;
	}

	@Override
	public boolean isValid(Integer userId, ConstraintValidatorContext context) {
		if (userId == null) {
			return true;
		}
		return properties.isAllowedUserId(userId);
	}

}
