package org.jejuro.miraero.domain.goal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jejuro.miraero.domain.goal.dto.request.GoalPossibilityRequest;
import org.jejuro.miraero.domain.goal.dto.response.GoalPossibilityResponse;
import org.jejuro.miraero.domain.goal.service.GoalService;
import org.jejuro.miraero.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class GoalControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private GoalService goalService;

    @BeforeEach
    void setUp() {
        GoalController goalController = new GoalController(goalService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(goalController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("목표 달성 가능성 확인에 성공하면 200 상태 코드와 가능성 정보를 반환한다")
    void checkPossibility_success_possible() throws Exception {
        // given
        String requestBody = "{\"goalAmount\": 1000000, \"goalMonths\": 10, \"startAmount\": 0}";

        GoalPossibilityResponse response = GoalPossibilityResponse.builder()
                .requiredMonthly(100000L)
                .availableMonthly(500000L)
                .possible(true)
                .build();

        given(goalService.checkPossibility(any(GoalPossibilityRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/goals/possibility")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requiredMonthly").value(100000L))
                .andExpect(jsonPath("$.data.availableMonthly").value(500000L))
                .andExpect(jsonPath("$.data.possible").value(true));

        verify(goalService).checkPossibility(any(GoalPossibilityRequest.class));
    }

    @Test
    @DisplayName("목표 달성이 불가능하면 possible은 false를 반환한다")
    void checkPossibility_success_not_possible() throws Exception {
        // given
        String requestBody = "{\"goalAmount\": 10000000, \"goalMonths\": 10, \"startAmount\": 0}";

        GoalPossibilityResponse response = GoalPossibilityResponse.builder()
                .requiredMonthly(1000000L)
                .availableMonthly(500000L)
                .possible(false)
                .build();

        given(goalService.checkPossibility(any(GoalPossibilityRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/goals/possibility")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.requiredMonthly").value(1000000L))
                .andExpect(jsonPath("$.data.availableMonthly").value(500000L))
                .andExpect(jsonPath("$.data.possible").value(false));

        verify(goalService).checkPossibility(any(GoalPossibilityRequest.class));
    }

    @Test
    @DisplayName("goalAmount가 null이면 400 상태 코드를 반환한다")
    void checkPossibility_nullGoalAmount() throws Exception {
        // given
        String requestBody = "{\"goalMonths\": 10, \"startAmount\": 0}";

        // when & then
        mockMvc.perform(
                        post("/api/goals/possibility")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));
    }

    @Test
    @DisplayName("goalMonths가 null이면 400 상태 코드를 반환한다")
    void checkPossibility_nullGoalMonths() throws Exception {
        // given
        String requestBody = "{\"goalAmount\": 1000000, \"startAmount\": 0}";

        // when & then
        mockMvc.perform(
                        post("/api/goals/possibility")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));
    }

    @Test
    @DisplayName("goalAmount가 0 이하면 400 상태 코드를 반환한다")
    void checkPossibility_negativeGoalAmount() throws Exception {
        // given
        String requestBody = "{\"goalAmount\": 0, \"goalMonths\": 10, \"startAmount\": 0}";

        // when & then
        mockMvc.perform(
                        post("/api/goals/possibility")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));
    }

    @Test
    @DisplayName("goalMonths가 0 이하면 400 상태 코드를 반환한다")
    void checkPossibility_negativeGoalMonths() throws Exception {
        // given
        String requestBody = "{\"goalAmount\": 1000000, \"goalMonths\": 0, \"startAmount\": 0}";

        // when & then
        mockMvc.perform(
                        post("/api/goals/possibility")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_002"));
    }

    @Test
    @DisplayName("startAmount가 null이면 0으로 간주하고 요청을 처리한다")
    void checkPossibility_nullStartAmount() throws Exception {
        // given
        String requestBody = "{\"goalAmount\": 1000000, \"goalMonths\": 10}";

        GoalPossibilityResponse response = GoalPossibilityResponse.builder()
                .requiredMonthly(100000L)
                .availableMonthly(500000L)
                .possible(true)
                .build();

        given(goalService.checkPossibility(any(GoalPossibilityRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/goals/possibility")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.possible").value(true));

        verify(goalService).checkPossibility(any(GoalPossibilityRequest.class));
    }
}
