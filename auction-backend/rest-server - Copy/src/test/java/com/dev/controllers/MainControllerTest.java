package com.dev.controllers;

import com.dev.objects.User;
import com.dev.responses.BasicResponse;
import com.dev.responses.UserResponse;
import com.dev.utils.Errors;
import com.dev.utils.Persist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MainControllerTest {

    private MainController controller;
    private Persist persist;

    @BeforeEach
    void setUp() {
        controller = new MainController();
        persist = mock(Persist.class);

        ReflectionTestUtils.setField(controller, "persist", persist);
    }

    @Test
    void getUserByToken_shouldReturnUser_whenTokenIsValid() {
        String token = "valid-token";

        User user = mock(User.class);
        when(user.getId()).thenReturn(10);
        when(user.getUsername()).thenReturn("ariel");
        when(user.getUserType()).thenReturn("user");

        when(persist.getUserByToken(token)).thenReturn(user);

        BasicResponse response = controller.getUserByToken(token);

        assertTrue(response.isSuccess());
        assertNull(response.getErrorCode());
        assertInstanceOf(UserResponse.class, response);

        verify(persist).getUserByToken(token);
    }

    @Test
    void getUserByToken_shouldReturnError_whenTokenIsInvalid() {
        String token = "invalid-token";

        when(persist.getUserByToken(token)).thenReturn(null);

        BasicResponse response = controller.getUserByToken(token);

        assertFalse(response.isSuccess());
        assertEquals(Errors.ERROR_NO_SUCH_TOKEN, response.getErrorCode());

        verify(persist).getUserByToken(token);
    }

    @Test
    void getUserByToken_shouldReturnMissingToken_whenTokenIsNull() {
        BasicResponse response = controller.getUserByToken(null);

        assertFalse(response.isSuccess());
        assertEquals(Errors.ERROR_MISSING_TOKEN, response.getErrorCode());

        verifyNoInteractions(persist);
    }

    @Test
    void isAdmin_shouldReturnSuccess_whenUserIsAdmin() {
        String token = "admin-token";

        User admin = mock(User.class);
        when(admin.getUserType()).thenReturn("admin");
        when(persist.getUserByToken(token)).thenReturn(admin);

        BasicResponse response = controller.isAdmin(token);

        assertTrue(response.isSuccess());
        assertNull(response.getErrorCode());

        verify(persist).getUserByToken(token);
    }

    @Test
    void isAdmin_shouldReturnError_whenUserIsNotAdmin() {
        String token = "user-token";

        User user = mock(User.class);
        when(user.getUserType()).thenReturn("user");
        when(persist.getUserByToken(token)).thenReturn(user);

        BasicResponse response = controller.isAdmin(token);

        assertFalse(response.isSuccess());
        assertEquals(Errors.ERROR_NO_PERMISSION, response.getErrorCode());

        verify(persist).getUserByToken(token);
    }

    @Test
    void isAdmin_shouldReturnError_whenTokenDoesNotBelongToUser() {
        String token = "unknown-token";

        when(persist.getUserByToken(token)).thenReturn(null);

        BasicResponse response = controller.isAdmin(token);

        assertFalse(response.isSuccess());
        assertEquals(Errors.ERROR_NO_SUCH_TOKEN, response.getErrorCode());

        verify(persist).getUserByToken(token);
    }
}
