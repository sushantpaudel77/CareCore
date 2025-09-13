package com.hospital_management.hotpital_management.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String specialization;

    @Column(nullable = false, unique  = true, length = 100)
    private String email;

    @ManyToMany(mappedBy = "doctors")
    @ToString.Exclude
    private Set<Department> departments = new HashSet<>();
}
