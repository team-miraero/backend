package org.jejuro.miraero.domain.aicoach.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiCoachConversationMapper {

    Map<String, Object> findLatestByUserId(@Param("userId") Long userId);

    List<Map<String, Object>> findAllByUserId(@Param("userId") Long userId);

    int save(Map<String, Object> conversation);

    Map<String, Object> findByIdAndUserId(
            @Param("userId") Long userId,
            @Param("conversationId") Long conversationId
    );

    int deleteByIdAndUserId(
            @Param("userId") Long userId,
            @Param("conversationId") Long conversationId
    );

    int updateTitle(
            @Param("userId") Long userId,
            @Param("conversationId") Long conversationId,
            @Param("title") String title
    );

    int updateLastMessageAt(
            @Param("userId") Long userId,
            @Param("conversationId") Long conversationId,
            @Param("lastMessageAt") LocalDateTime lastMessageAt
    );
}
