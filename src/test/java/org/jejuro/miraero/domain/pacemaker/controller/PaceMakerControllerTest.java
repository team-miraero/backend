package org.jejuro.miraero.domain.pacemaker.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.jejuro.miraero.domain.pacemaker.dto.response.PaceMakerResponse;
import org.jejuro.miraero.domain.pacemaker.service.PaceMakerService;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.jejuro.miraero.global.security.AuthenticatedUser;
import org.jejuro.miraero.global.security.JwtAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class PaceMakerControllerTest {

    private static final Long USER_ID = 42L;

    @Mock
    private PaceMakerService paceMakerService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PaceMakerController paceMakerController = new PaceMakerController(paceMakerService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(paceMakerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(new AuthenticatedUser(USER_ID))
        );
    }

    @Test
    @DisplayName("페이스메이커 조회 요청은 200 응답과 활성 상태를 반환한다")
    void getPaceMaker_active() throws Exception {
        PaceMakerResponse response = PaceMakerResponse.builder()
                .autoSavingId(21L)
                .status("ACTIVE")
                .enabled(true)
                .build();
        given(paceMakerService.getPaceMaker(USER_ID)).willReturn(response);

        mockMvc.perform(get("/api/pace-maker"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.autoSavingId").value(21L))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.enabled").value(true));

        verify(paceMakerService).getPaceMaker(USER_ID);
    }

    @Test
    @DisplayName("자동저축 설정이 없으면 null 상태와 enabled false를 반환한다")
    void getPaceMaker_notCreated() throws Exception {
        PaceMakerResponse response = PaceMakerResponse.builder()
                .autoSavingId(null)
                .status(null)
                .enabled(false)
                .build();
        given(paceMakerService.getPaceMaker(USER_ID)).willReturn(response);

        mockMvc.perform(get("/api/pace-maker"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.autoSavingId").doesNotExist())
                .andExpect(jsonPath("$.data.status").doesNotExist())
                .andExpect(jsonPath("$.data.enabled").value(false));

        verify(paceMakerService).getPaceMaker(USER_ID);
    }

    @Test
    @DisplayName("페이스메이커 상태 변경 요청은 200 응답과 변경된 상태를 반환한다")
    void updatePaceMaker_success() throws Exception {
        PaceMakerResponse response = PaceMakerResponse.builder()
                .autoSavingId(21L)
                .status("PAUSED")
                .enabled(false)
                .build();
        given(paceMakerService.updateStatus(USER_ID, 21L, "PAUSED")).willReturn(response);

        mockMvc.perform(patch("/api/pace-maker/{autoSavingId}/status", 21L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PAUSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.autoSavingId").value(21L))
                .andExpect(jsonPath("$.data.status").value("PAUSED"))
                .andExpect(jsonPath("$.data.enabled").value(false));

        verify(paceMakerService).updateStatus(USER_ID, 21L, "PAUSED");
    }

    @Test
    @DisplayName("허용되지 않은 상태값이면 400 응답을 반환한다")
    void updatePaceMaker_invalidStatus() throws Exception {
        mockMvc.perform(patch("/api/pace-maker/{autoSavingId}/status", 21L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OFF\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));
    }

    @Test
    @DisplayName("수정 대상 자동저축이 없으면 404 응답을 반환한다")
    void updatePaceMaker_notFound() throws Exception {
        given(paceMakerService.updateStatus(USER_ID, 99L, "ACTIVE"))
                .willThrow(new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND));

        mockMvc.perform(patch("/api/pace-maker/{autoSavingId}/status", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_004"));

        verify(paceMakerService).updateStatus(USER_ID, 99L, "ACTIVE");
    }
}