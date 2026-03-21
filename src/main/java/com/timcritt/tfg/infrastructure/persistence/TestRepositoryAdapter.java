package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.application.port.outbound.TestRepositoryPort;
import com.timcritt.tfg.domain.model.TestItem;
import com.timcritt.tfg.infrastructure.persistence.jpa.TestJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.spring.TestJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// This class is the adapter that implements the TestRepositoryPort interface and uses Spring Data JPA to interact with the database.

@Repository
public class TestRepositoryAdapter implements TestRepositoryPort {

    private final TestJpaRepository jpaRepository;

    public TestRepositoryAdapter(TestJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<TestItem> findById(Long id) {
        return jpaRepository.findById(id).map(TestEntityMapper::toDomain);
    }

    @Override
    public TestItem save(TestItem item) {
        TestJpaEntity entity = TestEntityMapper.toEntity(item);
        TestJpaEntity saved = jpaRepository.save(entity);
        return TestEntityMapper.toDomain(saved);
    }
    @Override
    public Boolean delete(Long id) {
        jpaRepository.deleteById(id);
        return true;
    }
}
