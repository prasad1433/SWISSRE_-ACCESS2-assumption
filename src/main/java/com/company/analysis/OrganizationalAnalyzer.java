package com.company.analysis;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes organizational structure and performs salary/reporting line analysis.
 * Assumptions:
 * - There is exactly one CEO (employee with no manager)
 * - All manager IDs reference valid employee IDs
 * - No circular reporting relationships exist
 * - Salary comparison uses 20%-50% range above direct subordinates' average
 */
public class OrganizationalAnalyzer {
    
    private final List<Employee> employees;
    private final Map<String, Employee> employeeMap;
    private final Map<String, List<Employee>> subordinatesMap;
    private Employee ceo;
    
    public OrganizationalAnalyzer(List<Employee> employees) {
        this.employees = new ArrayList<>(employees);
        this.employeeMap = employees.stream()
                .collect(Collectors.toMap(Employee::getId, e -> e));
        this.subordinatesMap = new HashMap<>();
        
        buildHierarchy();
    }
    
    private void buildHierarchy() {
        // Find CEO
        List<Employee> ceos = employees.stream()
                .filter(Employee::isCEO)
                .collect(Collectors.toList());
        
        if (ceos.isEmpty()) {
            throw new IllegalArgumentException("No CEO found (employee with no manager)");
        }
        if (ceos.size() > 1) {
            throw new IllegalArgumentException("Multiple CEOs found: " + 
                ceos.stream().map(Employee::getName).collect(Collectors.joining(", ")));
        }
        
        this.ceo = ceos.get(0);
        
        // Build subordinates mapping
        for (Employee employee : employees) {
            String managerId = employee.getManagerId();
            if (managerId != null) {
                subordinatesMap.computeIfAbsent(managerId, k -> new ArrayList<>()).add(employee);
                
                // Validate manager exists
                if (!employeeMap.containsKey(managerId)) {
                    throw new IllegalArgumentException(
                        String.format("Employee %s references non-existent manager %s", 
                                    employee.getId(), managerId));
                }
            }
        }
    }
    
    /**
     * Analyzes salary compliance for all managers.
     * Managers should earn 20%-50% more than the average of their direct subordinates.
     */
    public SalaryAnalysisResult analyzeSalaries() {
        List<SalaryViolation> underPaidManagers = new ArrayList<>();
        List<SalaryViolation> overPaidManagers = new ArrayList<>();
        
        for (Employee manager : employees) {
            List<Employee> directSubordinates = subordinatesMap.get(manager.getId());
            
            if (directSubordinates == null || directSubordinates.isEmpty()) {
                continue; // Not a manager
            }
            
            double averageSubordinateSalary = directSubordinates.stream()
                    .mapToDouble(Employee::getSalary)
                    .average()
                    .orElse(0.0);
            
            double minimumManagerSalary = averageSubordinateSalary * 1.20; // 20% more
            double maximumManagerSalary = averageSubordinateSalary * 1.50; // 50% more
            
            if (manager.getSalary() < minimumManagerSalary) {
                double shortage = minimumManagerSalary - manager.getSalary();
                underPaidManagers.add(new SalaryViolation(manager, shortage, averageSubordinateSalary));
            } else if (manager.getSalary() > maximumManagerSalary) {
                double excess = manager.getSalary() - maximumManagerSalary;
                overPaidManagers.add(new SalaryViolation(manager, excess, averageSubordinateSalary));
            }
        }
        
        return new SalaryAnalysisResult(underPaidManagers, overPaidManagers);
    }
    
    /**
     * Finds employees with more than 4 managers between them and the CEO.
     */
    public ReportingLineAnalysisResult analyzeReportingLines() {
        List<ReportingLineViolation> violations = new ArrayList<>();
        
        for (Employee employee : employees) {
            if (employee.isCEO()) {
                continue;
            }
            
            int managersToCEO = countManagersToCEO(employee);
            if (managersToCEO > 4) {
                int excess = managersToCEO - 4;
                violations.add(new ReportingLineViolation(employee, excess, managersToCEO));
            }
        }
        
        return new ReportingLineAnalysisResult(violations);
    }
    
    /**
     * Counts the number of managers between an employee and the CEO.
     */
    private int countManagersToCEO(Employee employee) {
        Set<String> visited = new HashSet<>();
        int count = 0;
        Employee current = employee;
        
        while (current != null && !current.isCEO()) {
            String managerId = current.getManagerId();
            
            // Detect circular references
            if (visited.contains(current.getId())) {
                throw new IllegalStateException(
                    String.format("Circular reference detected in reporting line for employee %s", 
                                employee.getId()));
            }
            visited.add(current.getId());
            
            current = employeeMap.get(managerId);
            if (current != null) {
                count++;
            }
        }
        
        return count;
    }
    
    public Employee getCEO() {
        return ceo;
    }
    
    public List<Employee> getDirectSubordinates(String managerId) {
        return subordinatesMap.getOrDefault(managerId, Collections.emptyList());
    }
    
    // Result classes
    public static class SalaryAnalysisResult {
        private final List<SalaryViolation> underPaidManagers;
        private final List<SalaryViolation> overPaidManagers;
        
        public SalaryAnalysisResult(List<SalaryViolation> underPaidManagers, List<SalaryViolation> overPaidManagers) {
            this.underPaidManagers = underPaidManagers;
            this.overPaidManagers = overPaidManagers;
        }
        
        public List<SalaryViolation> getUnderPaidManagers() { return underPaidManagers; }
        public List<SalaryViolation> getOverPaidManagers() { return overPaidManagers; }
    }
    
    public static class SalaryViolation {
        private final Employee manager;
        private final double amount;
        private final double subordinateAverage;
        
        public SalaryViolation(Employee manager, double amount, double subordinateAverage) {
            this.manager = manager;
            this.amount = amount;
            this.subordinateAverage = subordinateAverage;
        }
        
        public Employee getManager() { return manager; }
        public double getAmount() { return amount; }
        public double getSubordinateAverage() { return subordinateAverage; }
    }
    
    public static class ReportingLineAnalysisResult {
        private final List<ReportingLineViolation> violations;
        
        public ReportingLineAnalysisResult(List<ReportingLineViolation> violations) {
            this.violations = violations;
        }
        
        public List<ReportingLineViolation> getViolations() { return violations; }
    }
    
    public static class ReportingLineViolation {
        private final Employee employee;
        private final int excessManagers;
        private final int totalManagers;
        
        public ReportingLineViolation(Employee employee, int excessManagers, int totalManagers) {
            this.employee = employee;
            this.excessManagers = excessManagers;
            this.totalManagers = totalManagers;
        }
        
        public Employee getEmployee() { return employee; }
        public int getExcessManagers() { return excessManagers; }
        public int getTotalManagers() { return totalManagers; }
    }
}