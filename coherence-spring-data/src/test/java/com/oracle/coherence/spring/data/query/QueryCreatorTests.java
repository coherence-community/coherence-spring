package com.oracle.coherence.spring.data.query;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

import com.oracle.coherence.spring.data.model.Book;
import com.oracle.coherence.spring.data.repository.query.CoherenceQueryCreator;
import com.oracle.coherence.spring.data.repository.query.QueryResult;
import com.tangosol.util.Filters;
import com.tangosol.util.aggregator.Count;
import com.tangosol.util.aggregator.DistinctValues;
import com.tangosol.util.extractor.UniversalExtractor;
import com.tangosol.util.filter.LimitFilter;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;

import static com.oracle.coherence.spring.data.AbstractDataTest.FRANK_HERBERT;
import static org.assertj.core.api.Assertions.assertThat;

public class QueryCreatorTests {

	@Test
	public void ensureCreatesQuery() {
		QueryResult result = getQuery("findByAuthor", FRANK_HERBERT);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(Filters.equal("author", FRANK_HERBERT));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	public void ensureAndQuery() {
		QueryResult result = getQuery("findByAuthorAndPagesGreaterThan", FRANK_HERBERT, 100);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.equal("author", FRANK_HERBERT)
						.and(Filters.greater(new UniversalExtractor<>("pages"), 100)));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	public void ensureMultipleAndQuery() {
		QueryResult result = getQuery("findByAuthorAndPagesGreaterThanAndPagesLessThan", FRANK_HERBERT, 300, 500);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.equal("author", FRANK_HERBERT)
						.and(Filters.greater(new UniversalExtractor<>("pages"), 300))
						.and(Filters.less(new UniversalExtractor<>("pages"), 500)));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	public void ensureOrQuery() {
		QueryResult result = getQuery("findByPagesGreaterThanOrPagesLessThan", 300, 500);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.greater(new UniversalExtractor<>("pages"), 300)
						.or(Filters.less(new UniversalExtractor<>("pages"), 500)));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureMultipleOrQuery() {
		QueryResult result = getQuery("findByAuthorOrPagesGreaterThanOrPagesLessThan", FRANK_HERBERT, 300, 500);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.equal("author", FRANK_HERBERT)
						.or(Filters.greater(new UniversalExtractor<>("pages"), 300))
						.or(Filters.less(new UniversalExtractor<>("pages"), 500)));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureGreaterThanQuery() {
		QueryResult result = getQuery("findByPagesIsGreaterThan", 500);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.greater(new UniversalExtractor<>("pages"), 500));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureGreaterThanEqualQuery() {
		QueryResult result = getQuery("findByPagesIsGreaterThanEqual", 500);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.greaterEqual(new UniversalExtractor<>("pages"), 500));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureLessThanQuery() {
		QueryResult result = getQuery("findByPagesIsLessThan", 500);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.less(new UniversalExtractor<>("pages"), 500));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureLessThanEqualQuery() {
		QueryResult result = getQuery("findByPagesIsLessThanEqual", 500);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.lessEqual(new UniversalExtractor<>("pages"), 500));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureAfterQuery() {
		QueryResult result = getQuery("findByPublicationYearAfter", 1973);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.greater(new UniversalExtractor<>("publicationYear"), 1973));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureBeforeQuery() {
		QueryResult result = getQuery("findByPublicationYearBefore", 1973);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.less(new UniversalExtractor<>("publicationYear"), 1973));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureContainsQuery() {
		QueryResult result = getQuery("findByTitleContains", "keyword");

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.like(new UniversalExtractor<>("title"), "%keyword%"));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureLikeQuery() {
		QueryResult result = getQuery("findByTitleLike", "%keyw%ord%");

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.like(new UniversalExtractor<>("title"), "%keyw%ord%"));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureStartingWithQuery() {
		QueryResult result = getQuery("findByTitleStartingWith", "keyword");

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.like(new UniversalExtractor<>("title"), "keyword%"));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureEndingWithQuery() {
		QueryResult result = getQuery("findByTitleIn", Arrays.asList("Dune", "Dune Messiah"));

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.in(new UniversalExtractor<>("title"), "Dune", "Dune Messiah"));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureInQuery() {
		QueryResult result = getQuery("findByTitleEndingWith", "keyword");

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.like(new UniversalExtractor<>("title"), "%keyword"));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureBetweenQuery() {
		QueryResult result = getQuery("findByPublicationYearBetween", 1970, 1975);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.between(new UniversalExtractor<>("publicationYear"), 1970, 1975));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureIsNullQuery() {
		QueryResult result = getQuery("findByAuthorIsNull");

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(Filters.isNull(new UniversalExtractor<>("author")));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureIsNotNullQuery() {
		QueryResult result = getQuery("findByAuthorIsNotNull");

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(Filters.isNotNull(new UniversalExtractor<>("author")));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureCountQuery() {
		QueryResult result = getQuery("countByPagesGreaterThan", 500);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.greater(new UniversalExtractor<>("pages"), 500));
		assertThat(result.getAggregator()).isInstanceOf(Count.class);
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureDistinctQuery() {
		QueryResult result = getQuery("findDistinctTitleByPagesGreaterThan", 500);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.greater(new UniversalExtractor<>("pages"), 500));
		assertThat(result.getAggregator()).isInstanceOf(DistinctValues.class);
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensureExistsQuery() {
		QueryResult result = getQuery("existsByAuthor", FRANK_HERBERT);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(Filters.equal("author", FRANK_HERBERT));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@Test
	void ensurePropertyExpressions() {
		QueryResult result = getQuery("findByAuthorLastName", "King");

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(
				Filters.equal(new UniversalExtractor<>("author.lastName"), "King"));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());
	}

	@SuppressWarnings("rawtypes")
	@Test
	void ensureFirstQuery() {
		QueryResult result = getQuery("findFirstByAuthor", FRANK_HERBERT);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isInstanceOf(LimitFilter.class);
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());

		LimitFilter limitFilter = (LimitFilter) result.getFilter();
		assertThat(limitFilter.getFilter()).isEqualTo(
				Filters.equal(new UniversalExtractor<>("author"), FRANK_HERBERT));
		assertThat(limitFilter.getPageSize()).isEqualTo(1);
	}

	@SuppressWarnings("rawtypes")
	@Test
	void ensureFirstQueryWithValue() {
		QueryResult result = getQuery("findFirst10ByAuthor", FRANK_HERBERT);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isInstanceOf(LimitFilter.class);
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());

		LimitFilter limitFilter = (LimitFilter) result.getFilter();
		assertThat(limitFilter.getFilter()).isEqualTo(
				Filters.equal(new UniversalExtractor<>("author"), FRANK_HERBERT));
		assertThat(limitFilter.getPageSize()).isEqualTo(10);
	}

	@SuppressWarnings("rawtypes")
	@Test
	void ensureTopQuery() {
		QueryResult result = getQuery("findTopByPagesGreaterThan", 100);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isInstanceOf(LimitFilter.class);
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());

		LimitFilter limitFilter = (LimitFilter) result.getFilter();
		assertThat(limitFilter.getFilter()).isEqualTo(
				Filters.greater(new UniversalExtractor<>("pages"), 100));
		assertThat(limitFilter.getPageSize()).isEqualTo(1);
	}

	@SuppressWarnings("rawtypes")
	@Test
	void ensureTopQueryWithValue() {
		QueryResult result = getQuery("findTop10ByPagesGreaterThan", 100);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isInstanceOf(LimitFilter.class);
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.unsorted());

		LimitFilter limitFilter = (LimitFilter) result.getFilter();
		assertThat(limitFilter.getFilter()).isEqualTo(
				Filters.greater(new UniversalExtractor<>("pages"), 100));
		assertThat(limitFilter.getPageSize()).isEqualTo(10);

	}

	@Test
	void ensureOrderedAscQuery() {
		QueryResult result = getQuery("findByAuthorOrderByTitleAsc", FRANK_HERBERT);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(Filters.equal("author", FRANK_HERBERT));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "title"));
	}

	@Test
	void ensureOrderedDescQuery() {
		QueryResult result = getQuery("findByAuthorOrderByTitleDesc", FRANK_HERBERT);

		assertThat(result).isNotNull();
		assertThat(result.getFilter()).isEqualTo(Filters.equal("author", FRANK_HERBERT));
		assertThat(result.getAggregator()).isNull();
		assertThat(result.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "title"));
	}

	// ----- helper methods -------------------------------------------------

	private QueryResult getQuery(String finderQuery, Object... values) {
		PartTree tree = new PartTree(finderQuery, Book.class);
		CoherenceQueryCreator creator = new CoherenceQueryCreator(tree,
				new SimpleParameterAccessor(values));
		return creator.createQuery();
	}

	// ----- SimpleParameterAccessor ----------------------------------------

	@SuppressWarnings("NullableProblems")
	private static final class SimpleParameterAccessor implements ParameterAccessor {
		Object[] values;

		SimpleParameterAccessor(Object... values) {
			this.values = values;
		}

		@Override
		public Pageable getPageable() {
			return null;
		}

		@Override
		public Sort getSort() {
			return null;
		}

		@Override
		public Optional<Class<?>> getDynamicProjection() {
			return Optional.empty();
		}

		@Override
		public Class<?> findDynamicProjection() {
			return null;
		}

		@Override
		public Object getBindableValue(int index) {
			return this.values[index];
		}

		@Override
		public boolean hasBindableNullValue() {
			return false;
		}

		@Override
		public Iterator<Object> iterator() {
			return Arrays.asList(this.values).iterator();
		}
	}
}
