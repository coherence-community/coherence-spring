/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests.data;

import com.oracle.coherence.spring.data.repository.CoherenceRepository;

/**
 * A {@code Coherence}-bases repository for working with {@link Task tasks}.
 * @author Gunnar Hillert
 */
@com.oracle.coherence.spring.data.config.CoherenceMap("tasks")
public interface SpringDataTaskRepository extends CoherenceRepository<Task, String> {
}

