package org.jejuro.miraero.domain.goal.milestone.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;
import org.jejuro.miraero.domain.goal.milestone.domain.MilestoneReport;

import java.util.List;

@Mapper
public interface MilestoneReportMapper {

    MilestoneReport findByMilestoneId(@Param("milestoneId") Long milestoneId);

    List<MilestoneReport> findByMilestoneIds(
            @Param("milestoneIds") List<Long> milestoneIds);

    void save(
            @Param("report") MilestoneReport report);

    void updateReport(
            @Param("report") MilestoneReport report);
}