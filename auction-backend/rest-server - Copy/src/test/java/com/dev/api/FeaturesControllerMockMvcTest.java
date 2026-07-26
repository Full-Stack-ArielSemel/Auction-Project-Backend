package com.dev.api;

import com.dev.controllers.FeaturesController;
import com.dev.controllers.LiveUpdatesController;
import com.dev.objects.Product;
import com.dev.objects.User;
import com.dev.utils.Persist;
import com.dev.utils.Utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.dev.utils.Errors.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FeaturesControllerMockMvcTest {

    private static final String TOKEN = "valid-token";
    private static final int USER_ID = 10;
    private static final int PRODUCT_ID = 20;

    private MockMvc mockMvc;

    private Persist persist;
    private Utils utils;
    private LiveUpdatesController liveUpdatesController;

    @BeforeEach
    void setUp() {
        FeaturesController controller = new FeaturesController();

        persist = mock(Persist.class);
        utils = mock(Utils.class);
        liveUpdatesController = mock(LiveUpdatesController.class);

        /*
         * FeaturesController has its own Persist field.
         */
        ReflectionTestUtils.setField(
                controller,
                "persist",
                persist
        );

        ReflectionTestUtils.setField(
                controller,
                "utils",
                utils
        );

        ReflectionTestUtils.setField(
                controller,
                "liveUpdatesController",
                liveUpdatesController
        );

        /*
         * MainController, which FeaturesController extends,
         * also has its own Persist field.
         *
         * basicValidation() and userHasPermissions()
         * use this field.
         */
        ReflectionTestUtils.setField(
                controller,
                "persist",
                persist,
                com.dev.controllers.MainController.class
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    private void allowUser() {
        when(persist.userHasPermissions(USER_ID, TOKEN))
                .thenReturn(true);
    }

    @Test
    void placeBid_shouldReturnSuccess_whenBidIsValid()
            throws Exception {

        allowUser();

        Product product = mock(Product.class);
        User seller = mock(User.class);
        User buyer = mock(User.class);

        when(persist.productIsExist(PRODUCT_ID))
                .thenReturn(product);

        when(product.getSellerUser())
                .thenReturn(seller);

        when(seller.getId())
                .thenReturn(99);

        when(product.isOpenForSale())
                .thenReturn(true);

        when(persist.getUserCredit(TOKEN, USER_ID))
                .thenReturn(1000);

        when(persist.getBiggestBidOnProduct(USER_ID, PRODUCT_ID))
                .thenReturn(200);

        when(persist.getUserById(USER_ID))
                .thenReturn(buyer);

        when(buyer.getUsername())
                .thenReturn("buyer");

        mockMvc.perform(
                        post("/place-bid")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                                .param(
                                        "productId",
                                        String.valueOf(PRODUCT_ID)
                                )
                                .param("offer", "250")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                );

        verify(persist).placeBid(
                any(),
                eq(USER_ID),
                eq(749)
        );

        verify(liveUpdatesController)
                .sendPlaceBidEvent(
                        99,
                        "buyer"
                );
    }

    @Test
    void placeBid_shouldRejectBid_whenOfferIsTooLow()
            throws Exception {

        allowUser();

        Product product = mock(Product.class);
        User seller = mock(User.class);
        User buyer = mock(User.class);

        when(persist.productIsExist(PRODUCT_ID))
                .thenReturn(product);

        when(product.getSellerUser())
                .thenReturn(seller);

        when(seller.getId())
                .thenReturn(99);

        when(product.isOpenForSale())
                .thenReturn(true);

        when(persist.getUserCredit(TOKEN, USER_ID))
                .thenReturn(1000);

        when(persist.getBiggestBidOnProduct(USER_ID, PRODUCT_ID))
                .thenReturn(300);

        when(persist.getUserById(USER_ID))
                .thenReturn(buyer);

        mockMvc.perform(
                        post("/place-bid")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                                .param(
                                        "productId",
                                        String.valueOf(PRODUCT_ID)
                                )
                                .param("offer", "250")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(ERROR_OFFER_LOW)
                );

        verify(
                persist,
                never()
        ).placeBid(
                any(),
                anyInt(),
                anyInt()
        );
    }

    @Test
    void placeBid_shouldRejectBid_whenCreditIsInsufficient()
            throws Exception {

        allowUser();

        Product product = mock(Product.class);
        User seller = mock(User.class);

        when(persist.productIsExist(PRODUCT_ID))
                .thenReturn(product);

        when(product.getSellerUser())
                .thenReturn(seller);

        when(seller.getId())
                .thenReturn(99);

        when(product.isOpenForSale())
                .thenReturn(true);

        when(persist.getUserCredit(TOKEN, USER_ID))
                .thenReturn(100);

        mockMvc.perform(
                        post("/place-bid")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                                .param(
                                        "productId",
                                        String.valueOf(PRODUCT_ID)
                                )
                                .param("offer", "500")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(ERROR_NOT_ENOUGH_CREDIT)
                );

        verify(
                persist,
                never()
        ).placeBid(
                any(),
                anyInt(),
                anyInt()
        );
    }

    @Test
    void placeBid_shouldRejectBid_whenUserOwnsProduct()
            throws Exception {

        allowUser();

        Product product = mock(Product.class);
        User seller = mock(User.class);

        when(persist.productIsExist(PRODUCT_ID))
                .thenReturn(product);

        when(product.getSellerUser())
                .thenReturn(seller);

        when(seller.getId())
                .thenReturn(USER_ID);

        mockMvc.perform(
                        post("/place-bid")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                                .param(
                                        "productId",
                                        String.valueOf(PRODUCT_ID)
                                )
                                .param("offer", "250")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(ERROR_BID_ON_YOUR_PRODUCT)
                );

        verify(
                persist,
                never()
        ).placeBid(
                any(),
                anyInt(),
                anyInt()
        );
    }

    @Test
    void placeBid_shouldRejectBid_whenProductIsClosed()
            throws Exception {

        allowUser();

        Product product = mock(Product.class);
        User seller = mock(User.class);

        when(persist.productIsExist(PRODUCT_ID))
                .thenReturn(product);

        when(product.getSellerUser())
                .thenReturn(seller);

        when(seller.getId())
                .thenReturn(99);

        when(product.isOpenForSale())
                .thenReturn(false);

        mockMvc.perform(
                        post("/place-bid")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                                .param(
                                        "productId",
                                        String.valueOf(PRODUCT_ID)
                                )
                                .param("offer", "250")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(ERROR_PRODUCT_NOT_ON_SALE)
                );
    }

    @Test
    void placeBid_shouldRejectBid_whenProductDoesNotExist()
            throws Exception {

        allowUser();

        when(persist.productIsExist(PRODUCT_ID))
                .thenReturn(null);

        mockMvc.perform(
                        post("/place-bid")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                                .param(
                                        "productId",
                                        String.valueOf(PRODUCT_ID)
                                )
                                .param("offer", "250")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(ERROR_PRODUCT_DOESNT_EXIST)
                );
    }

    @Test
    void placeBid_shouldReturnPermissionError_whenTokenIsInvalid()
            throws Exception {

        when(persist.userHasPermissions(USER_ID, TOKEN))
                .thenReturn(false);

        mockMvc.perform(
                        post("/place-bid")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                                .param(
                                        "productId",
                                        String.valueOf(PRODUCT_ID)
                                )
                                .param("offer", "250")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(PERMISSION_ERROR_CODE)
                );

        verify(
                persist,
                never()
        ).productIsExist(anyInt());
    }

    @Test
    void placeBid_shouldReturnMissingToken_whenTokenIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/place-bid")
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                                .param(
                                        "productId",
                                        String.valueOf(PRODUCT_ID)
                                )
                                .param("offer", "250")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(ERROR_MISSING_TOKEN)
                );
    }

    @Test
    void placeBid_shouldRejectGetRequest()
            throws Exception {

        mockMvc.perform(
                        get("/place-bid")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                                .param(
                                        "productId",
                                        String.valueOf(PRODUCT_ID)
                                )
                                .param("offer", "250")
                )
                .andExpect(
                        status().isMethodNotAllowed()
                );
    }
}
