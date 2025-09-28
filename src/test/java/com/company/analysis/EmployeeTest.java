package com.company.analysis;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Employee class.
 */
class EmployeeTest {

    @Test
    void testValidEmployeeCreation() {
        Employee employee = new Employee("1", "John Doe", 50000.0, "2");
        
        assertEquals("1", employee.getId());
        assertEquals("John Doe", employee.getName());
        assertEquals(50000.0, employee.getSalary(), 0.01);
        assertEquals("2", employee.getManagerId());
        assertFalse(employee.isCEO());
    }
    
    @Test
    void testCEOCreation() {
        Employee ceo = new Employee("1", "CEO Name", 200000.0, null);
        
        assertTrue(ceo.isCEO());
        assertNull(ceo.getManagerId());
    }
    
    @Test
    void testCEOCreationWithEmptyManagerId() {
        Employee ceo = new Employee("1", "CEO Name", 200000.0, "");
        
        assertTrue(ceo.isCEO());
        assertNull(ceo.getManagerId());
    }
    
    @Test
    void testInvalidEmployeeId() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Employee(null, "John Doe", 50000.0, "2"));
        assertThrows(IllegalArgumentException.class, () -> 
            new Employee("", "John Doe", 50000.0, "2"));
        assertThrows(IllegalArgumentException.class, () -> 
            new Employee("   ", "John Doe", 50000.0, "2"));
    }
    
    @Test
    void testInvalidEmployeeName() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Employee("1", null, 50000.0, "2"));
        assertThrows(IllegalArgumentException.class, () -> 
            new Employee("1", "", 50000.0, "2"));
        assertThrows(IllegalArgumentException.class, () -> 
            new Employee("1", "   ", 50000.0, "2"));
    }
    
    @Test
    void testInvalidSalary() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Employee("1", "John Doe", 0.0, "2"));
        assertThrows(IllegalArgumentException.class, () -> 
            new Employee("1", "John Doe", -1000.0, "2"));
    }
    
    @Test
    void testEmployeeEquality() {
        Employee emp1 = new Employee("1", "John Doe", 50000.0, "2");
        Employee emp2 = new Employee("1", "Jane Smith", 60000.0, "3");
        Employee emp3 = new Employee("2", "John Doe", 50000.0, "2");
        
        assertEquals(emp1, emp2); // Same ID
        assertNotEquals(emp1, emp3); // Different ID
        assertEquals(emp1.hashCode(), emp2.hashCode());
    }
    
    @Test
    void testTrimmingWhitespace() {
        Employee employee = new Employee(" 1 ", " John Doe ", 50000.0, " 2 ");
        
        assertEquals("1", employee.getId());
        assertEquals("John Doe", employee.getName());
        assertEquals("2", employee.getManagerId());
    }
}