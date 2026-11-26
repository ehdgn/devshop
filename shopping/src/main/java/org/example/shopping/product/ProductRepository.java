package org.example.shopping.product;

import org.example.shopping.product.productEnum.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 재고 확인과 차감을 쿼리 하나로: 동시에 결제해도 재고가 음수가 되지 않는다 (0이면 재고 부족)
    @Modifying(flushAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity, " +
            "p.minusQuantity = p.minusQuantity + :quantity " +
            "WHERE p.id = :id AND p.stockQuantity >= :quantity")
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);


    @Query("SELECT p FROM Product p JOIN FETCH p.owner o WHERE o.id = :id ORDER BY p.createdAt DESC")
    List<Product> findAllWithUser(@Param("id") Long ownerId);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(@Param("id") Long id);

    // 상태별 조회
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.status = :status")
    List<Product> findByStatusWithCategory(@Param("status") ProductStatus status);

    // 카테고리별 조회
    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.category.id = :categoryId")
    List<Product> findByCategoryIdWithCategory(@Param("categoryId") Long categoryId);

    // Owner별 상품명 검색
    @Query("SELECT p FROM Product p JOIN FETCH p.category JOIN FETCH p.owner o " +
            "WHERE o.id = :ownerId AND p.productName LIKE %:keyword% ORDER BY p.createdAt DESC")
    List<Product> searchByOwnerAndProductName(@Param("ownerId") Long ownerId, @Param("keyword") String keyword);

    // Owner별 상품명 + 상태 검색
    @Query("SELECT p FROM Product p JOIN FETCH p.category JOIN FETCH p.owner o " +
            "WHERE o.id = :ownerId AND p.productName LIKE %:keyword% AND p.status = :status ORDER BY p.createdAt DESC")
    List<Product> searchByOwnerAndProductNameAndStatus(@Param("ownerId") Long ownerId,
                                                       @Param("keyword") String keyword,
                                                       @Param("status") ProductStatus status);

    // Owner별 상품명 + 카테고리 검색
    @Query("SELECT p FROM Product p JOIN FETCH p.category JOIN FETCH p.owner o " +
            "WHERE o.id = :ownerId AND p.productName LIKE %:keyword% AND p.category.id = :categoryId ORDER BY p.createdAt DESC")
    List<Product> searchByOwnerAndProductNameAndCategory(@Param("ownerId") Long ownerId,
                                                         @Param("keyword") String keyword,
                                                         @Param("categoryId") Long categoryId);

    List<Product> findByStatusAndCategoryIdOrderByCreatedAtDesc(ProductStatus status, Long categoryId);

    long countByOwnerId(Long ownerId);

    Optional<Product> findByOwnerId(Long ownerId);

    @Query("SELECT p FROM Product p JOIN FETCH p.owner WHERE p.status = :status AND p.owner.id = :id")
    List<Product> findByStatusWithUser(@Param("status") ProductStatus status, @Param("id") Long ownerId);


    @Query("SELECT p FROM Product p JOIN FETCH p.category JOIN FETCH p.owner WHERE p.status = :status ORDER BY p.createdAt DESC")
    List<Product> findByStatusWithCategoryOrderByCreatedAtDesc(@Param("status") ProductStatus status);



    // ==================== ADMIN 쿼리 ====================

    /**
     * 관리자 - 전체 상품 조회 (카테고리, Owner 포함)
     */
    @Query("SELECT p FROM Product p " +
            "JOIN FETCH p.category " +
            "JOIN FETCH p.owner o " +
            "JOIN FETCH o.user " +
            "ORDER BY p.createdAt DESC")
    List<Product> findAllWithCategoryAndOwner();


    // 관리자 - 상품명 검색 (카테고리, Owner 포함)

    @Query("SELECT p FROM Product p " +
            "JOIN FETCH p.category " +
            "JOIN FETCH p.owner o " +
            "JOIN FETCH o.user " +
            "WHERE p.productName LIKE %:keyword% " +
            "ORDER BY p.createdAt DESC")
    List<Product> searchByProductNameWithCategoryAndOwner(@Param("keyword") String keyword);


     // 관리자 - 상품 단건 조회 (카테고리, Owner 포함)

    @Query("SELECT p FROM Product p " +
            "JOIN FETCH p.category " +
            "JOIN FETCH p.owner o " +
            "JOIN FETCH o.user " +
            "WHERE p.id = :id")
    Optional<Product> findByIdWithCategoryAndOwner(@Param("id") Long id);
}


