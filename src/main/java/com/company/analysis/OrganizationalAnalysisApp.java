package com.company.analysis;

import java.io.IOException;
import java.util.List;

/**
 * Main application class for organizational analysis.
 * Reads employee data from CSV file and reports salary and reporting line issues.
 */
public class OrganizationalAnalysisApp {
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java OrganizationalAnalysisApp <csv-file-path>");
            System.exit(1);
        }
        
        String csvFilePath = args[0];
        
        try {
            // Read employee data
            List<Employee> employees = EmployeeCsvReader.readEmployees(csvFilePath);
            System.out.println("Loaded " + employees.size() + " employees from " + csvFilePath);
            System.out.println();
            
            // Create analyzer
            OrganizationalAnalyzer analyzer = new OrganizationalAnalyzer(employees);
            
            // Analyze salaries
            System.out.println("=== SALARY ANALYSIS ===");
            OrganizationalAnalyzer.SalaryAnalysisResult salaryResult = analyzer.analyzeSalaries();
            printSalaryAnalysis(salaryResult);
            
            // Analyze reporting lines
            System.out.println("=== REPORTING LINE ANALYSIS ===");
            OrganizationalAnalyzer.ReportingLineAnalysisResult reportingResult = analyzer.analyzeReportingLines();
            printReportingLineAnalysis(reportingResult);
            
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error analyzing organization: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private static void printSalaryAnalysis(OrganizationalAnalyzer.SalaryAnalysisResult result) {
        // Managers earning less than they should
        List<OrganizationalAnalyzer.SalaryViolation> underPaid = result.getUnderPaidManagers();
        if (underPaid.isEmpty()) {
            System.out.println("✓ No managers earning less than they should.");
        } else {
            System.out.println("⚠ Managers earning LESS than they should:");
            for (OrganizationalAnalyzer.SalaryViolation violation : underPaid) {
                Employee manager = violation.getManager();
                System.out.printf("  • %s (ID: %s): earning $%.2f, should earn at least $%.2f (shortage: $%.2f)%n",
                    manager.getName(),
                    manager.getId(),
                    manager.getSalary(),
                    manager.getSalary() + violation.getAmount(),
                    violation.getAmount());
                System.out.printf("    Direct subordinates average: $%.2f%n",
                    violation.getSubordinateAverage());
            }
        }
        System.out.println();
        
        // Managers earning more than they should
        List<OrganizationalAnalyzer.SalaryViolation> overPaid = result.getOverPaidManagers();
        if (overPaid.isEmpty()) {
            System.out.println("✓ No managers earning more than they should.");
        } else {
            System.out.println("⚠ Managers earning MORE than they should:");
            for (OrganizationalAnalyzer.SalaryViolation violation : overPaid) {
                Employee manager = violation.getManager();
                System.out.printf("  • %s (ID: %s): earning $%.2f, should earn at most $%.2f (excess: $%.2f)%n",
                    manager.getName(),
                    manager.getId(),
                    manager.getSalary(),
                    manager.getSalary() - violation.getAmount(),
                    violation.getAmount());
                System.out.printf("    Direct subordinates average: $%.2f%n",
                    violation.getSubordinateAverage());
            }
        }
        System.out.println();
    }
    
    private static void printReportingLineAnalysis(OrganizationalAnalyzer.ReportingLineAnalysisResult result) {
        List<OrganizationalAnalyzer.ReportingLineViolation> violations = result.getViolations();
        
        if (violations.isEmpty()) {
            System.out.println("✓ No employees with reporting lines longer than 4 managers.");
        } else {
            System.out.println("⚠ Employees with reporting lines longer than 4 managers:");
            for (OrganizationalAnalyzer.ReportingLineViolation violation : violations) {
                Employee employee = violation.getEmployee();
                System.out.printf("  • %s (ID: %s): %d managers to CEO (%d more than recommended)%n",
                    employee.getName(),
                    employee.getId(),
                    violation.getTotalManagers(),
                    violation.getExcessManagers());
            }
        }
        System.out.println();
    }
}