package org.example.shopping.product;

import org.example.shopping._core.errors.exception.Exception403;
import org.example.shopping.users.User;
import org.example.shopping.users.owner.Owner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @InjectMocks ProductService productService;

    // 판매자(user id 1)가 등록한 상품
    private Product product() {
        User seller = User.builder().id(1L).build();
        Product product = new Product();
        product.setId(10L);
        product.setOwner(Owner.builder().id(100L).user(seller).build());
        return product;
    }

    @Test
    void 다른_판매자의_상품은_삭제할_수_없다() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product()));

        assertThrows(Exception403.class, () -> productService.deleteById(10L, 2L));
        verify(productRepository, never()).delete(any());
    }

    @Test
    void 다른_판매자의_상품은_수정할_수_없다() {
        Product product = product();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThrows(Exception403.class,
                () -> productService.updateById(10L, 2L, new ProductRequest.UpdateDTO(), null));
    }

    @Test
    void 본인_상품은_삭제할_수_있다() {
        Product product = product();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        productService.deleteById(10L, 1L);

        verify(productRepository).delete(product);
    }
}
