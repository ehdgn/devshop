package org.example.shopping.payment.log;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HistoryRepository extends JpaRepository<History, Long> {

    Optional<History> findByOrderItemId(Long orderItemId);
}
