package com.company.analysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OrganizationalAnalyzer.
 */
class OrganizationalAnalyzerTest {

    private List<Employee> testEmployees;
    private OrganizationalAnalyzer analyzer;
    
    @BeforeEach
    void setUp() {
        // Create a test organization structure:
        // CEO (1) - $200K
        // ├── Manager A (2) - $120K
        // │   ├── Employee A1 (3) - $80K
        // │   └── Employee A2 (4) - $85K
        // └── Manager B (5) - $160K (overpaid)
        //     ├── Employee B1 (6) - $90K
        //     └── Manager C (7) - $95K
        //         └── Employee C1 (8) - $70K
        //             └── Manager D (9) - $75K
        //                 └── Employee D1 (10) - $60K (too deep)
        
        testEmployees = Arrays.asList(
            new Employee("1", "CEO", 200000.0, null),
            new Employee("2", "Manager A", 120000.0, "1"),
            new Employee("3", "Employee A1", 80000.0, "2"),
            new Employee("4", "Employee A2", 85000.0, "2"),
            new Employee("5", "Manager B", 160000.0, "1"), // Overpaid
            new Employee("6", "Employee B1", 90000.0, "5"),
            new Employee("7", "Manager C", 95000.0, "5"),
            new Employee("8", "Employee C1", 70000.0, "7"),
            new Employee("9", "Manager D", 75000.0, "8"), // Underpaid
            new Employee("10", "Employee D1", 60000.0, "9") // 5 levels deep
        );
        
        analyzer = new OrganizationalAnalyzer(testEmployees);
    }
    
    @Test
    void testFindCEO() {
        Employee ceo = analyzer.getCEO();
        assertNotNull(ceo);
        assertEquals("1", ceo.getId());
        assertEquals("CEO", ceo.getName());
    }
    
    @Test
    void testNoCEO() {
        List<Employee> noCEOEmployees = Arrays.asList(
            new Employee("1", "Manager", 100000.0, "2"),
            new Employee("2", "Manager", 100000.0, "1")
        );
        
        assertThrows(IllegalArgumentException.class, () -> 
            new OrganizationalAnalyzer(noCEOEmployees));
    }
    
    @Test
    void testMultipleCEOs() {
        List<Employee> multipleCEOs = Arrays.asList(
            new Employee("1", "CEO 1", 200000.0, null),
            new Employee("2", "CEO 2", 200000.0, null)
        );
        
        assertThrows(IllegalArgumentException.class, () -> 
            new OrganizationalAnalyzer(multipleCEOs));
    }
    
    @Test
    void testInvalidManagerReference() {
        List<Employee> invalidManager = Arrays.asList(
            new Employee("1", "CEO", 200000.0, null),
            new Employee("2", "Employee", 80000.0, "999") // Non-existent manager
        );
        
        assertThrows(IllegalArgumentException.class, () -> 
            new OrganizationalAnalyzer(invalidManager));
    }
    
    @Test
    void testSalaryAnalysis() {
        OrganizationalAnalyzer.SalaryAnalysisResult result = analyzer.analyzeSalaries();
        
        // Check underpaid managers
        List<OrganizationalAnalyzer.SalaryViolation> underPaid = result.getUnderPaidManagers();
        assertEquals(1, underPaid.size());
        
        OrganizationalAnalyzer.SalaryViolation underPaidViolation = underPaid.get(0);
        assertEquals("9", underPaidViolation.getManager().getId()); // Manager D
        assertEquals(70000.0, underPaidViolation.getSubordinateAverage(), 0.01); // Employee D1 salary
        double expectedMinSalary = 70000.0 * 1.20; // 84K
        double shortage = expectedMinSalary - 75000.0; // 9K
        assertEquals(shortage, underPaidViolation.getAmount(), 0.01);
        
        // Check overpaid managers
        List<OrganizationalAnalyzer.SalaryViolation> overPaid = result.getOverPaidManagers();
        assertEquals(1, overPaid.size());
        
        OrganizationalAnalyzer.SalaryViolation overPaidViolation = overPaid.get(0);
        assertEquals("5", overPaidViolation.getManager().getId()); // Manager B
        double subordinateAverage = (90000.0 + 95000.0) / 2; // 92.5K
        assertEquals(subordinateAverage, overPaidViolation.getSubordinateAverage(), 0.01);
        double expectedMaxSalary = subordinateAverage * 1.50; // 138.75K
        double excess = 160000.0 - expectedMaxSalary; // 21.25K
        assertEquals(excess, overPaidViolation.getAmount(), 0.01);
    }
    
    @Test
    void testReportingLineAnalysis() {
        OrganizationalAnalyzer.ReportingLineAnalysisResult result = analyzer.analyzeReportingLines();
        
        List<OrganizationalAnalyzer.ReportingLineViolation> violations = result.getViolations();
        assertEquals(1, violations.size());
        
        OrganizationalAnalyzer.ReportingLineViolation violation = violations.get(0);
        assertEquals("10", violation.getEmployee().getId()); // Employee D1
        assertEquals(5, violation.getTotalManagers()); // 5 managers between D1 and CEO
        assertEquals(1, violation.getExcessManagers()); // 1 more than allowed
    }
    
    @Test
    void testGetDirectSubordinates() {
        List<Employee> ceoSubordinates = analyzer.getDirectSubordinates("1");
        assertEquals(2, ceoSubordinates.size());
        assertTrue(ceoSubordinates.stream().anyMatch(e -> e.getId().equals("2")));
        assertTrue(ceoSubordinates.stream().anyMatch(e -> e.getId().equals("5")));
        
        List<Employee> managerASubordinates = analyzer.getDirectSubordinates("2");
        assertEquals(2, managerASubordinates.size());
        assertTrue(managerASubordinates.stream().anyMatch(e -> e.getId().equals("3")));
        assertTrue(managerASubordinates.stream().anyMatch(e -> e.getId().equals("4")));
        
        List<Employee> noSubordinates = analyzer.getDirectSubordinates("10");
        assertTrue(noSubordinates.isEmpty());
    }
    
    @Test
    void testValidSalaryRanges() {
        // Test employees with salaries within the valid range
        List<Employee> validSalaryEmployees = Arrays.asList(
            new Employee("1", "CEO", 200000.0, null),
            new Employee("2", "Manager", 120000.0, "1"), // 20% more than subordinate average
            new Employee("3", "Employee", 100000.0, "2")
        );
        
        OrganizationalAnalyzer validAnalyzer = new OrganizationalAnalyzer(validSalaryEmployees);
        OrganizationalAnalyzer.SalaryAnalysisResult result = validAnalyzer.analyzeSalaries();
        
        assertTrue(result.getUnderPaidManagers().isEmpty());
        assertTrue(result.getOverPaidManagers().isEmpty());
    }
}