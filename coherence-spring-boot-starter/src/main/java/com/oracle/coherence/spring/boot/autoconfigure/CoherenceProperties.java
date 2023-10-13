/*
 * Copyright (c) 2013, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.autoconfigure;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oracle.coherence.spring.boot.autoconfigure.support.LogType;
import com.oracle.coherence.spring.configuration.session.AbstractSessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.ClientSessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.ServerSessionConfigurationBean;
import com.oracle.coherence.spring.configuration.support.CoherenceInstanceType;
import com.oracle.coherence.spring.configuration.support.SpringSystemPropertyResolver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Configuration properties for the Coherence Spring integration.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@ConfigurationProperties(prefix = CoherenceProperties.PREFIX)
public class CoherenceProperties {

	/**
	 * Configuration prefix for config properties.
	 */
	public static final String PREFIX = "coherence";

	/**
	 * The name of the config property for the character limit.
	 */
	private static final String LOG_LIMIT = "coherence.log.limit";

	/**
	 * The name of the config property for logging destination.
	 */
	private static final String LOG_DESTINATION = "coherence.log";

	/**
	 * The name of the config property for log level.
	 */
	private static final String LOG_LEVEL = "coherence.log.level";

	/**
	 * The name of the config property for logger name.
	 */
	private static final String LOG_LOGGER_NAME = "coherence.log.logger";

	/**
	 * The name of the config property for message format.
	 */
	private static final String LOG_MESSAGE_FORMAT = "coherence.log.format";

	/**
	 * Additional native properties passed to Coherence.
	 */
	private Map<String, String> properties = new HashMap<>();

	/**
	 * Coherence Logging configuration.
	 */
	private LoggingProperties logging;

	/**
	 * Spring cache abstraction configuration.
	 */
	private CacheAbstractionProperties cache = new CacheAbstractionProperties();


	/**
	 * Default prefix for Coherence properties. Defaults to {@link SpringSystemPropertyResolver#DEFAULT_PROPERTY_PREFIX}.
	 */
	private String propertyPrefix = SpringSystemPropertyResolver.DEFAULT_PROPERTY_PREFIX;

	/**
	 * Configuration properties of the Coherence Server.
	 */
	private ServerProperties server = new ServerProperties();

	/**
	 * Configuration properties of the Coherence Instance.
	 */
	private InstanceProperties instance = new InstanceProperties();

	/**
	 * Session configuration.
	 */
	private SessionProperties sessions = new SessionProperties();

	public SessionProperties getSessions() {
		return this.sessions;
	}

	public void setSessions(SessionProperties sessions) {
		this.sessions = sessions;
	}

	public Map<String, String> getProperties() {
		return this.properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public LoggingProperties getLogging() {
		return this.logging;
	}

	public void setLogging(LoggingProperties logging) {
		this.logging = logging;
	}

	public String getPropertyPrefix() {
		return this.propertyPrefix;
	}

	public void setPropertyPrefix(String propertyPrefix) {
		this.propertyPrefix = propertyPrefix;
	}

	public CacheAbstractionProperties getCache() {
		return this.cache;
	}

	public void setCache(CacheAbstractionProperties cache) {
		this.cache = cache;
	}

	public ServerProperties getServer() {
		return this.server;
	}

	public void setServer(ServerProperties server) {
		this.server = server;
	}

	public InstanceProperties getInstance() {
		return this.instance;
	}

	public void setInstance(InstanceProperties instance) {
		this.instance = instance;
	}

	/**
	 * Returns a {@link Map} of Coherence properties using the format {@code coherence.properties.*}.
	 * @return the Coherence properties as a {@link Map}. Never returns null.
	 */
	final Map<String, Object> retrieveCoherencePropertiesAsMap() {
		final Map<String, Object> coherenceProperties = new HashMap<>();

		if (this.logging != null) {

			if (this.logging.destination != null) {
				coherenceProperties.put(this.propertyPrefix + this.LOG_DESTINATION, this.logging.destination.getKey());
			}

			if (StringUtils.hasText(this.logging.loggerName)) {
				coherenceProperties.put(this.propertyPrefix + this.LOG_LOGGER_NAME, this.logging.loggerName);
			}

			if (this.logging.severityLevel != null) {
				coherenceProperties.put(this.propertyPrefix + this.LOG_LEVEL, this.logging.severityLevel);
			}

			if (StringUtils.hasText(this.logging.messageFormat)) {
				coherenceProperties.put(this.propertyPrefix + this.LOG_MESSAGE_FORMAT, this.logging.messageFormat);
			}

			if (this.logging.characterLimit != null) {
				coherenceProperties.put(this.propertyPrefix + this.LOG_LIMIT, this.logging.characterLimit);
			}
		}
		return coherenceProperties;
	}

	/**
	 * Coherence Logging Configuration.
	 */
	public static class LoggingProperties {

		/**
		 * The type of the logging destination. Default to "stderr" if not set.
		 */
		private LogType destination = LogType.SLF4J;

		/**
		 * Specifies which logged messages are emitted to the log destination. The legal values are -1 to 9.
		 * No messages are emitted if -1 is specified. More log messages are emitted as the log level is increased.
		 */
		private Integer severityLevel;

		/**
		 * Specifies a logger name within chosen logging system that logs Coherence related messages. This value is
		 * used by the JDK, log4j, log4j2, and slf4j logging systems. The default value is "Coherence".
		 */
		private String loggerName;

		/**
		 * Specifies how messages that have a logging level specified are formatted before passing them to the log destination.
		 * The format can include static text and any of the following replaceable parameters: {date}, {uptime}, {product},
		 * {version}, {level}, {thread}, {member}, {location}, {role}, {text}, and {ecid}.
		 *
		 * The default value is:
		 *
		 * {date}/{uptime} {product} {version} &lt;{level}&gt; (thread={thread}, member={member}): {text}
		 */
		private String messageFormat;

		/**
		 * Specifies the maximum number of characters that the logger daemon processes from the message queue before discarding
		 * all remaining messages in the queue. All messages that are discarded are summarized by the logging system with
		 * a single log entry that details the number of messages that were discarded and their total size. Legal values
		 * are positive integers or 0. Zero implies no limit. The default value in production mode is 1048576 and 2147483647
		 * in development mode.
		 */
		private Integer characterLimit;

		public LogType getDestination() {
			return this.destination;
		}

		public void setDestination(LogType destination) {
			this.destination = destination;
		}

		public Integer getSeverityLevel() {
			return this.severityLevel;
		}

		public void setSeverityLevel(Integer severityLevel) {
			this.severityLevel = severityLevel;
		}

		public String getLoggerName() {
			return this.loggerName;
		}

		public void setLoggerName(String loggerName) {
			this.loggerName = loggerName;
		}

		public String getMessageFormat() {
			return this.messageFormat;
		}

		public void setMessageFormat(String messageFormat) {
			this.messageFormat = messageFormat;
		}

		public Integer getCharacterLimit() {
			return this.characterLimit;
		}

		public void setCharacterLimit(Integer characterLimit) {
			this.characterLimit = characterLimit;
		}
	}

	/**
	 * Coherence Session Configuration.
	 */
	public static class SessionProperties {

		/**
		 * Client Session configuration.
		 */
		private List<ClientSessionConfigurationBean> client;

		/**
		 * Session configuration.
		 */
		private List<ServerSessionConfigurationBean> server;

		public List<ClientSessionConfigurationBean> getClient() {
			return this.client;
		}

		public void setClient(List<ClientSessionConfigurationBean> client) {
			this.client = client;
		}

		public List<ServerSessionConfigurationBean> getServer() {
			return this.server;
		}

		public void setServer(List<ServerSessionConfigurationBean> server) {
			this.server = server;
		}


		public List<AbstractSessionConfigurationBean> getAllSessionConfigurationBeans() {
			final List<AbstractSessionConfigurationBean> sessionConfigurationBeans = new ArrayList<>();
			if (!CollectionUtils.isEmpty(this.client)) {
				sessionConfigurationBeans.addAll(this.client);
			}
			if (!CollectionUtils.isEmpty(this.server)) {
				sessionConfigurationBeans.addAll(this.server);
			}
			return sessionConfigurationBeans;
		}
	}

	/**
	 * Spring cache abstraction properties.
	 */
	public static class CacheAbstractionProperties {

		/**
		 * Defines the global time-to-live (ttl) value for Coherence cache entries that are used as part of the Spring Cache
		 * abstraction. By default, this property will be initialized with a value of {@link Duration#ZERO}, which
		 * means that the expiration value for cache values will NOT be specified when performing cache puts. However,
		 * depending on your Coherence cache configuration in coherence-cache-config.xml, cache values may still expire.
		 */
		private Duration timeToLive = Duration.ZERO;

		/**
		 * Disabled by default. Prepend cache names with a prefix.
		 */
		private boolean useCacheNamePrefix = false;

		/**
		 * The String to prepend cache names with. Empty by default
		 */
		private String cacheNamePrefix = "";

		/**
		 * Enabled by default. Lock cache entries. When using Coherence*Extend or gRPC, it is recommended to not use
		 * locking.
		 */
		private boolean useLocks = true;

		/**
		 * Disabled by default. Locks the entire cache. This is usually not recommended.
		 */
		private boolean lockEntireCache = false;

		/**
		 * The number of milliseconds to continue trying to obtain a lock. When pass zero the lock attempt will to return
		 * immediately. Passing -1 will block indefinitely until the lock could be obtained. Defaults to 0.
		 */
		private long lockTimeout = 0;

		public Duration getTimeToLive() {
			return this.timeToLive;
		}

		public void setTimeToLive(Duration timeToLive) {
			this.timeToLive = timeToLive;
		}

		public String getCacheNamePrefix() {
			return this.cacheNamePrefix;
		}

		public void setCacheNamePrefix(String cacheNamePrefix) {
			this.cacheNamePrefix = cacheNamePrefix;
		}

		public boolean isUseCacheNamePrefix() {
			return this.useCacheNamePrefix;
		}

		public void setUseCacheNamePrefix(boolean useCacheNamePrefix) {
			this.useCacheNamePrefix = useCacheNamePrefix;
		}

		public boolean isUseLocks() {
			return this.useLocks;
		}

		public void setUseLocks(boolean useLocks) {
			this.useLocks = useLocks;
		}

		public boolean isLockEntireCache() {
			return this.lockEntireCache;
		}

		public void setLockEntireCache(boolean lockEntireCache) {
			this.lockEntireCache = lockEntireCache;
		}

		public long getLockTimeout() {
			return this.lockTimeout;
		}

		public void setLockTimeout(long lockTimeout) {
			this.lockTimeout = lockTimeout;
		}
	}

	/**
	 * Configuration properties of the Coherence Instance.
	 */
	public static class InstanceProperties {

		/**
		 * Defines the type of the Coherence instance. If not specified, defaults to CLIENT or CLUSTER, depending on the
		 * configured Coherence sessions.
		 */
		private CoherenceInstanceType type;

		public CoherenceInstanceType getType() {
			return this.type;
		}

		public void setType(CoherenceInstanceType type) {
			this.type = type;
		}
	}

	/**
	 * Configuration properties of the Coherence Server.
	 */
	public static class ServerProperties {

		/**
		 * Overrides the default startup-timeout when starting Coherence. If not set, defaults to
		 * {@link com.oracle.coherence.spring.CoherenceServer#DEFAULT_STARTUP_TIMEOUT_MILLIS}.
		 */
		private Duration startupTimeout;

		public Duration getStartupTimeout() {
			return this.startupTimeout;
		}

		public void setStartupTimeout(Duration startupTimeout) {
			this.startupTimeout = startupTimeout;
		}
	}
}
