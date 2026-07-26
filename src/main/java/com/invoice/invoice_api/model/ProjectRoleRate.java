package com.invoice.invoice_api.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(
        name = "project_role_rates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_position_rate_effective_from",
                        columnNames = {
                                "project_position_id",
                                "effective_from"
                        }
                )
        }
)
public class ProjectRoleRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "project_position_id",
            nullable = false
    )
    private ProjectPosition projectPosition;

    @Column(
            name = "effective_from",
            nullable = false
    )
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(
            mappedBy = "projectRoleRate",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProjectRoleRateItem> items = new ArrayList<>();

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

    public ProjectRoleRate() {
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

    public void addItem(ProjectRoleRateItem item) {
        items.add(item);
        item.setProjectRoleRate(this);
    }

    public void removeItem(ProjectRoleRateItem item) {
        items.remove(item);
        item.setProjectRoleRate(null);
    }

    public Long getId() {
        return id;
    }

    public ProjectPosition getProjectPosition() {
        return projectPosition;
    }

    public void setProjectPosition(
            ProjectPosition projectPosition
    ) {
        this.projectPosition = projectPosition;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<ProjectRoleRateItem> getItems() {
        return items;
    }

    public void setItems(List<ProjectRoleRateItem> items) {
        this.items.clear();

        if (items != null) {
            items.forEach(this::addItem);
        }
    }
    public void clearItems() {
        for (ProjectRoleRateItem item : items) {
            item.setProjectRoleRate(null);
        }

        items.clear();
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
