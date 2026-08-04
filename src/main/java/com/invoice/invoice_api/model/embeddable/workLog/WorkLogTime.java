package com.invoice.invoice_api.model.embeddable.workLog;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Duration;
import java.time.LocalTime;

@Embeddable
public class WorkLogTime {
    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "finish_time")
    private LocalTime finishTime;

    @Column(
            name = "unpaid_break_minutes",
            nullable = false
    )
    private Integer unpaidBreakMinutes = 0;

    public WorkLogTime() {
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(
            LocalTime startTime
    ) {
        this.startTime = startTime;
    }

    public LocalTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(
            LocalTime finishTime
    ) {
        this.finishTime = finishTime;
    }

    public Integer getUnpaidBreakMinutes() {
        return unpaidBreakMinutes;
    }

    public void setUnpaidBreakMinutes(
            Integer unpaidBreakMinutes
    ) {
        this.unpaidBreakMinutes =
                unpaidBreakMinutes == null
                        ? 0
                        : unpaidBreakMinutes;
    }

    public boolean hasStartAndFinishTime() {
        return startTime != null
                && finishTime != null;
    }

    public boolean crossesMidnight() {
        return hasStartAndFinishTime()
                && finishTime.isBefore(startTime);
    }

    public long calculateWorkedMinutes() {
        if (!hasStartAndFinishTime()) {
            return 0;
        }

        LocalTime effectiveFinishTime =
                finishTime;

        long totalMinutes;

        if (crossesMidnight()) {
            totalMinutes =
                    Duration.between(
                            startTime,
                            LocalTime.MAX
                    ).toMinutes()
                            + 1
                            + Duration.between(
                            LocalTime.MIDNIGHT,
                            effectiveFinishTime
                    ).toMinutes();
        } else {
            totalMinutes =
                    Duration.between(
                            startTime,
                            effectiveFinishTime
                    ).toMinutes();
        }

        long breakMinutes =
                unpaidBreakMinutes == null
                        ? 0
                        : unpaidBreakMinutes;

        return Math.max(
                totalMinutes - breakMinutes,
                0
        );
    }
}
