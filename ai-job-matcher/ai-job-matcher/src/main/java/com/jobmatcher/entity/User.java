
package com.jobmatcher.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(
            nullable = false,
            length = 50
    )
    private String fullName;

    @Email
    @NotBlank
    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String email;

    @NotBlank
    @Column(
            nullable = false,
            length = 255
    )
    private String password;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {
    }

    public User(
            String fullName,
            String email,
            String password) {

        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

