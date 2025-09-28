package com.company.analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reading employee data from CSV files.
 * Assumptions:
 * - CSV format: id,name,salary,managerId
 * - Header line is expected and will be skipped
 * - Empty managerId field indicates CEO
 * - All fields are properly quoted/escaped if needed
 */
public class EmployeeCsvReader {
    
    /**
     * Reads employee data from a CSV file.
     * 
     * @param filePath path to the CSV file
     * @return list of Employee objects
     * @throws IOException if file cannot be read
     * @throws IllegalArgumentException if CSV format is invalid
     */
    public static List<Employee> readEmployees(String filePath) throws IOException {
        List<Employee> employees = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }
                
                // Skip header line
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                try {
                    Employee employee = parseCsvLine(line);
                    employees.add(employee);
                } catch (Exception e) {
                    throw new IllegalArgumentException(
                        String.format("Error parsing line %d: %s. Error: %s", lineNumber, line, e.getMessage()), e);
                }
            }
        }
        
        return employees;
    }
    
    /**
     * Parses a single CSV line into an Employee object.
     * Handles basic CSV parsing including quoted fields.
     */
    private static Employee parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            
            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField.setLength(0);
            } else {
                currentField.append(ch);
            }
        }
        
        // Add the last field
        fields.add(currentField.toString().trim());
        
        if (fields.size() != 4) {
            throw new IllegalArgumentException(
                String.format("Expected 4 fields (id,name,salary,managerId) but found %d", fields.size()));
        }
        
        String id = fields.get(0);
        String name = fields.get(1);
        double salary;
        String managerId = fields.get(3);
        
        try {
            salary = Double.parseDouble(fields.get(2));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid salary format: " + fields.get(2), e);
        }
        
        // Handle empty managerId (CEO case)
        if (managerId.isEmpty()) {
            managerId = null;
        }
        
        return new Employee(id, name, salary, managerId);
    }
}