/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.di.wherefilter;

import com.oracle.coherence.spring.annotation.PropertyExtractor;
import com.oracle.coherence.spring.annotation.View;
import com.oracle.coherence.spring.annotation.WhereFilter;
import com.oracle.coherence.spring.configuration.annotation.CoherenceMap;
import com.oracle.coherence.spring.doc.filterbinding.Plant;
import com.tangosol.net.NamedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.assertj.core.api.Assertions;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class PeopleService {
	private final Log logger = LogFactory.getLog(PeopleService.class);

	@CoherenceMap("people")
	private NamedMap<String, Person> allPeople;

// tag::simpleWhereFilter[]
	@CoherenceMap("people")                           // <1>
	@View                                             // <2>
	@WhereFilter("lastName = 'Simpson'")              // <3>
	private NamedMap<String, Person> allSimpsons;     // <4>
// end::simpleWhereFilter[]

// tag::complexWhereFilter[]
	@CoherenceMap("people")
	@View
	@WhereFilter("lastName = 'Simpson' and age > 10") // <1>
	private NamedMap<String, Person> simpsons;
// end::complexWhereFilter[]

	public void getPeople() {
		assertThat(this.allPeople.size()).isEqualTo(10);
		assertThat(this.allSimpsons.size()).isEqualTo(5);
		assertThat(this.simpsons.size()).isEqualTo(2);
		Assertions.assertThat(this.allSimpsons.values()).extracting(Person::getLastName).containsExactly(
				"Simpson", "Simpson","Simpson","Simpson","Simpson");
		Assertions.assertThat(this.allSimpsons.values()).extracting(Person::getFirstName).containsExactly(
				"Homer", "Marge","Bart","Lisa","Maggie");
		Assertions.assertThat(this.simpsons.values()).extracting(Person::getAge).containsExactly(
				36, 34);
		Assertions.assertThat(this.simpsons.values()).extracting(Person::getFirstName).containsExactly(
				"Homer", "Marge");
		Assertions.assertThat(this.simpsons.values()).extracting(Person::getLastName).containsExactly(
				"Simpson", "Simpson");
	}
}
