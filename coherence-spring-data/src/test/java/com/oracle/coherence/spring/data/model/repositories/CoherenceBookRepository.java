package com.oracle.coherence.spring.data.model.repositories;

import com.oracle.coherence.spring.data.config.CoherenceMap;
import com.oracle.coherence.spring.data.model.Book;
import com.oracle.coherence.spring.data.repository.CoherenceRepository;
import com.tangosol.util.UUID;

@CoherenceMap("book")
public interface CoherenceBookRepository extends CoherenceRepository<Book, UUID> {
}
