package com.invoice.invoice_api.model;

import com.invoice.invoice_api.enums.RateCalculationType;
import com.invoice.invoice_api.enums.RateType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "project_role_rate_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_role_rate_item_type",
                        columnNames = {
                                "project_role_rate_id",
                                "rate_type"
                        }
                )
        }
)
public class ProjectRoleRateItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "project_role_rate_id",
            nullable = false
    )
    private ProjectRoleRate projectRoleRate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "rate_type",
            nullable = false,
            length = 50
    )
    private RateType rateType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "calculation_type",
            nullable = false,
            length = 30
    )
    private RateCalculationType calculationType;

    @Column(
            nullable = false,
            precision = 12,
            scale = 4
    )
    private BigDecimal value;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    public ProjectRoleRateItem() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ProjectRoleRate getProjectRoleRate() {
        return projectRoleRate;
    }

    public void setProjectRoleRate(
            ProjectRoleRate projectRoleRate
    ) {
        this.projectRoleRate = projectRoleRate;
    }

    public RateType getRateType() {
        return rateType;
    }

    public void setRateType(RateType rateType) {
        this.rateType = rateType;
    }

    public RateCalculationType getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(
            RateCalculationType calculationType
    ) {
        this.calculationType = calculationType;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
