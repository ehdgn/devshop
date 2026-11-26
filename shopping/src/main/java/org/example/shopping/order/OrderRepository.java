package org.example.shopping.order;

import org.example.shopping.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findAllByUserIdAndOrderByCreatedAtDesc(@Param("userId") Long userId);

    Optional<Order> findByUserId(Long userId);

    // 검색 결과에는 키워드와 맞는 주문 상품만 담긴다
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.orderItems oi " +
            "WHERE o.user.id = :userId " +
            "AND LOWER(oi.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY o.createdAt DESC ")
    List<Order> findByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
}
