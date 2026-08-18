package org.jejuro.miraero.domain.goal.milestone.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.aicoach.client.OpenAiClient;
import org.jejuro.miraero.domain.goal.milestone.dto.request.MilestoneReportAiRequest;
import org.jejuro.miraero.domain.goal.milestone.exception.MilestoneErrorCode;
import org.jejuro.miraero.global.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MilestoneReportAiService {

    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper =
            new ObjectMapper().findAndRegisterModules();


    public ParsedReport generateReport(
            MilestoneReportAiRequest request
    ) {

        try {

            String prompt =
                    buildPrompt(request);

            String response =
                    openAiClient.generateText(
                            prompt
                    );

            return parseAiResponse(
                    response
            );

        } catch (JsonProcessingException e) {

            throw new BusinessException(
                    MilestoneErrorCode.MILESTONE_REPORT_GENERATION_FAILED
            );
        }
    }

    private String buildPrompt(
            MilestoneReportAiRequest request
    ) throws JsonProcessingException {

        String requestJson =
                objectMapper.writeValueAsString(
                        request
                );

        return """
                당신은 금융 목표 달성 과정을 분석하고
                사용자에게 다음 행동 방향을 알려주며 그동안의 여정을 격려해주는 AI 금융 코치입니다.
              
                아래 목표, 마일스톤, 지출 데이터를 바탕으로
                이번 마일스톤까지의 목표 달성 과정을 평가하고,
                소비 패턴이 목표 달성에 어떤 영향을 주었는지 분석한 뒤,
                다음 마일스톤까지 실천할 수 있는 방향을 제안하세요.
              
                반드시 다음 규칙을 지키세요.
              
                1. 한국어로 작성하세요.
              
                2. 반드시 JSON 형식으로만 응답하세요.
                {"title":"...","content":"..."}
             
                3. 제목은 짧고 명확하게 작성하세요.
                   마일스톤 달성 상황이 드러나도록 작성할 수 있습니다.
              
                4. 본문은 3~5문장, 150자 내외로 작성하세요.
              
                5. 리포트의 핵심은 지출 통계가 아니라
                   해당 마일스톤까지 목표를 얼마나 잘 진행했는지에 대한 평가입니다.
              
                6. 다음 순서로 작성하세요.
                   - 먼저 사용자가 지금까지 목표를 위해 노력해온 과정을 인정하고 공감하세요.
                   - 그다음 마일스톤까지의 목표 달성 과정을 평가하세요.
                   - 지출 데이터가 있다면 목표 달성 과정과 연결하여 해석
                   - 다음 마일스톤까지의 구체적이고 간단한 행동 제안
              
                   단, 마일스톤의 달성 금액, 달성률, 달성일, 목표일 등
                        화면에서 이미 제공되는 정보는 본문에서 반복해서 설명하지 마세요.
                        이러한 정보를 단순히 나열하거나 문장으로 재진술하는 대신,
                        해당 정보를 바탕으로 목표 달성 과정을 평가하는 데 활용하세요.
              
                7. 지출 데이터가 존재하는 경우에만 소비 관련 내용을 작성하세요.
                   지출 데이터가 없다면 목표 및 마일스톤 달성 상황을 중심으로 작성하세요.
              
                8. 지출 카테고리는 목표 달성에 의미가 있는 경우에만
                   최대 2개까지 언급하세요.
                   단순히 높은 비율의 카테고리를 나열하지 마세요.
              
                9. amount와 proportion은 현재 기간의 지출 현황을
                   설명하는 데 필요한 경우에만 사용하세요.
              
                10. changeRate가 제공된 경우에만 이전 기간과 비교하세요.
                    changeRate가 null이면 증감 여부를 언급하지 마세요.
              
                11. 제공된 데이터에 없는 수치나 사실을
                    임의로 만들어내지 마세요.
              
                12. 같은 날짜에 여러 마일스톤이 달성된 경우에도
                    각 마일스톤을 독립적으로 평가하세요.
                    같은 날짜라는 이유로 지출 분석 기간을 임의로 나누지 마세요.
              
                13. 과도하게 단정적인 금융 조언을 하지 말고,
                    현재 데이터에 근거하여 현실적인 행동 방향을 제안하세요.
              
                14. 단순히 다음과 같은 통계 나열식 표현은 피하세요.
                    예: "총지출은 O원이고 일평균은 O원이며
                    식비가 O%, 쇼핑이 O%입니다."
              
                15. 숫자는 반드시 목표 달성 과정이나 소비 특성을
                    설명하는 데 의미가 있을 때만 사용하세요.

                요청 데이터:
              """ + requestJson;
    }

    private ParsedReport parseAiResponse(
            String response
    ) {

        if (!StringUtils.hasText(response)) {
            throw new BusinessException(
                    MilestoneErrorCode.MILESTONE_REPORT_GENERATION_FAILED
            );
        }

        String cleanedResponse =
                response
                        .replace("```json", "")
                        .replace("```", "")
                        .trim();

        try {

            JsonNode jsonNode =
                    objectMapper.readTree(
                            cleanedResponse
                    );

            String title =
                    jsonNode
                            .path("title")
                            .asText();

            String content =
                    jsonNode
                            .path("content")
                            .asText();

            if (!StringUtils.hasText(title)) {
                title = "마일스톤 AI 리포트";
            }

            if (!StringUtils.hasText(content)) {
                throw new BusinessException(
                        MilestoneErrorCode
                                .MILESTONE_REPORT_GENERATION_FAILED
                );
            }

            return new ParsedReport(
                    title.trim(),
                    content.trim()
            );

        } catch (JsonProcessingException e) {

            throw new BusinessException(
                    MilestoneErrorCode
                            .MILESTONE_REPORT_GENERATION_FAILED
            );
        }
    }

    public record ParsedReport(
            String title,
            String content
    ) {
    }
}