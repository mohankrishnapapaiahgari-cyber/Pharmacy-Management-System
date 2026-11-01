package com.example.demo.service;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service — validates credentials against the EMPLOYEE table.
 *
 * NOTE: For this academic demo, passwords are compared as plain text.
 * In production, use BCrypt hashing via Spring Security's PasswordEncoder.
 */
@Service
public class AuthService {

    private final EmployeeRepository employeeRepository;

    public AuthService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Authenticates an employee by email and password.
     *
     * @param request the login credentials
     * @return LoginResponseDTO with employee details and role
     * @throws RuntimeException if credentials are invalid or account is inactive
     */
    @Transactional(readOnly = true)
    public LoginResponseDTO authenticate(LoginRequestDTO request) {

        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        // Check password (plain text comparison for demo; use BCrypt in production)
        if (!employee.getPasswordHash().equals(request.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        // Check active status
        if (employee.getIsActive() == null || !employee.getIsActive()) {
            throw new RuntimeException("Account is deactivated. Contact your administrator.");
        }

        // Build response
        String fullName = employee.getFirstName() + " " + employee.getLastName();
        String roleName = employee.getRole().getRoleName();

        return new LoginResponseDTO(
                employee.getEmployeeId(),
                fullName,
                roleName,
                employee.getEmail()
        );
    }
}
