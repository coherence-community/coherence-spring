/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oracle.coherence.spring.boot.autoconfigure.support;

/**
 * An enum representing the different Logging types for Coherence.
 *
 * @author Gunnar Hillert
 */
public enum LogType {

	/**
	 * Log to Standard Out.
	 */
	STDOUT("stdout"),

	/**
	 * Log to Standard Error.
	 */
	STDERR("stderr"),

	/**
	 * Log using Java's logging facility.
	 */
	JDK("jdk"),

	/**
	 * Log using Log4J.
	 */
	LOG4J("log4j"),

	/**
	 * Log using Log4J2.
	 */
	LOG4J2("log4j2"),

	/**
	 * Log using Simple Logging Facade for Java.
	 */
	SLF4J("slf4j"),

	/**
	 * Log directly to a log file.
	 */
	FILENAME("file_name");

	private String key;

	LogType(String key) {
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}
}
