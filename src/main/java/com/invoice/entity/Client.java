package com.invoice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client extends BaseEntity {

    @NotBlank(message = "Client name is required")
    @Column(nullable = false)
    private String name;

    @Email(message = "Invalid email")
    @Column(nullable = false)
    private String email;

    private String phone;
    private String address;
    private String companyName;

    // Many clients belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // One client has many invoices
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Invoice> invoices;
}
