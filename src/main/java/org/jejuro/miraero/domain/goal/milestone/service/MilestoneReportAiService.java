package org.jejuro.miraero.domain.goal.milestone.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.aicoach.client.OpenAiClient;
import org.jejuro.miraero.domain.goal.milestone.dto.request.MilestoneReportAiRequest;
import org.jejuro.miraero.domain.goal.milestone.exception.MilestoneErrorCode;
import org.jejuro.miraero.global.exception.BusinessException;
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
                당신은 금융 목표 달성 리포트를 작성하는 AI 코치입니다.

                아래 목표, 마일스톤, 지출 데이터를 기반으로
                사용자의 목표 관리 상태를 분석해주세요.

                반드시 다음 규칙을 지켜주세요.

                1. 한국어로 작성하세요.

                2. 제목은 짧고 명확하게 작성하세요.

                3. 본문은 3~5문장으로 작성하세요.
                   한 문장은 너무 길게 작성하지 마세요.

                4. 전체 본문은 150자 내외의
                   간결한 리포트로 작성하세요.

                5. 반드시 JSON 형식으로만 응답하세요.

                6. 응답 형식:
                   {"title":"...","content":"..."}

                7. amount는 현재 분석 기간의
                   해당 카테고리 총지출입니다.

                8. proportion은 현재 분석 기간의
                   전체 지출 중 해당 카테고리의 비율입니다.

                9. dailyAverageExpense는 현재 분석 기간의
                   전체 지출 일평균입니다.

                10. changeRate는 총지출이 아니라
                    카테고리별 일평균 지출 기준의 증감률입니다.

                11. 현재 기간과 이전 비교 기간의 길이가
                    서로 다를 수 있습니다.
                    따라서 총지출만 비교해서
                    증가 또는 감소를 판단하지 마세요.

                12. 이전 비교 기간이 없으면
                    changeRate는 null입니다.

                13. changeRate가 null인 경우
                    임의의 증감률을 만들지 마세요.

                14. 현재 일평균 지출이 이전보다 낮다면
                    지출 관리가 개선된 것으로 해석할 수 있습니다.

                15. 현재 일평균 지출이 이전보다 높다면
                    증가한 카테고리를 구체적으로 언급할 수 있습니다.

                16. 전체 지출에서 비중이 높은 카테고리를
                    최대 2개까지 언급할 수 있습니다.

                17. amount가 0인 카테고리는
                    해당 기간에 지출이 없었던 것입니다.

                18. 제공된 데이터에 없는 수치나 사실을
                    임의로 만들어내지 마세요.

                19. 같은 날짜에 여러 마일스톤이 달성된 경우
                    각각 별도의 리포트를 생성합니다.

                20. 같은 날짜에 달성된 마일스톤들은
                    지출 분석 구간을 나누는 경계로 사용하지 않습니다.

                21. 지출 분석 기간은 시작일과 종료일을
                    모두 포함합니다.

                22. 이전 비교 기간이 존재하지 않는 경우
                    현재 기간의 지출 데이터를 중심으로 분석하세요.

                23. 하나의 마일스톤에 대해
                    리포트는 한 번만 생성됩니다.

                24. 사용자에게 실제 금융 조언을 하는 것처럼
                    과도하게 단정하지 말고,
                    현재 데이터에 근거한 간단한 행동 제안을 포함하세요.

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