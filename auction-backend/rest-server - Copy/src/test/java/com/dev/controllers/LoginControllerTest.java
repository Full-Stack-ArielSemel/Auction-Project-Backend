package com.dev.controllers;

import com.dev.objects.User;
import com.dev.responses.BasicResponse;
import com.dev.responses.LoginResponse;
import com.dev.utils.Errors;
import com.dev.utils.Persist;
import com.dev.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoginControllerTest {

    private LoginController controller;
    private Utils utils;
    private Persist persist;
    private LiveUpdatesController liveUpdatesController;

    @BeforeEach
    void setUp() {
        controller = new LoginController();
        utils = mock(Utils.class);
        persist = mock(Persist.class);
        liveUpdatesController = mock(LiveUpdatesController.class);

        ReflectionTestUtils.setField(controller, "utils", utils);
        ReflectionTestUtils.setField(controller, "persist", persist);
        ReflectionTestUtils.setField(controller, "liveUpdatesController", liveUpdatesController);
    }

    @Test
    void login_shouldReturnLoginResponse_whenCredentialsAreValid() {
        String username = "ariel";
        String password = "StrongPass1";
        String token = "generated-token";

        User user = mock(User.class);
        when(utils.createHash(username, password)).thenReturn(token);
        when(persist.getUserByUsernameAndToken(username, token)).thenReturn(user);
        when(user.getId()).thenReturn(7);
        when(user.getUserType()).thenReturn("user");

        BasicResponse response = controller.login(username, password);

        assertTrue(response.isSuccess());
        assertNull(response.getErrorCode());
        assertInstanceOf(LoginResponse.class, response);
        verify(persist).getUserByUsernameAndToken(username, token);
    }

    @Test
    void login_shouldReturnWrongCredentials_whenUserIsNotFound() {
        String username = "ariel";
        String password = "WrongPass1";
        String token = "generated-token";

        when(utils.createHash(username, password)).thenReturn(token);
        when(persist.getUserByUsernameAndToken(username, token)).thenReturn(null);

        BasicResponse response = controller.login(username, password);

        assertFalse(response.isSuccess());
        assertEquals(Errors.ERROR_WRONG_LOGIN_CREDS, response.getErrorCode());
    }

    @Test
    void login_shouldReturnMissingUsername_whenUsernameIsNull() {
        BasicResponse response = controller.login(null, "StrongPass1");

        assertFalse(response.isSuccess());
        assertEquals(Errors.ERROR_MISSING_USERNAME, response.getErrorCode());
        verifyNoInteractions(persist);
    }

    @Test
    void login_shouldReturnMissingPassword_whenPasswordIsNull() {
        BasicResponse response = controller.login("ariel", null);

        assertFalse(response.isSuccess());
        assertEquals(Errors.ERROR_MISSING_PASSWORD, response.getErrorCode());
        verifyNoInteractions(persist);
    }

    @Test
    void signUp_shouldCreateUser_whenInputIsValid() {
        String fullName = "Ariel Semel";
        String email = "ariel@example.com";
        String username = "ariel12";
        String password = "StrongPass1";
        String token = "hashed-password";

        when(utils.isValidUsername(username)).thenReturn(true);
        when(utils.isStrongPassword(password)).thenReturn(true);
        when(persist.getUserByUsername(username)).thenReturn(null);
        when(utils.createHash(username, password)).thenReturn(token);

        BasicResponse response = controller.signUp(
                fullName, email, username, password, password
        );

        assertTrue(response.isSuccess());
        assertNull(response.getErrorCode());
        verify(persist).saveUser(any(User.class));
        verify(liveUpdatesController).sendStatsEvent();
    }

    @Test
    void signUp_shouldRejectExistingUsername() {
        String username = "existingUser";
        String password = "StrongPass1";

        when(utils.isValidUsername(username)).thenReturn(true);
        when(utils.isStrongPassword(password)).thenReturn(true);
        when(persist.getUserByUsername(username)).thenReturn(mock(User.class));

        BasicResponse response = controller.signUp(
                "Ariel Semel",
                "ariel@example.com",
                username,
                password,
                password
        );

        assertFalse(response.isSuccess());
        assertEquals(Errors.ERROR_USERNAME_ALREADY_EXISTS, response.getErrorCode());
        verify(persist, never()).saveUser(any(User.class));
    }

    @Test
    void signUp_shouldRejectWeakUsername() {
        String username = "a";
        String password = "StrongPass1";

        when(utils.isValidUsername(username)).thenReturn(false);

        BasicResponse response = controller.signUp(
                "Ariel Semel",
                "ariel@example.com",
                username,
                password,
                password
        );

        assertFalse(response.isSuccess());
        assertEquals(Errors.ERROR_WEAK_USERNAME, response.getErrorCode());
        verifyNoInteractions(persist);
    }

    @Test
    void signUp_shouldRejectDifferentPasswords() {
        String username = "ariel12";

        when(utils.isValidUsername(username)).thenReturn(true);

        BasicResponse response = controller.signUp(
                "Ariel Semel",
                "ariel@example.com",
                username,
                "StrongPass1",
                "DifferentPass1"
        );

        assertFalse(response.isSuccess());
        assertEquals(Errors.ERROR_PASSWORD_DONT_MATCH, response.getErrorCode());
        verifyNoInteractions(persist);
    }
}
