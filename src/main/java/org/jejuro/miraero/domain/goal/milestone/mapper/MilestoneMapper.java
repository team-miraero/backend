package org.jejuro.miraero.domain.goal.milestone.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;
import org.jejuro.miraero.domain.goal.milestone.domain.MilestoneReport;

import java.util.List;

@Mapper
public interface MilestoneMapper {

    List<Milestone> findByGoalId(Long goalId);

    Milestone findById(Long milestoneId);

    void save(Milestone milestone);

    void saveAll(List<Milestone> milestones);

    void deleteByGoalId(Long goalId);

    void updateAchievement(Milestone milestone);
}