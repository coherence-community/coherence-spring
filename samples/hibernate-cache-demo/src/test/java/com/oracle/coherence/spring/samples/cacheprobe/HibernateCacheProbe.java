/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.cacheprobe;

import java.util.Arrays;
import java.util.List;

import com.oracle.coherence.hibernate.cache.v7.CoherenceRegionFactory;
import com.oracle.coherence.spring.samples.hibernate.HibernateApplication;
import com.oracle.coherence.spring.samples.hibernate.dao.PlantRepository;
import com.oracle.coherence.spring.samples.hibernate.model.Plant;
import com.tangosol.internal.net.SessionNamedCache;
import com.tangosol.io.DefaultSerializer;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Coherence;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.NearCache;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import org.springframework.boot.SpringApplication;

import static org.assertj.core.api.Assertions.assertThat;

/** Fresh JVM using only the packaged application's production dependencies. */
public final class HibernateCacheProbe {

	private HibernateCacheProbe() {
	}

	public static void main(String[] args) throws Exception {
		if (args[0].equals("storage")) {
			CacheRuntime.observe();
			Coherence.main(new String[0]);
			return;
		}
		try (var context = SpringApplication.run(HibernateApplication.class, Arrays.copyOfRange(args, 1, args.length))) {
			CacheRuntime.observe();
			var factory = context.getBean(EntityManagerFactory.class).unwrap(SessionFactoryImplementor.class);
			var regionFactory = (CoherenceRegionFactory) factory.getCache().getRegionFactory();
			assertThat(regionFactory.getCoherenceSession().getName()).isEqualTo("coherence_hibernate");
			assertThat(factory.getSessionFactoryOptions().isSecondLevelCacheEnabled()).isTrue();
			assertThat(factory.getSessionFactoryOptions().isQueryCacheEnabled()).isTrue();
			var statistics = factory.getStatistics();
			statistics.setStatisticsEnabled(true);
			var repository = context.getBean(PlantRepository.class);
			Plant saved = repository.saveAndFlush(new Plant("Acer", "rubrum", "red maple"));
			factory.getCache().evictAllRegions();
			Plant first = repository.findById(saved.getId()).orElseThrow();
			statistics.clear();
			Plant second = repository.findById(saved.getId()).orElseThrow();
			assertThat(first).isNotSameAs(second);
			assertThat(second).usingRecursiveComparison().isEqualTo(saved);
			assertThat(statistics.getSecondLevelCacheHitCount()).isEqualTo(1);
			assertThat(statistics.getPrepareStatementCount()).isZero();
			System.out.println("HIBERNATE_ENTITY_HIT " + statistics);
			assertThat(repository.getAllPlantsCacheable()).hasSize(1);
			for (String name : List.of(Plant.class.getName(), "default-query-results-region", "default-update-timestamps-region")) {
				NamedCache<?, ?> cache = regionFactory.getCoherenceSession().getCache(name);
				assertThat(((DistributedCacheService) cache.getCacheService()).isLocalStorageEnabled()).isFalse();
				assertThat(cache.getCacheService().getSerializer()).isInstanceOf(DefaultSerializer.class);
				assertThat(cache.size()).isPositive();
				for (Object key : cache.keySet()) {
					Object value = cache.get(key);
					System.out.println("HIBERNATE_GRAPH cache=" + name + " key=" + key.getClass().getName()
							+ " value=" + value.getClass().getName());
					CacheRuntime.descriptors(key);
					CacheRuntime.descriptors(value);
				}
				if (!name.equals(Plant.class.getName())) {
					NamedCache<?, ?> internal = ((SessionNamedCache<?, ?>) cache).getInternalNamedCache();
					assertThat(internal).isInstanceOf(NearCache.class);
					NearCache<?, ?> near = (NearCache<?, ?>) internal;
					assertThat(near.getFrontMap()).isNotEmpty();
					near.getFrontMap().clear();
					assertThat(near.getFrontMap()).isEmpty();
					System.out.println("HIBERNATE_FRONT_CLEARED " + name + " size=" + near.getFrontMap().size());
				}
			}
			statistics.clear();
			assertThat(repository.getAllPlantsCacheable().get(0).getCommonName()).isEqualTo("red maple");
			assertThat(statistics.getQueryCacheHitCount()).isEqualTo(1);
			assertThat(statistics.getUpdateTimestampsCacheHitCount()).isPositive();
			assertThat(statistics.getPrepareStatementCount()).isZero();
			System.out.println("HIBERNATE_QUERY_HIT " + statistics);
			System.out.println("HIBERNATE_CACHE_PATH_PASS orm=" + org.hibernate.Version.getVersionString());
		}
		finally {
			Coherence.closeAll();
			CacheFactory.shutdown();
		}
	}
}
