/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.support;

import com.tangosol.util.Filter;
import com.tangosol.util.extractor.UniversalExtractor;
import com.tangosol.util.filter.LimitFilter;
import com.tangosol.util.function.Remote;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static com.tangosol.util.function.Remote.comparator;

/**
 * General static utilities.
 *
 * @author Ryan Lubke
 * @since 3.0.0
 */
public class Utils {

	/**
	 * Convert Spring {@link Sort} to {@link Remote.Comparator}.
	 * @param sort the spring sort configuration
	 * @param <T> the entity type
	 * @return a {@link Remote.Comparator} based on the provided {@link Sort}
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Nullable
	public static <T> Remote.Comparator<? super T> toComparator(@NonNull Sort sort) {
		Remote.Comparator comparator = null;
		if (sort.isSorted()) {
			for (Sort.Order order : sort) {
				if (comparator == null) {
					comparator = comparator(new UniversalExtractor(order.getProperty()));
					if (order.isDescending()) {
						comparator = comparator.reversed();
					}
				}
				else {
					Remote.Comparator temp = comparator(new UniversalExtractor(order.getProperty()));
					if (order.isDescending()) {
						temp = comparator.reversed();
					}
					comparator.thenComparing(temp);
				}
			}
		}

		return comparator;
	}

	/**
	 * Configure a {@link LimitFilter} based on the provided {@link Pageable}.
	 * @param pageable the {@link Pageable} to configure
	 * @param filter the {@link Filter} to be limited
	 * @return the configured {@link LimitFilter} or null if {@link Pageable#isPaged()}
	 *         is {@code false}
	 */
	@SuppressWarnings({"rawtypes"})
	@Nullable
	public static LimitFilter configureLimitFilter(Pageable pageable, Filter filter) {
		if (!pageable.isPaged()) {
			return null;
		}
		LimitFilter limitFilter = filter.asLimitFilter(pageable.getPageSize());
		limitFilter.setPage(pageable.getPageNumber());
		return limitFilter;
	}
}
