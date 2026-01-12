package com.example.finance.core.model;

import java.util.*;

public class Wallet {
    private List<Operation> operations = new ArrayList<>();
    private Set<String> categories = new HashSet<>();
    private Map<String, Double> budgetsByCategory = new HashMap<>();

    public List<Operation> getOperations() { return operations; }
    public void setOperations(List<Operation> operations) { this.operations = operations; }

    public Set<String> getCategories() { return categories; }
    public void setCategories(Set<String> categories) { this.categories = categories; }

    public Map<String, Double> getBudgetsByCategory() { return budgetsByCategory; }
    public void setBudgetsByCategory(Map<String, Double> budgetsByCategory) { this.budgetsByCategory = budgetsByCategory; }
}
