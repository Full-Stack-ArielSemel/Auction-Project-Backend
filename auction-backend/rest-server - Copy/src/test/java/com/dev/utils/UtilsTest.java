package com.dev.utils;

import com.dev.models.MyProductsModel;
import com.dev.objects.Bid;
import com.dev.objects.Product;
import com.dev.objects.User;
import com.dev.pojo.TotalBidsCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.dev.utils.Definitions.MINIMAL_PASSWORD_LENGTH;
import static com.dev.utils.Definitions.MINIMAL_USERNAME_LENGTH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UtilsTest {

    private Utils utils;

    @BeforeEach
    void setUp() {
        utils = new Utils();
    }

    @Test
    void createHash_shouldReturnSameHashForSameCredentials() {
        String username = "ariel";
        String password = "StrongPass1";

        String firstHash = utils.createHash(username, password);
        String secondHash = utils.createHash(username, password);

        assertNotNull(firstHash);
        assertEquals(firstHash, secondHash);
    }

    @Test
    void createHash_shouldReturnDifferentHashForDifferentPasswords() {
        String username = "ariel";

        String firstHash = utils.createHash(username, "Password1");
        String secondHash = utils.createHash(username, "Password2");

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void createHash_shouldReturnDifferentHashForDifferentUsernames() {
        String password = "StrongPass1";

        String firstHash = utils.createHash("ariel", password);
        String secondHash = utils.createHash("otherUser", password);

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void isStrongPassword_shouldReturnTrue_whenPasswordMeetsMinimumLength() {
        String password = "a".repeat(MINIMAL_PASSWORD_LENGTH);

        boolean result = utils.isStrongPassword(password);

        assertTrue(result);
    }

    @Test
    void isStrongPassword_shouldReturnFalse_whenPasswordIsTooShort() {
        String password = "a".repeat(MINIMAL_PASSWORD_LENGTH - 1);

        boolean result = utils.isStrongPassword(password);

        assertFalse(result);
    }

    @Test
    void isValidUsername_shouldReturnTrue_whenUsernameMeetsMinimumLength() {
        String username = "a".repeat(MINIMAL_USERNAME_LENGTH);

        boolean result = utils.isValidUsername(username);

        assertTrue(result);
    }

    @Test
    void isValidUsername_shouldReturnFalse_whenUsernameIsTooShort() {
        String username = "a".repeat(MINIMAL_USERNAME_LENGTH - 1);

        boolean result = utils.isValidUsername(username);

        assertFalse(result);
    }

    @Test
    void calculateBiggestBids_shouldReturnHighestBidForProduct() {
        Product product = mock(Product.class);
        Bid firstBid = mock(Bid.class);
        Bid secondBid = mock(Bid.class);

        when(product.getId()).thenReturn(1);
        when(product.getStartingPrice()).thenReturn(100);

        when(firstBid.getProduct()).thenReturn(product);
        when(firstBid.getOffer()).thenReturn(150);

        when(secondBid.getProduct()).thenReturn(product);
        when(secondBid.getOffer()).thenReturn(250);

        List<MyProductsModel> result = utils.calculateBiggestBids(
                Collections.singletonList(product),
                Arrays.asList(firstBid, secondBid)
        );

        assertEquals(1, result.size());
        assertEquals(250, result.get(0).getBiggestBid());
    }

    @Test
    void calculateBiggestBids_shouldReturnNull_whenProductHasNoBidsAboveStartingPrice() {
        Product product = mock(Product.class);

        when(product.getId()).thenReturn(1);
        when(product.getStartingPrice()).thenReturn(100);

        List<MyProductsModel> result = utils.calculateBiggestBids(
                Collections.singletonList(product),
                Collections.emptyList()
        );

        assertEquals(1, result.size());
        assertNull(result.get(0).getBiggestBid());
    }

    @Test
    void calculateBidsStatusMap_shouldMarkAllBidsFalse_whenThereAreNoWinningProducts() {
        Bid firstBid = mock(Bid.class);
        Bid secondBid = mock(Bid.class);

        Map<Bid, Boolean> result = utils.calculateBidsStatusMap(
                Arrays.asList(firstBid, secondBid),
                Collections.emptyList()
        );

        assertEquals(2, result.size());
        assertFalse(result.get(firstBid));
        assertFalse(result.get(secondBid));
    }

    @Test
    void calculateBidsStatusMap_shouldMarkHighestBidAsWinner() {
        Product product = mock(Product.class);

        Bid lowerBid = mock(Bid.class);
        Bid higherBid = mock(Bid.class);

        when(product.getId()).thenReturn(1);

        when(lowerBid.getProduct()).thenReturn(product);
        when(lowerBid.getOffer()).thenReturn(150);

        when(higherBid.getProduct()).thenReturn(product);
        when(higherBid.getOffer()).thenReturn(250);

        Map<Bid, Boolean> result = utils.calculateBidsStatusMap(
                Arrays.asList(lowerBid, higherBid),
                Collections.singletonList(product)
        );

        assertFalse(result.get(lowerBid));
        assertTrue(result.get(higherBid));
    }

    @Test
    void calculateProductsBidsMap_shouldCountUserAndTotalBidsCorrectly() {
        Integer currentUserId = 10;

        Product product = mock(Product.class);

        User currentUser = mock(User.class);
        User anotherUser = mock(User.class);

        Bid currentUserBid1 = mock(Bid.class);
        Bid currentUserBid2 = mock(Bid.class);
        Bid anotherUserBid = mock(Bid.class);

        when(product.getId()).thenReturn(1);

        when(currentUser.getId()).thenReturn(currentUserId);
        when(anotherUser.getId()).thenReturn(20);

        when(currentUserBid1.getProduct()).thenReturn(product);
        when(currentUserBid1.getBuyerUser()).thenReturn(currentUser);

        when(currentUserBid2.getProduct()).thenReturn(product);
        when(currentUserBid2.getBuyerUser()).thenReturn(currentUser);

        when(anotherUserBid.getProduct()).thenReturn(product);
        when(anotherUserBid.getBuyerUser()).thenReturn(anotherUser);

        Map<Product, TotalBidsCounter> result =
                utils.calculateProductsBidsMap(
                        Collections.singletonList(product),
                        Arrays.asList(
                                currentUserBid1,
                                currentUserBid2,
                                anotherUserBid
                        ),
                        currentUserId
                );

        TotalBidsCounter counter = result.get(product);

        assertNotNull(counter);
        assertEquals(2, counter.getUserTotalBids());
        assertEquals(3, counter.getAllUsersTotalBids());
    }

    @Test
    void calculateProductsBidsMap_shouldReturnZeroCounters_whenProductHasNoBids() {
        Product product = mock(Product.class);

        when(product.getId()).thenReturn(1);

        Map<Product, TotalBidsCounter> result =
                utils.calculateProductsBidsMap(
                        Collections.singletonList(product),
                        Collections.emptyList(),
                        10
                );

        TotalBidsCounter counter = result.get(product);

        assertNotNull(counter);
        assertEquals(0, counter.getUserTotalBids());
        assertEquals(0, counter.getAllUsersTotalBids());
    }
}
