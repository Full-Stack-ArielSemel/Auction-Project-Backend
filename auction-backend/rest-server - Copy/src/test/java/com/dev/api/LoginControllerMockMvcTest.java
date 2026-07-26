package com.dev.api;

import com.dev.controllers.LiveUpdatesController;
import com.dev.controllers.LoginController;
import com.dev.objects.User;
import com.dev.utils.Persist;
import com.dev.utils.Utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.dev.utils.Errors.ERROR_MISSING_PASSWORD;
import static com.dev.utils.Errors.ERROR_MISSING_USERNAME;
import static com.dev.utils.Errors.ERROR_WRONG_LOGIN_CREDS;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LoginControllerMockMvcTest {

    private MockMvc mockMvc;

    private Persist persist;
    private Utils utils;
    private LiveUpdatesController liveUpdatesController;

    @BeforeEach
    void setUp() {
        LoginController controller = new LoginController();

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

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void login_shouldReturnSuccess_whenCredentialsAreValid()
            throws Exception {

        String username = "ariel";
        String password = "StrongPass1";
        String token = "generated-token";

        User user = mock(User.class);

        when(utils.createHash(username, password))
                .thenReturn(token);

        when(persist.getUserByUsernameAndToken(username, token))
                .thenReturn(user);

        when(user.getId()).thenReturn(10);
        when(user.getUserType()).thenReturn("user");

        mockMvc.perform(
                        post("/login")
                                .param("username", username)
                                .param("password", password)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.errorCode").doesNotExist())
                .andExpect(jsonPath("$.token").value(token));

        verify(utils).createHash(username, password);

        verify(persist)
                .getUserByUsernameAndToken(username, token);
    }

    @Test
    void login_shouldReturnError_whenCredentialsAreInvalid()
            throws Exception {

        String username = "ariel";
        String password = "WrongPassword";
        String token = "generated-token";

        when(utils.createHash(username, password))
                .thenReturn(token);

        when(persist.getUserByUsernameAndToken(username, token))
                .thenReturn(null);

        mockMvc.perform(
                        post("/login")
                                .param("username", username)
                                .param("password", password)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(ERROR_WRONG_LOGIN_CREDS)
                );

        verify(persist)
                .getUserByUsernameAndToken(username, token);
    }

    @Test
    void login_shouldReturnError_whenUsernameIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/login")
                                .param("password", "StrongPass1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(ERROR_MISSING_USERNAME)
                );

        verifyNoInteractions(persist);
    }

    @Test
    void login_shouldReturnError_whenPasswordIsMissing()
            throws Exception {

        mockMvc.perform(
                        post("/login")
                                .param("username", "ariel")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(
                        jsonPath("$.errorCode")
                                .value(ERROR_MISSING_PASSWORD)
                );

        verifyNoInteractions(persist);
    }

    @Test
    void login_shouldRejectGetRequest()
            throws Exception {

        mockMvc.perform(
                        org.springframework.test.web.servlet
                                .request.MockMvcRequestBuilders
                                .get("/login")
                                .param("username", "ariel")
                                .param("password", "StrongPass1")
                )
                .andExpect(status().isMethodNotAllowed());
    }
}
