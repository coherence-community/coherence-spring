/*
 * Copyright 2021-2023 original authors
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
package com.oracle.coherence.spring.configuration.session;

/**
 * Creates an instance of {@link com.tangosol.net.SessionConfiguration} that will be
 * created for each named session in the application configuration properties.
 * <p>
 * Instances of {@link com.tangosol.net.SessionConfiguration} are used for both Coherence*Extend configurations and
 * (since 4.0) for Grpc configurations.
 * <p>
 * Sessions are configured with the {@code coherence.session} prefix,
 * for example {@code coherence.session.foo} configures a session named
 * foo.
 * <p>
 * The session name {@code default} is a special case that configures
 * the default session named {@link com.tangosol.net.Coherence#DEFAULT_NAME}.
 * @author Gunnar Hillert
 * @since 3.0
 * @see com.tangosol.net.SessionConfiguration
 */
public class ClientSessionConfigurationBean extends SessionConfigurationBean {
	public ClientSessionConfigurationBean() {
		super.setType(SessionType.CLIENT);
	}
}
