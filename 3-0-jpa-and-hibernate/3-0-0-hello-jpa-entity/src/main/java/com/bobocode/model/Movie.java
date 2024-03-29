package com.bobocode.model;

import jakarta.persistence.*;
import lombok.*;

import java.lang.annotation.Target;

/**
 * TODO: you're job is to implement mapping for JPA entity {@link Movie}
 * - explicitly specify the table name
 * - specify id
 * - configure id as auto-increment column, choose an Identity generation strategy
 * - explicitly specify each column name ("id", "name", "director", and "duration" accordingly)
 * - specify not null constraint for fields {@link Movie#name} and {@link Movie#director}
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "movie")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "director", nullable = false)
    private String director;

    @Column(name = "duration")
    private Integer durationSeconds;
}