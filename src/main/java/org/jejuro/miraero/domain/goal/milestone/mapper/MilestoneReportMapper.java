package org.jejuro.miraero.domain.goal.milestone.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.jejuro.miraero.domain.goal.milestone.domain.MilestoneReport;

@Mapper
public interface MilestoneReportMapper {

    MilestoneReport findByMilestoneId(Long milestoneId);

    void save(MilestoneReport report);

    void updateReport(MilestoneReport report);
}