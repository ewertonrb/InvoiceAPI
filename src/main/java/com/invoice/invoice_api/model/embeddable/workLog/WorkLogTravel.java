package com.invoice.invoice_api.model.embeddable.workLog;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class WorkLogTravel {
    @Column(
            name = "travel_hours",
            precision = 10,
            scale = 2,
            nullable = false
    )
    private BigDecimal travelHours = BigDecimal.ZERO;

    @Column(
            name = "kilometres",
            precision = 10,
            scale = 2,
            nullable = false
    )
    private BigDecimal kilometres = BigDecimal.ZERO;

    @Column(
            name = "lafha_nights",
            nullable = false
    )
    private Integer lafhaNights = 0;

    public WorkLogTravel() {
    }

    public BigDecimal getTravelHours() {
        return travelHours;
    }

    public void setTravelHours(
            BigDecimal travelHours
    ) {
        this.travelHours =
                travelHours == null
                        ? BigDecimal.ZERO
                        : travelHours;
    }

    public BigDecimal getKilometres() {
        return kilometres;
    }

    public void setKilometres(
            BigDecimal kilometres
    ) {
        this.kilometres =
                kilometres == null
                        ? BigDecimal.ZERO
                        : kilometres;
    }

    public Integer getLafhaNights() {
        return lafhaNights;
    }

    public void setLafhaNights(
            Integer lafhaNights
    ) {
        this.lafhaNights =
                lafhaNights == null
                        ? 0
                        : lafhaNights;
    }

    public boolean hasTravelHours() {
        return travelHours != null
                && travelHours.compareTo(
                BigDecimal.ZERO
        ) > 0;
    }

    public boolean hasKilometres() {
        return kilometres != null
                && kilometres.compareTo(
                BigDecimal.ZERO
        ) > 0;
    }

    public boolean hasLafha() {
        return lafhaNights != null
                && lafhaNights > 0;
    }

    public boolean hasAnyTravelAllowance() {
        return hasTravelHours()
                || hasKilometres()
                || hasLafha();
    }
}
