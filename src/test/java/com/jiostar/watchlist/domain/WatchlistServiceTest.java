package com.jiostar.watchlist.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.jiostar.watchlist.config.WatchlistProperties;
import com.jiostar.watchlist.exception.WatchlistFullException;
import com.jiostar.watchlist.exception.WatchlistUserNotAllowedException;
import com.jiostar.watchlist.persistence.WatchlistItemEntity;
import com.jiostar.watchlist.persistence.WatchlistItemId;
import com.jiostar.watchlist.persistence.WatchlistItemRepository;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

	@Mock
	private WatchlistItemRepository repository;

	private WatchlistProperties properties;

	private WatchlistService service;

	@BeforeEach
	void setUp() {
		properties = new WatchlistProperties();
		properties.setMaxSize(2);
		properties.setMaxPageSize(50);
		properties.setAllowedUserIds(List.of(1, 2, 3, 4));
		service = new WatchlistService(repository, properties);
	}

	@Test
	void addOrBump_insertsWhenNewAndUnderCapacity() {
		when(repository.findById(new WatchlistItemId(1, "a"))).thenReturn(Optional.empty());
		when(repository.countByIdUserId(1)).thenReturn(1L);

		AddWatchlistItemResult result = service.addOrBump(1, "a");

		assertThat(result.bumped()).isFalse();
		assertThat(result.userId()).isEqualTo(1);
		assertThat(result.contentId()).isEqualTo("a");
		assertThat(result.addedAt()).isNotNull();

		ArgumentCaptor<WatchlistItemEntity> captor = ArgumentCaptor.forClass(WatchlistItemEntity.class);
		verify(repository).save(captor.capture());
		assertThat(captor.getValue().getId().getContentId()).isEqualTo("a");
	}

	@Test
	void addOrBump_bumpsExistingWithoutCapacityCheck() {
		Instant older = Instant.parse("2026-01-01T00:00:00Z");
		WatchlistItemEntity existing = new WatchlistItemEntity(new WatchlistItemId(1, "a"), older);
		when(repository.findById(new WatchlistItemId(1, "a"))).thenReturn(Optional.of(existing));

		AddWatchlistItemResult result = service.addOrBump(1, "a");

		assertThat(result.bumped()).isTrue();
		assertThat(existing.getAddedAt()).isAfter(older);
		verify(repository).save(existing);
		verify(repository, never()).countByIdUserId(1);
	}

	@Test
	void addOrBump_throwsWhenFullAndNewItem() {
		when(repository.findById(new WatchlistItemId(1, "new"))).thenReturn(Optional.empty());
		when(repository.countByIdUserId(1)).thenReturn(2L);

		assertThatThrownBy(() -> service.addOrBump(1, "new")).isInstanceOf(WatchlistFullException.class)
				.extracting("maxSize").isEqualTo(2);

		verify(repository, never()).save(any());
	}

	@Test
	void remove_deletesItem() {
		service.remove(2, "x");
		verify(repository).deleteByIdUserIdAndIdContentId(2, "x");
	}

	@Test
	void list_returnsPageAndCapsSize() {
		Instant t = Instant.parse("2026-01-02T00:00:00Z");
		WatchlistItemEntity entity = new WatchlistItemEntity(new WatchlistItemId(1, "b"), t);
		properties.setMaxPageSize(10);
		when(repository.findByIdUserIdOrderByAddedAtDesc(eq(1), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1));

		WatchlistPage page = service.list(1, 0, 100);

		assertThat(page.size()).isEqualTo(10);
		assertThat(page.items()).hasSize(1);
		assertThat(page.items().get(0).contentId()).isEqualTo("b");
		assertThat(page.totalItems()).isEqualTo(1);
	}

	@Test
	void rejectsDisallowedUserId() {
		assertThatThrownBy(() -> service.addOrBump(99, "a")).isInstanceOf(WatchlistUserNotAllowedException.class);
		assertThatThrownBy(() -> service.remove(99, "a")).isInstanceOf(WatchlistUserNotAllowedException.class);
		assertThatThrownBy(() -> service.list(99, 0, 10)).isInstanceOf(WatchlistUserNotAllowedException.class);
	}

	@Test
	void rejectsInvalidPagination() {
		assertThatThrownBy(() -> service.list(1, -1, 10)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> service.list(1, 0, 0)).isInstanceOf(IllegalArgumentException.class);
	}

}
