package com.jiostar.watchlist.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class WatchlistItemRepositoryTest {

	@Autowired
	private WatchlistItemRepository repository;

	@Test
	void persistsAndFindsByUserOrderedByAddedAtDesc() {
		Instant older = Instant.parse("2026-01-01T10:00:00Z");
		Instant newer = Instant.parse("2026-01-02T10:00:00Z");

		repository.save(new WatchlistItemEntity(new WatchlistItemId(1, "a"), older));
		repository.save(new WatchlistItemEntity(new WatchlistItemId(1, "b"), newer));

		Page<WatchlistItemEntity> page = repository.findByIdUserIdOrderByAddedAtDesc(1, PageRequest.of(0, 10));

		assertThat(page.getTotalElements()).isEqualTo(2);
		assertThat(page.getContent()).extracting(e -> e.getId().getContentId()).containsExactly("b", "a");
	}

	@Test
	void countExistsAndDelete() {
		repository.save(new WatchlistItemEntity(new WatchlistItemId(2, "x"), Instant.now()));

		assertThat(repository.countByIdUserId(2)).isEqualTo(1);
		assertThat(repository.existsByIdUserIdAndIdContentId(2, "x")).isTrue();

		repository.deleteByIdUserIdAndIdContentId(2, "x");

		assertThat(repository.countByIdUserId(2)).isZero();
		assertThat(repository.findAll()).isEmpty();
	}

}
