package com.finora.dashboard.ai.prompt;

import com.finora.dashboard.ai.context.BudgetContext;
import com.finora.dashboard.ai.context.CategorySpendingContext;
import com.finora.dashboard.ai.context.FinancialInsightContext;
import org.springframework.stereotype.Component;

@Component
public class FinancialInsightPromptBuilder {

    public String build(FinancialInsightContext context) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
        You are a personal finance assistant.
                Analyze the user's overall financial position and current-month
                financial activity to provide a short, useful and actionable
                financial insight.   
                Important:
                - Overall income, overall expense and overall balance represent the user's total financial position.
                - Monthly income, monthly expense and monthly balance represent the current month only.
                - Category spending values represent the current month only.
                - Budget spending values are calculated specifically for each budget.
                - Do not calculate or infer budget spending from category spending.
                BUDGET RULES:
                        - A budget listed in the "Budgets" section ALREADY EXISTS.
                        - NEVER recommend creating, setting, adding or defining a new budget for a category that appears in the "Budgets" section.
                        - NEVER say that a category has no budget if that category appears in the "Budgets" section.
                        - For an existing budget, only discuss its current usage, remaining amount and risk level.
                        - Only recommend creating a budget for a category if that category is NOT present in the "Budgets" section.
                - If a budget exists and usage is below 80%, do not describe it as a budget problem.
                - If a budget usage is above 80%, warn the user about the risk of exceeding it.
                - If a budget usage is above 100%, clearly state that the budget has been exceeded.
                - Do not claim that a budget does not exist if a budget is present.
                - Do not recommend creating a budget for a category that already has one.
                - Do not invent financial data.
                - Use only the provided data.
                - Do not assume household size, location or other information that was not provided.
                - Do not ask the user for additional personal information.
                - Be concise.
                - Mention important current-month spending patterns.
                - Use overall financial position only to provide context.
                - Mention budget risks when relevant.
                - Give practical financial advice.
                - Respond in English.
                
                Financial data:
        """);

        prompt.append("Overall financial position:\n");

        prompt.append("Total income: ")
                .append(context.totalIncome())
                .append("\n");

        prompt.append("Total expense: ")
                .append(context.totalExpense())
                .append("\n");

        prompt.append("Overall balance: ")
                .append(context.balance())
                .append("\n\n");

        prompt.append("Current month financial activity:\n");

        prompt.append("Monthly income: ")
                .append(context.monthly().income())
                .append("\n");

        prompt.append("Monthly expense: ")
                .append(context.monthly().expense())
                .append("\n");

        prompt.append("Monthly balance: ")
                .append(context.monthly().balance())
                .append("\n\n");


        for (CategorySpendingContext category :
                context.categorySpendings()) {

            prompt.append("- ")
                    .append(category.categoryName())
                    .append(": ")
                    .append(category.amount())
                    .append("\n");
        }

        prompt.append("\nBudgets:\n");

        for (BudgetContext budget : context.budgets()) {

            prompt.append("- ")
                    .append(budget.categoryName())
                    .append(": budget=")
                    .append(budget.budget())
                    .append(", spent=")
                    .append(budget.spent())
                    .append(", remaining=")
                    .append(budget.remaining())
                    .append(", usage=")
                    .append(budget.percentage())
                    .append("%\n");
        }
        prompt.append("""
        
        RESPONSE FORMAT:
        Return ONLY valid JSON.
        Do not use Markdown.
        Do not use code fences.
        Do not include any text before or after the JSON.

        The JSON must exactly follow this structure:

        {
          "summary": "short overall financial summary",
          "monthlyStatus": {
            "income": 0,
            "expenses": 0,
            "balance": 0
          },
          "budgetInsights": [
            {
              "category": "category name",
              "spent": 0,
              "budget": 0,
              "remaining": 0,
              "usagePercentage": 0
            }
          ],
          "recommendations": [
            "practical recommendation"
          ]
        }

        RESPONSE RULES:
        - summary must be concise.
        - monthlyStatus must use the provided monthly financial values.
        - budgetInsights must contain only budgets provided in the input.
        - Do not invent budgets.
        - Do not calculate budget spending from category spending.
        - spent, budget and remaining must use the provided budget values.
        - usagePercentage must use the provided budget usage percentage.
        - recommendations must be practical and based only on the provided data.
        - Return valid JSON only.
        """);

        return prompt.toString();
    }
}