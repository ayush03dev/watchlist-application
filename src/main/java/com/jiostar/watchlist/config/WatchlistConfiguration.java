package com.jiostar.watchlist.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WatchlistProperties.class)
public class WatchlistConfiguration {

}
