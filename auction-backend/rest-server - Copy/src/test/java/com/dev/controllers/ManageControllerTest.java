package com.dev.controllers;

import com.dev.objects.Product;
import com.dev.objects.User;
import com.dev.responses.BasicResponse;
import com.dev.responses.ManageResponse;
import com.dev.responses.UserDetailsResponse;
import com.dev.utils.Errors;
import com.dev.utils.Persist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;

import static com.dev.utils.Definitions.ADMIN_PARAM;
import static com.dev.utils.Definitions.USER_PARAM;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ManageControllerTest {

    private static final String TOKEN = "admin-token";
    private static final Integer ADMIN_ID = 1;
    private static final Integer USER_ID = 2;

    private ManageController controller;
    private Persist persist;

    @BeforeEach
    void setUp() {
        controller = spy(new ManageController());
        persist = mock(Persist.class);

        ReflectionTestUtils.setField(controller, "persist", persist);

        doReturn(new BasicResponse(true, null))
                .when(controller)
                .basicValidation(TOKEN, ADMIN_ID);
    }

    @Test
    void getAllUsers_shouldReturnManageResponse_whenValidationSucceeds() {
        User admin = mock(User.class);
        User user1 = mock(User.class);
        User user2 = mock(User.class);

        when(admin.getCredit()).thenReturn(500);
        when(persist.getUserByToken(TOKEN)).thenReturn(admin);
        when(persist.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        BasicResponse response = controller.getAllUsers(TOKEN, ADMIN_ID);

        assertTrue(response.isSuccess());
        assertInstanceOf(ManageResponse.class, response);

        ManageResponse manageResponse = (ManageResponse) response;

        assertEquals(500, manageResponse.getCredit());
        assertEquals(2, manageResponse.getUsers().size());

        verify(persist).getUserByToken(TOKEN);
        verify(persist).getAllUsers();
    }

    @Test
    void getAllUsers_shouldReturnValidationError_whenValidationFails() {
        doReturn(new BasicResponse(false, Errors.PERMISSION_ERROR_CODE))
                .when(controller)
                .basicValidation(TOKEN, ADMIN_ID);

        BasicResponse response = controller.getAllUsers(TOKEN, ADMIN_ID);

        assertFalse(response.isSuccess());
        assertEquals(
                Errors.PERMISSION_ERROR_CODE,
                response.getErrorCode()
        );

        verify(persist, never()).getAllUsers();
    }

    @Test
    void getUserDetails_shouldRejectNonAdminUser() {
        User normalUser = mock(User.class);

        when(normalUser.getUserType()).thenReturn(USER_PARAM);
        when(persist.getUserByToken(TOKEN)).thenReturn(normalUser);

        BasicResponse response = controller.getUserDetails(TOKEN, USER_ID);

        assertFalse(response.isSuccess());
        assertEquals(
                Errors.NO_SUCH_USER,
                response.getErrorCode()
        );

        verify(persist, never()).getUserById(USER_ID);
    }

    @Test
    void getUserDetails_shouldReturnNoSuchUser_whenTokenDoesNotExist() {
        when(persist.getUserByToken(TOKEN)).thenReturn(null);

        BasicResponse response = controller.getUserDetails(TOKEN, USER_ID);

        assertFalse(response.isSuccess());
        assertEquals(
                Errors.GENERAL_ERROR,
                response.getErrorCode()
        );

        verify(persist, never()).getUserById(USER_ID);
    }

    @Test
    void getUserDetails_shouldExposeCurrentControllerBug_forValidAdmin() {
        User admin = mock(User.class);
        User user = mock(User.class);
        Product product = mock(Product.class);

        when(admin.getUserType()).thenReturn(ADMIN_PARAM);
        when(persist.getUserByToken(TOKEN)).thenReturn(admin);
        when(persist.getUserById(USER_ID)).thenReturn(user);
        when(persist.getMyProductsForSale(USER_ID))
                .thenReturn(Collections.singletonList(product));

        BasicResponse response = controller.getUserDetails(TOKEN, USER_ID);

        assertInstanceOf(UserDetailsResponse.class, response);

        /*
         * The controller currently creates a successful UserDetailsResponse,
         * but then overwrites its errorCode with NO_SUCH_USER.
         *
         * This assertion documents the current bug.
         * After fixing ManageController, change this to assertNull(...).
         */
        assertEquals(
                Errors.NO_SUCH_USER,
                response.getErrorCode()
        );
    }

    @Test
    void addCredit_shouldUpdateCredit_whenAdminAndUserAreValid() {
        int currentCredit = 100;
        int creditToAdd = 50;

        User admin = mock(User.class);
        User userBeforeUpdate = mock(User.class);
        User userAfterUpdate = mock(User.class);

        when(admin.getUserType()).thenReturn(ADMIN_PARAM);
        when(persist.getUserByToken(TOKEN)).thenReturn(admin);

        when(userBeforeUpdate.getCredit()).thenReturn(currentCredit);

        when(persist.getUserById(USER_ID))
                .thenReturn(userBeforeUpdate)
                .thenReturn(userBeforeUpdate)
                .thenReturn(userAfterUpdate);

        when(persist.getMyProductsForSale(USER_ID))
                .thenReturn(Collections.emptyList());

        BasicResponse response =
                controller.addCredit(TOKEN, USER_ID, creditToAdd);

        assertTrue(response.isSuccess());
        assertInstanceOf(UserDetailsResponse.class, response);

        verify(persist).updateUserCredit(
                USER_ID,
                currentCredit + creditToAdd
        );
    }

    @Test
    void addCredit_shouldRejectNonAdminUser() {
        User normalUser = mock(User.class);
        User targetUser = mock(User.class);

        when(normalUser.getUserType()).thenReturn(USER_PARAM);
        when(persist.getUserByToken(TOKEN)).thenReturn(normalUser);
        when(persist.getUserById(USER_ID)).thenReturn(targetUser);

        BasicResponse response =
                controller.addCredit(TOKEN, USER_ID, 50);

        assertFalse(response.isSuccess());
        assertEquals(
                Errors.PERMISSION_ERROR_CODE,
                response.getErrorCode()
        );

        verify(persist, never())
                .updateUserCredit(anyInt(), anyInt());
    }

    @Test
    void addCredit_shouldRejectUnknownUser() {
        User admin = mock(User.class);

        when(admin.getUserType()).thenReturn(ADMIN_PARAM);
        when(persist.getUserByToken(TOKEN)).thenReturn(admin);
        when(persist.getUserById(USER_ID)).thenReturn(null);

        BasicResponse response =
                controller.addCredit(TOKEN, USER_ID, 50);

        assertFalse(response.isSuccess());
        assertEquals(
                Errors.ERROR_MISSING_USERNAME,
                response.getErrorCode()
        );

        verify(persist, never())
                .updateUserCredit(anyInt(), anyInt());
    }
}
