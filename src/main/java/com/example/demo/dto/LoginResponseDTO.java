package com.example.demo.dto;

/**
 * Login response payload — returned on successful authentication.
 * Contains the employee's identity and their assigned role for frontend RBAC.
 */
public class LoginResponseDTO {

    private Long employeeId;
    private String fullName;
    private String roleName;
    private String email;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(Long employeeId, String fullName, String roleName, String email) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.roleName = roleName;
        this.email = email;
    }

    // --- Getters & Setters ---

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
