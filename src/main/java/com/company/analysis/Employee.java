package com.company.analysis;

import java.util.Objects;

/**
 * Represents an employee in the organization.
 * Assumptions:
 * - Employee ID is unique and positive
 * - CEO has no manager (managerId is null)
 * - Salary is always positive
 * - Employee name is non-null and non-empty
 */
public class Employee {
    private final String id;
    private final String name;
    private final double salary;
    private final String managerId; // null for CEO
    
    public Employee(String id, String name, double salary, String managerId) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be null or empty");
        }
        if (salary <= 0) {
            throw new IllegalArgumentException("Salary must be positive");
        }
        
        this.id = id.trim();
        this.name = name.trim();
        this.salary = salary;
        this.managerId = (managerId == null || managerId.trim().isEmpty()) ? null : managerId.trim();
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public double getSalary() {
        return salary;
    }
    
    public String getManagerId() {
        return managerId;
    }
    
    public boolean isCEO() {
        return managerId == null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Employee{id='%s', name='%s', salary=%.2f, managerId='%s'}", 
                           id, name, salary, managerId);
    }
}