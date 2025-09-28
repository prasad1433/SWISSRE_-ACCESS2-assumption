# Organizational Analysis Tool

This is a Java application that analyzes organizational structure and identifies potential salary and reporting line issues.

## Features

The application analyzes:
- **Salary Compliance**: Identifies managers who earn less than 20% or more than 50% above their direct subordinates' average salary
- **Reporting Line Length**: Identifies employees with more than 4 managers between them and the CEO

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Building the Project

```bash
mvn clean compile
```

## Running Tests

```bash
mvn test
```

## Running the Application

### Using Maven

```bash
mvn exec:java -Dexec.mainClass="com.company.analysis.OrganizationalAnalysisApp" -Dexec.args="path/to/employees.csv"
```

### Using Java directly

```bash
# Compile first
mvn compile

# Run with classpath
java -cp target/classes com.company.analysis.OrganizationalAnalysisApp path/to/employees.csv
```

### Example with sample data

```bash
mvn exec:java -Dexec.mainClass="com.company.analysis.OrganizationalAnalysisApp" -Dexec.args="sample_employees.csv"
```

## CSV File Format

The CSV file should contain employee data with the following columns:
- `id`: Unique employee identifier
- `name`: Employee name
- `salary`: Employee salary (positive number)
- `managerId`: Manager's employee ID (empty for CEO)

Example:
```csv
id,name,salary,managerId
1,John CEO,200000,
2,Jane Manager,120000,1
3,Bob Employee,80000,2
```

## Assumptions Made

1. **Single CEO**: There is exactly one employee with no manager (CEO)
2. **Valid References**: All manager IDs reference existing employee IDs
3. **No Circular Reporting**: No circular reporting relationships exist
4. **Positive Salaries**: All salaries are positive numbers
5. **Salary Comparison**: Managers should earn 20-50% more than the average of their direct subordinates
6. **Reporting Line Limit**: No more than 4 managers should be between any employee and the CEO

## Output

The application prints analysis results to the console:

### Salary Analysis
- Lists managers earning less than they should (with shortage amount)
- Lists managers earning more than they should (with excess amount)

### Reporting Line Analysis
- Lists employees with reporting lines longer than 4 managers

## Architecture

- `Employee`: Data model representing an employee
- `EmployeeCsvReader`: Utility for reading CSV files and parsing employee data
- `OrganizationalAnalyzer`: Main analysis engine for salary and reporting line checks
- `OrganizationalAnalysisApp`: Main application class with console output

## Error Handling

The application handles various error conditions:
- Invalid CSV format
- Missing or invalid employee data
- Circular reporting relationships
- Multiple or missing CEOs
- File I/O errors