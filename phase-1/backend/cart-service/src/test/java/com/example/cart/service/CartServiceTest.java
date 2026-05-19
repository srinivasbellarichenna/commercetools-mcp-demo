package com.example.cart.service;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.cart.Cart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProjectApiRoot apiRoot;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart mockCart;

    @BeforeEach
    void setUp() {
        mockCart = org.mockito.Mockito.mock(Cart.class);
    }

    @Test
    void testGetCartById() {
        String cartId = "test-cart-id";
        when(apiRoot.carts().withId(cartId).get().executeBlocking().getBody()).thenReturn(mockCart);

        Cart result = cartService.getCartById(cartId);

        assertNotNull(result);
        assertEquals(mockCart, result);
    }

    @Test
    void testCreateCart() {
        when(apiRoot.carts().post(any(com.commercetools.api.models.cart.CartDraft.class)).executeBlocking().getBody()).thenReturn(mockCart);
        
        Cart result = cartService.createCart("EUR", "DE");
        
        assertNotNull(result);
        assertEquals(mockCart, result);
    }

    @Test
    void testAddLineItemToCart() {
        String cartId = "test-cart-id";
        when(mockCart.getVersion()).thenReturn(1L);
        when(apiRoot.carts().withId(cartId).get().executeBlocking().getBody()).thenReturn(mockCart);
        when(apiRoot.carts().withId(cartId).post(any(com.commercetools.api.models.cart.CartUpdate.class)).executeBlocking().getBody()).thenReturn(mockCart);

        Cart result = cartService.addLineItemToCart(cartId, "SKU-123", 2L);

        assertNotNull(result);
        assertEquals(mockCart, result);
    }
}
