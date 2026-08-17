package com.finora.budget.entity;

import com.finora.budget.enums.BudgetPeriod;
import com.finora.category.entity.CategoryEntity;
import com.finora.common.entity.BaseEntity;
import com.finora.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name = "budgets")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BudgetEntity extends BaseEntity {

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetPeriod period;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private BudgetEntity(BigDecimal amount, BudgetPeriod period, LocalDate startDate, LocalDate endDate,
            CategoryEntity category, UserEntity user) {
        this.amount = amount;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.user = user;
    }

    public static BudgetEntity create(BigDecimal amount, BudgetPeriod period, LocalDate startDate, LocalDate endDate,
            CategoryEntity category, UserEntity user) {
        return new BudgetEntity(amount, period, startDate, endDate, category, user);
    }

    public void update(BigDecimal amount, BudgetPeriod period, LocalDate startDate, LocalDate endDate,
            CategoryEntity category) {
        this.amount = amount;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
    }
}
