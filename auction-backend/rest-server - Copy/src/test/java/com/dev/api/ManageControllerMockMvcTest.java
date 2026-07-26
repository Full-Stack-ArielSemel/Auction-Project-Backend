package com.dev.api;

import com.dev.controllers.LiveUpdatesController;
import com.dev.controllers.MainController;
import com.dev.controllers.ManageController;
import com.dev.objects.User;
import com.dev.utils.Persist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static com.dev.utils.Definitions.ADMIN_PARAM;
import static com.dev.utils.Definitions.USER_PARAM;
import static com.dev.utils.Errors.*;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ManageControllerMockMvcTest {

    private static final String TOKEN = "admin-token";
    private static final int ADMIN_ID = 1;
    private static final int USER_ID = 2;

    private MockMvc mockMvc;

    private Persist persist;
    private LiveUpdatesController liveUpdatesController;

    @BeforeEach
    void setUp() {
        ManageController controller = new ManageController();

        persist = mock(Persist.class);
        liveUpdatesController = mock(LiveUpdatesController.class);

        /*
         * ManageController has its own Persist field.
         */
        ReflectionTestUtils.setField(
                controller,
                "persist",
                persist
        );

        ReflectionTestUtils.setField(
                controller,
                "liveUpdatesController",
                liveUpdatesController
        );

        /*
         * MainController also has a Persist field.
         * basicValidation() uses the parent field.
         */
        ReflectionTestUtils.setField(
                controller,
                "persist",
                persist,
                MainController.class
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    private void allowAdminValidation() {
        when(persist.userHasPermissions(ADMIN_ID, TOKEN))
                .thenReturn(true);
    }

    @Test
    void getManageDetails_shouldReturnSuccess_whenRequestIsValid()
            throws Exception {

        allowAdminValidation();

        User admin = mock(User.class);
        User firstUser = mock(User.class);
        User secondUser = mock(User.class);

        when(admin.getCredit()).thenReturn(1000);

        when(persist.getUserByToken(TOKEN))
                .thenReturn(admin);

        when(persist.getAllUsers())
                .thenReturn(Arrays.asList(firstUser, secondUser));

        mockMvc.perform(
                        get("/get-manage-details")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(ADMIN_ID)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.credit")
                                .value(1000)
                );

        verify(persist).getAllUsers();
    }

    @Test
    void getManageDetails_shouldReturnPermissionError_whenValidationFails()
            throws Exception {

        when(persist.userHasPermissions(ADMIN_ID, TOKEN))
                .thenReturn(false);

        mockMvc.perform(
                        get("/get-manage-details")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(ADMIN_ID)
                                )
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

        verify(persist, never()).getAllUsers();
    }

    @Test
    void getManageDetails_shouldReturnMissingToken_whenTokenIsMissing()
            throws Exception {

        mockMvc.perform(
                        get("/get-manage-details")
                                .param(
                                        "userId",
                                        String.valueOf(ADMIN_ID)
                                )
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
    void getManageDetails_shouldRejectPostRequest()
            throws Exception {

        mockMvc.perform(
                        post("/get-manage-details")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(ADMIN_ID)
                                )
                )
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void getUserDetails_shouldReturnResponseForValidAdmin()
            throws Exception {

        User admin = mock(User.class);
        User targetUser = mock(User.class);

        when(admin.getUserType())
                .thenReturn(ADMIN_PARAM);

        when(persist.getUserByToken(TOKEN))
                .thenReturn(admin);

        when(persist.getUserById(USER_ID))
                .thenReturn(targetUser);

        when(persist.getMyProductsForSale(USER_ID))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                        get("/get-user-details")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                )
                /*
                 * Current controller behavior:
                 * errorCode is overwritten with NO_SUCH_USER
                 * even after creating a successful response.
                 */
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(NO_SUCH_USER)
                );

        verify(persist).getUserById(USER_ID);
    }

    @Test
    void getUserDetails_shouldRejectNonAdminUser()
            throws Exception {

        User normalUser = mock(User.class);

        when(normalUser.getUserType())
                .thenReturn(USER_PARAM);

        when(persist.getUserByToken(TOKEN))
                .thenReturn(normalUser);

        mockMvc.perform(
                        get("/get-user-details")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                /*
                 * ManageController currently overwrites the
                 * permission error with NO_SUCH_USER.
                 */
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(NO_SUCH_USER)
                );

        verify(persist, never()).getUserById(USER_ID);
    }

    @Test
    void addCredit_shouldReturnSuccess_whenAdminAddsCredit()
            throws Exception {

        User admin = mock(User.class);
        User targetUser = mock(User.class);
        User updatedUser = mock(User.class);

        when(admin.getUserType())
                .thenReturn(ADMIN_PARAM);

        when(targetUser.getCredit())
                .thenReturn(100);

        when(updatedUser.getCredit())
                .thenReturn(150);

        when(persist.getUserByToken(TOKEN))
                .thenReturn(admin);

        when(persist.getUserById(USER_ID))
                .thenReturn(targetUser)
                .thenReturn(targetUser)
                .thenReturn(updatedUser);

        when(persist.getMyProductsForSale(USER_ID))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                        post("/add-credit")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                                .param(
                                        "creditToAdd",
                                        "50"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(true)
                );

        verify(persist)
                .updateUserCredit(USER_ID, 150);
    }

    @Test
    void addCredit_shouldReturnPermissionError_whenUserIsNotAdmin()
            throws Exception {

        User normalUser = mock(User.class);
        User targetUser = mock(User.class);

        when(normalUser.getUserType())
                .thenReturn(USER_PARAM);

        when(persist.getUserByToken(TOKEN))
                .thenReturn(normalUser);

        when(persist.getUserById(USER_ID))
                .thenReturn(targetUser);

        mockMvc.perform(
                        post("/add-credit")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                                .param(
                                        "creditToAdd",
                                        "50"
                                )
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
        ).updateUserCredit(anyInt(), anyInt());
    }

    @Test
    void addCredit_shouldReturnNoSuchUser_whenTokenIsInvalid()
            throws Exception {

        when(persist.getUserByToken(TOKEN))
                .thenReturn(null);

        mockMvc.perform(
                        post("/add-credit")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                                .param(
                                        "creditToAdd",
                                        "50"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.success")
                                .value(false)
                )
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(NO_SUCH_USER)
                );

        verify(
                persist,
                never()
        ).updateUserCredit(anyInt(), anyInt());
    }

    @Test
    void addCredit_shouldRejectGetRequest()
            throws Exception {

        mockMvc.perform(
                        get("/add-credit")
                                .param("token", TOKEN)
                                .param(
                                        "userId",
                                        String.valueOf(USER_ID)
                                )
                                .param(
                                        "creditToAdd",
                                        "50"
                                )
                )
                .andExpect(status().isMethodNotAllowed());
    }
}
