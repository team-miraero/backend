package org.jejuro.miraero.domain.goal.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.jejuro.miraero.domain.goal.domain.Goal;

import java.util.List;

@Mapper
public interface GoalMapper {
    void save(Goal goal);
    List<Goal> findGoalsByUserId(Long userId);
}
