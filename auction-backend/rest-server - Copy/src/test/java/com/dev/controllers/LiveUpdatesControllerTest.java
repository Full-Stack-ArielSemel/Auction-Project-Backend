package com.dev.controllers;

import com.dev.objects.User;
import com.dev.utils.Persist;
import com.dev.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LiveUpdatesControllerTest {

    private LiveUpdatesController controller;
    private Persist persist;
    private Utils utils;

    @BeforeEach
    void setUp() {
        controller = new LiveUpdatesController();
        persist = mock(Persist.class);
        utils = mock(Utils.class);

        ReflectionTestUtils.setField(controller, "persist", persist);
        ReflectionTestUtils.setField(controller, "utils", utils);
    }

    @Test
    void mainPageHandler_shouldReturnEmitter_whenTokenBelongsToUser() {
        String token = "valid-token";

        User user = mock(User.class);

        when(user.getId()).thenReturn(10);
        when(persist.getUserByToken(token)).thenReturn(user);

        SseEmitter emitter = controller.mainPageHandler(token);

        assertNotNull(emitter);
        verify(persist).getUserByToken(token);
    }

    @Test
    void mainPageHandler_shouldStoreEmitterForUser() {
        String token = "valid-token";
        int userId = 10;

        User user = mock(User.class);

        when(user.getId()).thenReturn(userId);
        when(persist.getUserByToken(token)).thenReturn(user);

        SseEmitter emitter = controller.mainPageHandler(token);

        Map<Integer, SseEmitter> emitterMap =
                (Map<Integer, SseEmitter>) ReflectionTestUtils.getField(
                        controller,
                        "mainPageEmitterMap"
                );

        assertNotNull(emitterMap);
        assertTrue(emitterMap.containsKey(userId));
        assertSame(emitter, emitterMap.get(userId));
    }

    @Test
    void mainPageHandler_shouldReturnNull_whenTokenDoesNotBelongToUser() {
        String token = "invalid-token";

        when(persist.getUserByToken(token)).thenReturn(null);

        SseEmitter emitter = controller.mainPageHandler(token);

        assertNull(emitter);
        verify(persist).getUserByToken(token);
    }

    @Test
    void mainPageHandler_shouldNotStoreEmitter_whenUserDoesNotExist() {
        String token = "invalid-token";

        when(persist.getUserByToken(token)).thenReturn(null);

        controller.mainPageHandler(token);

        Map<Integer, SseEmitter> emitterMap =
                (Map<Integer, SseEmitter>) ReflectionTestUtils.getField(
                        controller,
                        "mainPageEmitterMap"
                );

        assertNotNull(emitterMap);
        assertTrue(emitterMap.isEmpty());
    }

    @Test
    void loginPageHandler_shouldReturnNewEmitter() {
        SseEmitter emitter = controller.loginPageHandler();

        assertNotNull(emitter);
    }

    @Test
    void loginPageHandler_shouldAddEmitterToLoginList() {
        SseEmitter emitter = controller.loginPageHandler();

        List<SseEmitter> emitterList =
                (List<SseEmitter>) ReflectionTestUtils.getField(
                        controller,
                        "loginEmitterList"
                );

        assertNotNull(emitterList);
        assertEquals(1, emitterList.size());
        assertSame(emitter, emitterList.get(0));
    }

    @Test
    void loginPageHandler_shouldCreateDifferentEmittersForDifferentConnections() {
        SseEmitter firstEmitter = controller.loginPageHandler();
        SseEmitter secondEmitter = controller.loginPageHandler();

        assertNotNull(firstEmitter);
        assertNotNull(secondEmitter);
        assertNotSame(firstEmitter, secondEmitter);

        List<SseEmitter> emitterList =
                (List<SseEmitter>) ReflectionTestUtils.getField(
                        controller,
                        "loginEmitterList"
                );

        assertEquals(2, emitterList.size());
    }

    @Test
    void sendStatsEvent_shouldNotThrow_whenNoLoginEmittersExist() {
        assertDoesNotThrow(() -> controller.sendStatsEvent());
    }

    @Test
    void sendPlaceBidEvent_shouldNotThrow_whenSellerHasNoEmitter() {
        assertDoesNotThrow(
                () -> controller.sendPlaceBidEvent(
                        10,
                        "buyerUser"
                )
        );
    }

    @Test
    void sendCloseAuctionEvent_shouldNotThrow_whenBidderListIsEmpty() {
        assertDoesNotThrow(
                () -> controller.sendCloseAuctionEvent(
                        "sellerUser",
                        Collections.emptyList()
                )
        );
    }

    @Test
    void sendCloseAuctionEvent_shouldNotThrow_whenBiddersHaveNoEmitters() {
        List<Integer> bidderIds = Arrays.asList(10, 20, 30);

        assertDoesNotThrow(
                () -> controller.sendCloseAuctionEvent(
                        "sellerUser",
                        bidderIds
                )
        );
    }

    @Test
    void multipleUsers_shouldReceiveDifferentMainPageEmitters() {
        User firstUser = mock(User.class);
        User secondUser = mock(User.class);

        when(firstUser.getId()).thenReturn(10);
        when(secondUser.getId()).thenReturn(20);

        when(persist.getUserByToken("token-1"))
                .thenReturn(firstUser);

        when(persist.getUserByToken("token-2"))
                .thenReturn(secondUser);

        SseEmitter firstEmitter =
                controller.mainPageHandler("token-1");

        SseEmitter secondEmitter =
                controller.mainPageHandler("token-2");

        assertNotNull(firstEmitter);
        assertNotNull(secondEmitter);
        assertNotSame(firstEmitter, secondEmitter);

        Map<Integer, SseEmitter> emitterMap =
                (Map<Integer, SseEmitter>) ReflectionTestUtils.getField(
                        controller,
                        "mainPageEmitterMap"
                );

        assertEquals(2, emitterMap.size());

        assertSame(
                firstEmitter,
                emitterMap.get(10)
        );

        assertSame(
                secondEmitter,
                emitterMap.get(20)
        );
    }
}
