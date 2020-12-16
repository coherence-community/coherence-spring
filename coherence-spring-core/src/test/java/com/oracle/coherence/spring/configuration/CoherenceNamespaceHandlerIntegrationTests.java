package com.oracle.coherence.spring.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

public class CoherenceNamespaceHandlerIntegrationTests {

	@Scope(BeanDefinition.SCOPE_SINGLETON)
	@Named("cacheStore")
	public static class CacheStore implements com.tangosol.net.cache.CacheStore<Long, String> {
		private Map<Long, String> storeMap = new HashMap<>();

		public synchronized Map<Long, String> getStoreMap() {
			return storeMap;
		}

		@Override
		public synchronized void store(Long key, String value) {
			storeMap.put(key, value);
		}

		@Override
		public synchronized void storeAll(Map<? extends Long, ? extends String> map) {
			storeMap.putAll(map);
		}

		@Override
		public synchronized void erase(Long key) {
			storeMap.remove(key);
		}

		@Override
		public synchronized void eraseAll(Collection<? extends Long> keys) {
			keys.forEach(storeMap::remove);
		}

		@Override
		public synchronized String load(Long key) {
			return key.toString();
		}

		@Override
		public synchronized Map<Long, String> loadAll(Collection<? extends Long> keys) {
			return keys.stream().collect(Collectors.toMap(k -> k, Object::toString));
		}
	}
}
