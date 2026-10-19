/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import com.oracle.coherence.spring.session.serialization.pof.GrantedAuthorityPofSerializer;
import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.SimplePofContext;
import com.tangosol.util.ExternalizableHelper;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regressions for Spring Security 7 token graphs and the existing POF wire fields.
 */
class AuthenticationPofSerializerTests {

	private final ConfigurablePofContext context = new ConfigurablePofContext("pof-config.xml");

	@Test
	void preservesMixedAuthoritiesAndErasedCredentials() {
		FactorGrantedAuthority factor = FactorGrantedAuthority.withAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY)
				.issuedAt(Instant.parse("2026-01-01T00:00:00Z")).build();
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("user", null,
				List.of(new SimpleGrantedAuthority("ROLE_USER"), factor));
		token.setDetails("details");
		UsernamePasswordAuthenticationToken copy = roundTrip(token);
		assertThat(copy.getAuthorities()).containsExactlyElementsOf(token.getAuthorities());
		assertThat(copy.getCredentials()).isNull();
		assertThat(copy.getPrincipal()).isEqualTo("user");
		assertThat(copy.getDetails()).isEqualTo("details");
		assertThat(copy.isAuthenticated()).isTrue();
	}

	@Test
	void preservesUnauthenticatedState() {
		UsernamePasswordAuthenticationToken copy = roundTrip(new UsernamePasswordAuthenticationToken("user", null));
		assertThat(copy.isAuthenticated()).isFalse();
		assertThat(copy.getCredentials()).isNull();
		assertThat(copy.getAuthorities()).isEmpty();
	}

	@Test
	void readsLegacyUniformAuthoritiesAndAbsentAuthenticationFlag() {
		SimplePofContext legacy = new SimplePofContext();
		legacy.registerUserType(3000, UsernamePasswordAuthenticationToken.class, new LegacyTokenSerializer());
		legacy.registerUserType(3002, SimpleGrantedAuthority.class, new GrantedAuthorityPofSerializer());
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("user", "password",
				List.of(new SimpleGrantedAuthority("ROLE_USER")));
		UsernamePasswordAuthenticationToken copy = ExternalizableHelper.fromBinary(
				ExternalizableHelper.toBinary(token, legacy), this.context);
		assertThat(copy.isAuthenticated()).isTrue();
		assertThat(copy.getPrincipal()).isEqualTo("user");
		assertThat(copy.getCredentials()).isEqualTo("password");
		assertThat(copy.getAuthorities()).containsExactlyElementsOf(token.getAuthorities());
	}

	private UsernamePasswordAuthenticationToken roundTrip(UsernamePasswordAuthenticationToken token) {
		return ExternalizableHelper.fromBinary(ExternalizableHelper.toBinary(token, this.context), this.context);
	}

	private static final class LegacyTokenSerializer implements PofSerializer<UsernamePasswordAuthenticationToken> {

		@Override
		public void serialize(PofWriter writer, UsernamePasswordAuthenticationToken token) throws IOException {
			writer.writeCollection(1, token.getAuthorities(), SimpleGrantedAuthority.class);
			writer.writeString(2, token.getCredentials().toString());
			writer.writeObject(3, token.getDetails());
			writer.writeObject(4, token.getPrincipal());
			writer.writeRemainder(null);
		}

		@Override
		public UsernamePasswordAuthenticationToken deserialize(PofReader reader) {
			throw new UnsupportedOperationException("write-only legacy fixture");
		}
	}
}
