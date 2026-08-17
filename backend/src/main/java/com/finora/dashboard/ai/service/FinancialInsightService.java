package com.finora.dashboard.ai.service;

import com.finora.dashboard.ai.response.FinancialInsightResponse;
import com.finora.user.entity.UserEntity;

public interface FinancialInsightService {

    FinancialInsightResponse generateInsight(UserEntity user);

}