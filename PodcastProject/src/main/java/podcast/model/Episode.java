package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Episodes")
public class Episode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDateTime publicationDate;

    private Integer season;
    private Integer chapter;

    @Column(nullable = false)
    private String audioUrl;

    private Integer durationSeconds;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "podcast_id", nullable = false)
    private Podcast podcast;

    @OneToMany(mappedBy = "episode")
    private List<Commentary> commentaries;

    // getters y setters
}