package org.jejuro.miraero.domain.goal.milestone.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.jejuro.miraero.domain.goal.milestone.domain.MilestoneReport;

import java.util.List;

@Mapper
public interface MilestoneReportMapper {

    List<MilestoneReport> findByMilestoneIds(List<Long> milestoneIds);

    void save(MilestoneReport report);

    void updateReport(MilestoneReport report);
}