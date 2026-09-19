package com.demo.infrastructure.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaOrderJpaRepository extends JpaRepository<JpaOrderEntity, String> {
    List<JpaOrderEntity> findByCustomerId(String customerId);
}
