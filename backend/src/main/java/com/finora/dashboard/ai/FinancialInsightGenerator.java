package com.finora.dashboard.ai;

import com.finora.dashboard.ai.response.FinancialInsightResponse;

public interface FinancialInsightGenerator {

    FinancialInsightResponse generate(String prompt);

}