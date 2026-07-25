
package com.dev.controllers;

import com.dev.objects.Bid;
import com.dev.objects.Product;
import com.dev.objects.User;
import com.dev.responses.BasicResponse;
import com.dev.utils.Definitions;
import com.dev.utils.Errors;
import com.dev.utils.Persist;
import com.dev.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FeaturesControllerTest {

    private static final String TOKEN = "valid-token";
    private static final Integer USER_ID = 10;
    private static final Integer PRODUCT_ID = 20;

    private FeaturesController controller;
    private Persist persist;
    private Utils utils;
    private LiveUpdatesController liveUpdatesController;

    @BeforeEach
    void setUp() {
        controller = spy(new FeaturesController());
        persist = mock(Persist.class);
        utils = mock(Utils.class);
        liveUpdatesController = mock(LiveUpdatesController.class);

        ReflectionTestUtils.setField(controller, "persist", persist);
        ReflectionTestUtils.setField(controller, "utils", utils);
        ReflectionTestUtils.setField(
                controller,
                "liveUpdatesController",
                liveUpdatesController
        );

        doReturn(new BasicResponse(true, null))
                .when(controller)
                .basicValidation(TOKEN, USER_ID);
    }

    @Test
    void placeBid_shouldSucceed_whenOfferIsValid() {
        int offer = 150;
        int currentCredit = 1000;

        Product product = mock(Product.class);
        User seller = mock(User.class);
        User buyer = mock(User.class);

        when(persist.productIsExist(PRODUCT_ID)).thenReturn(product);
        when(product.getSellerUser()).thenReturn(seller);
        when(seller.getId()).thenReturn(99);
        when(product.isOpenForSale()).thenReturn(true);

        when(persist.getUserCredit(TOKEN, USER_ID))
                .thenReturn(currentCredit);

        when(persist.getBiggestBidOnProduct(USER_ID, PRODUCT_ID))
                .thenReturn(null);

        when(product.getStartingPrice()).thenReturn(100);
        when(persist.getUserById(USER_ID)).thenReturn(buyer);
        when(buyer.getUsername()).thenReturn("buyer");

        BasicResponse response = controller.placeBid(
                TOKEN,
                USER_ID,
                PRODUCT_ID,
                offer
        );

        assertTrue(response.isSuccess());
        assertNull(response.getErrorCode());

        verify(persist).placeBid(
                any(Bid.class),
                eq(USER_ID),
                eq(currentCredit - offer - Definitions.BID_COST_FEE)
        );

        verify(persist).addToSystemCredit(Definitions.BID_COST_FEE);

        verify(liveUpdatesController)
                .sendPlaceBidEvent(99, "buyer");

        verify(liveUpdatesController)
                .sendStatsEvent();
    }

    @Test
    void placeBid_shouldRejectOfferThatIsTooLow() {
        int offer = 150;

        Product product = mock(Product.class);
        User seller = mock(User.class);
        User buyer = mock(User.class);

        when(persist.productIsExist(PRODUCT_ID)).thenReturn(product);
        when(product.getSellerUser()).thenReturn(seller);
        when(seller.getId()).thenReturn(99);
        when(product.isOpenForSale()).thenReturn(true);

        when(persist.getUserCredit(TOKEN, USER_ID))
                .thenReturn(1000);

        when(persist.getBiggestBidOnProduct(USER_ID, PRODUCT_ID))
                .thenReturn(200);

        when(persist.getUserById(USER_ID)).thenReturn(buyer);

        BasicResponse response = controller.placeBid(
                TOKEN,
                USER_ID,
                PRODUCT_ID,
                offer
        );

        assertFalse(response.isSuccess());
        assertEquals(
                Errors.ERROR_OFFER_LOW,
                response.getErrorCode()
        );

        verify(persist, never())
                .placeBid(any(Bid.class), anyInt(), anyInt());
    }

    @Test
    void placeBid_shouldRejectBidWhenCreditIsInsufficient() {
        int offer = 500;

        Product product = mock(Product.class);
        User seller = mock(User.class);

        when(persist.productIsExist(PRODUCT_ID)).thenReturn(product);
        when(product.getSellerUser()).thenReturn(seller);
        when(seller.getId()).thenReturn(99);
        when(product.isOpenForSale()).thenReturn(true);

        when(persist.getUserCredit(TOKEN, USER_ID))
                .thenReturn(100);

        BasicResponse response = controller.placeBid(
                TOKEN,
                USER_ID,
                PRODUCT_ID,
                offer
        );

        assertFalse(response.isSuccess());
        assertEquals(
                Errors.ERROR_NOT_ENOUGH_CREDIT,
                response.getErrorCode()
        );

        verify(persist, never())
                .placeBid(any(Bid.class), anyInt(), anyInt());
    }

    @Test
    void placeBid_shouldRejectBidOnOwnProduct() {
        Product product = mock(Product.class);
        User seller = mock(User.class);

        when(persist.productIsExist(PRODUCT_ID)).thenReturn(product);
        when(product.getSellerUser()).thenReturn(seller);
        when(seller.getId()).thenReturn(USER_ID);

        BasicResponse response = controller.placeBid(
                TOKEN,
                USER_ID,
                PRODUCT_ID,
                150
        );

        assertFalse(response.isSuccess());
        assertEquals(
                Errors.ERROR_BID_ON_YOUR_PRODUCT,
                response.getErrorCode()
        );
    }

    @Test
    void placeBid_shouldRejectProductThatIsClosed() {
        Product product = mock(Product.class);
        User seller = mock(User.class);

        when(persist.productIsExist(PRODUCT_ID)).thenReturn(product);
        when(product.getSellerUser()).thenReturn(seller);
        when(seller.getId()).thenReturn(99);
        when(product.isOpenForSale()).thenReturn(false);

        BasicResponse response = controller.placeBid(
                TOKEN,
                USER_ID,
                PRODUCT_ID,
                150
        );

        assertFalse(response.isSuccess());
        assertEquals(
                Errors.ERROR_PRODUCT_NOT_ON_SALE,
                response.getErrorCode()
        );
    }

    @Test
    void placeBid_shouldRejectUnknownProduct() {
        when(persist.productIsExist(PRODUCT_ID))
                .thenReturn(null);

        BasicResponse response = controller.placeBid(
                TOKEN,
                USER_ID,
                PRODUCT_ID,
                150
        );

        assertFalse(response.isSuccess());
        assertEquals(
                Errors.ERROR_PRODUCT_DOESNT_EXIST,
                response.getErrorCode()
        );
    }

    @Test
    void closeAuction_shouldRejectAuctionWithTooFewBids() {
        Product product = mock(Product.class);
        User seller = mock(User.class);
        Bid bid = mock(Bid.class);

        when(persist.productIsExist(PRODUCT_ID)).thenReturn(product);
        when(product.getSellerUser()).thenReturn(seller);
        when(seller.getId()).thenReturn(USER_ID);

        int tooFewBids =
                Math.max(0, Definitions.MIN_BIDS_FOR_CLOSE_AUCTION - 1);

        if (tooFewBids == 0) {
            when(persist.getBidsByProductIdBidDateAsc(PRODUCT_ID))
                    .thenReturn(Collections.emptyList());
        } else {
            Bid[] bids = new Bid[tooFewBids];
            Arrays.fill(bids, bid);

            when(persist.getBidsByProductIdBidDateAsc(PRODUCT_ID))
                    .thenReturn(Arrays.asList(bids));
        }

        BasicResponse response = controller.closeAuction(
                TOKEN,
                USER_ID,
                PRODUCT_ID
        );

        assertFalse(response.isSuccess());
        assertEquals(
                Errors.PRODUCT_HASNT_ENOUGH_BIDS_FOR_CLOSE,
                response.getErrorCode()
        );

        verify(persist, never())
                .closeAuction(PRODUCT_ID);
    }

    @Test
    void closeAuction_shouldRejectUserWhoDoesNotOwnProduct() {
        Product product = mock(Product.class);
        User seller = mock(User.class);

        when(persist.productIsExist(PRODUCT_ID)).thenReturn(product);
        when(product.getSellerUser()).thenReturn(seller);
        when(seller.getId()).thenReturn(999);

        BasicResponse response = controller.closeAuction(
                TOKEN,
                USER_ID,
                PRODUCT_ID
        );

        assertFalse(response.isSuccess());
        assertEquals(
                Errors.ERROR_USER_DOESNT_OWNER,
                response.getErrorCode()
        );

        verify(persist, never())
                .closeAuction(PRODUCT_ID);
    }
}
