/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.messaging;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;

/**
 * Conversion service for reactive types.
 *
 * @author Vaso Putica
 * @since 3.0
 */
class ReactorConversionService extends GenericConversionService {

	ReactorConversionService() {
		DefaultConversionService.addDefaultConverters(this);
		addReactorConverters(this);
	}

	private void addReactorConverters(ConverterRegistry converterRegistry) {
		// Mono
		converterRegistry.addConverter(Mono.class, Publisher.class, Mono::flux);
		converterRegistry.addConverter(Object.class, Mono.class, Mono::just);

		// Flux
		converterRegistry.addConverter(Flux.class, Mono.class, (source) -> source.take(1, true).single());
		converterRegistry.addConverter(Object.class, Flux.class, (o) -> {
			if (o instanceof Iterable) {
				return Flux.fromIterable((Iterable) o);
			}
			else {
				return Flux.just(o);
			}
		});

		// Publisher
		converterRegistry.addConverter(Publisher.class, Flux.class, (publisher) -> {
			if (publisher instanceof Flux) {
				return (Flux) publisher;
			}
			return Flux.from(publisher);
		});
		converterRegistry.addConverter(Publisher.class, Mono.class, Mono::from);
	}
}
