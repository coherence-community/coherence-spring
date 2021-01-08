/*
 * Copyright 2017-2020 original authors
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
package com.oracle.coherence.spring.configuration;

import com.tangosol.net.SessionConfiguration;

import java.util.Optional;

/**
 * A provider of {@link com.tangosol.net.SessionConfiguration} instances.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
public interface SessionConfigurationProvider {

    /**
     * Returns the optional {@link SessionConfiguration} that is provider provides.
     *
     * @return  the optional {@link SessionConfiguration} that is provider provides
     *          or an empty {@link Optional} if this provider cannot provide a
     *          configuration
     */
    Optional<SessionConfiguration> getConfiguration();
}
