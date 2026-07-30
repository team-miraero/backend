package org.jejuro.miraero.domain.goal.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.jejuro.miraero.domain.goal.domain.Goal;

@Mapper
public interface GoalMapper {
    void save(Goal goal);
}
