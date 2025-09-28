package com.company.analysis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmployeeCsvReader.
 */
class EmployeeCsvReaderTest {

    @TempDir
    Path tempDir;
    
    @Test
    void testReadValidCsv() throws IOException {
        String csvContent = "id,name,salary,managerId\n" +
                           "1,John CEO,200000,\n" +
                           "2,Jane Manager,120000,1\n" +
                           "3,Bob Employee,80000,2\n";
        
        Path csvFile = tempDir.resolve("employees.csv");
        Files.write(csvFile, csvContent.getBytes());
        
        List<Employee> employees = EmployeeCsvReader.readEmployees(csvFile.toString());
        
        assertEquals(3, employees.size());
        
        Employee ceo = employees.get(0);
        assertEquals("1", ceo.getId());
        assertEquals("John CEO", ceo.getName());
        assertEquals(200000.0, ceo.getSalary(), 0.01);
        assertNull(ceo.getManagerId());
        
        Employee manager = employees.get(1);
        assertEquals("2", manager.getId());
        assertEquals("Jane Manager", manager.getName());
        assertEquals(120000.0, manager.getSalary(), 0.01);
        assertEquals("1", manager.getManagerId());
        
        Employee employee = employees.get(2);
        assertEquals("3", employee.getId());
        assertEquals("Bob Employee", employee.getName());
        assertEquals(80000.0, employee.getSalary(), 0.01);
        assertEquals("2", employee.getManagerId());
    }
    
    @Test
    void testReadCsvWithQuotedFields() throws IOException {
        String csvContent = "id,name,salary,managerId\n" +
                           "1,\"John, CEO\",200000,\n" +
                           "2,\"Jane \"\"The Boss\"\" Manager\",120000,1\n";
        
        Path csvFile = tempDir.resolve("employees.csv");
        Files.write(csvFile, csvContent.getBytes());
        
        List<Employee> employees = EmployeeCsvReader.readEmployees(csvFile.toString());
        
        assertEquals(2, employees.size());
        assertEquals("John, CEO", employees.get(0).getName());
        assertEquals("Jane \"The Boss\" Manager", employees.get(1).getName());
    }
    
    @Test
    void testEmptyFile() throws IOException {
        Path csvFile = tempDir.resolve("empty.csv");
        Files.write(csvFile, "id,name,salary,managerId\n".getBytes());
        
        List<Employee> employees = EmployeeCsvReader.readEmployees(csvFile.toString());
        
        assertTrue(employees.isEmpty());
    }
    
    @Test
    void testSkipEmptyLines() throws IOException {
        String csvContent = "id,name,salary,managerId\n" +
                           "\n" +
                           "1,John CEO,200000,\n" +
                           "\n" +
                           "2,Jane Manager,120000,1\n" +
                           "\n";
        
        Path csvFile = tempDir.resolve("employees.csv");
        Files.write(csvFile, csvContent.getBytes());
        
        List<Employee> employees = EmployeeCsvReader.readEmployees(csvFile.toString());
        
        assertEquals(2, employees.size());
    }
    
    @Test
    void testInvalidFieldCount() throws IOException {
        String csvContent = "id,name,salary,managerId\n" +
                           "1,John CEO,200000\n";
        
        Path csvFile = tempDir.resolve("employees.csv");
        Files.write(csvFile, csvContent.getBytes());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            EmployeeCsvReader.readEmployees(csvFile.toString()));
        
        assertTrue(exception.getMessage().contains("Expected 4 fields"));
    }
    
    @Test
    void testInvalidSalaryFormat() throws IOException {
        String csvContent = "id,name,salary,managerId\n" +
                           "1,John CEO,not-a-number,\n";
        
        Path csvFile = tempDir.resolve("employees.csv");
        Files.write(csvFile, csvContent.getBytes());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            EmployeeCsvReader.readEmployees(csvFile.toString()));
        
        assertTrue(exception.getMessage().contains("Invalid salary format"));
    }
    
    @Test
    void testFileNotFound() {
        String nonExistentFile = tempDir.resolve("nonexistent.csv").toString();
        
        assertThrows(IOException.class, () -> 
            EmployeeCsvReader.readEmployees(nonExistentFile));
    }
}