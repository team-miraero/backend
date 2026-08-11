package org.jejuro.miraero.domain.goal.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalStatus;

import java.util.List;

@Mapper
public interface GoalMapper {
    void save(Goal goal);
    List<Goal> findGoalsByUserId(@Param("userId") Long userId);
    Goal findById(@Param("goalId") Long goalId);
    Goal findByIdAndUserId(
            @Param("userId") Long userId,
            @Param("goalId") Long goalId
    );
    int update(@Param("goal") Goal goal);
    void delete(@Param("goalId") Long goalId);
    void updateCollection(
            @Param("userId") Long userId,
            @Param("goalId") Long goalId
    );

    void updateCompleteStatus(
            @Param("goalId") Long goalId
    );
    List<Goal> findGoalCollectionsByUserId(
            @Param("userId") Long userId
    );

    void updateStatus(
            Long goalId,
            GoalStatus status
    );

    boolean existsActiveGoalByUserId(@Param("userId") Long userId);
}
