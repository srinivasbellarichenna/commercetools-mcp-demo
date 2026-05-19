package com.example.cart.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.cart.Cart;
import io.vrap.rmf.base.client.ApiHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProjectApiRoot apiRoot;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart mockCart;
    private CompletableFuture<ApiHttpResponse<Cart>> mockFuture;

    @BeforeEach
    void setUp() {
        mockCart = org.mockito.Mockito.mock(Cart.class);
        ApiHttpResponse<Cart> apiResponse = new ApiHttpResponse<>(200, null, mockCart);
        mockFuture = CompletableFuture.completedFuture(apiResponse);
    }

    @Test
    void testGetCartById() {
        String cartId = "test-cart-id";
        when(apiRoot.carts().withId(cartId).get().execute()).thenReturn(mockFuture);

        StepVerifier.create(cartService.getCartById(cartId))
            .expectNext(mockCart)
            .verifyComplete();
    }

    @Test
    void testCreateCart() {
        when(apiRoot.carts().post(any(com.commercetools.api.models.cart.CartDraft.class)).execute()).thenReturn(mockFuture);
        
        StepVerifier.create(cartService.createCart("EUR", "DE"))
            .expectNext(mockCart)
            .verifyComplete();
    }

    @Test
    void testAddLineItemToCart() {
        String cartId = "test-cart-id";
        when(mockCart.getVersion()).thenReturn(1L);
        when(apiRoot.carts().withId(cartId).get().execute()).thenReturn(mockFuture);
        when(apiRoot.carts().withId(cartId).post(any(com.commercetools.api.models.cart.CartUpdate.class)).execute()).thenReturn(mockFuture);

        StepVerifier.create(cartService.addLineItemToCart(cartId, "SKU-123", 2L))
            .expectNext(mockCart)
            .verifyComplete();
    }
}
