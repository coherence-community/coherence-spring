/*
 * Copyright (c) 2021, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.session.config;

import com.oracle.coherence.spring.samples.session.filter.JsonUsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

/**
 * Contains Spring Security configuration.
 * @author Gunnar Hillert
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private ObjectMapper objectMapper;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(
			AuthenticationManager authenticationManager,
			HttpSecurity http) throws Exception {
		final BasicAuthenticationFilter basicAuthenticationFilter = new BasicAuthenticationFilter(
				authenticationManager, basicAuthenticationEntryPoint());
		http.securityContext((securityContext) -> securityContext.requireExplicitSave(false))
				.addFilterAfter(customAuthFilter(authenticationManager), RequestHeaderAuthenticationFilter.class)
				.addFilterAfter(basicAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.csrf((csrf) -> csrf.disable())
				.requestCache((requestCache) -> requestCache.disable())
				.authorizeHttpRequests((requests) -> requests
						.requestMatchers("/login").permitAll()
						.anyRequest().authenticated())
				.exceptionHandling((exceptionHandling) -> exceptionHandling
						.defaultAuthenticationEntryPointFor(basicAuthenticationEntryPoint(), AnyRequestMatcher.INSTANCE))
				.logout((logout) -> logout.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()));
		return http.build();
	}

	private AuthenticationSuccessHandler authenticationSuccessHandler() {
		return (httpServletRequest, httpServletResponse, authentication) -> httpServletResponse.setStatus(200);
	}

	@Bean
	public BasicAuthenticationEntryPoint basicAuthenticationEntryPoint() {
		final BasicAuthenticationEntryPoint basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
		basicAuthenticationEntryPoint.setRealmName("Coherence Spring");
		return basicAuthenticationEntryPoint;
	}

	@Bean
	public UsernamePasswordAuthenticationFilter customAuthFilter(AuthenticationManager authenticationManager) throws Exception {
		final UsernamePasswordAuthenticationFilter authenticationFilter = new JsonUsernamePasswordAuthenticationFilter(this.objectMapper);
		authenticationFilter.setRequiresAuthenticationRequestMatcher(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/login"));
		authenticationFilter.setUsernameParameter("username");
		authenticationFilter.setPasswordParameter("password");
		authenticationFilter.setAuthenticationManager(authenticationManager);
		authenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler());
		authenticationFilter.setSessionAuthenticationStrategy(new ChangeSessionIdAuthenticationStrategy());

		return authenticationFilter;
	}

	@Bean
	public InMemoryUserDetailsManager userDetailsService() {
		UserDetails user = User.withDefaultPasswordEncoder()
				.username("coherence")
				.password("rocks")
				.roles("USER")
				.build();
		return new InMemoryUserDetailsManager(user);
	}
}
