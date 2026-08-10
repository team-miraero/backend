package org.jejuro.miraero.domain.goal.milestone.service;

public interface MilestoneReportService {

    void generateReport(
            Long milestoneId,
            Long goalId
    );
}
