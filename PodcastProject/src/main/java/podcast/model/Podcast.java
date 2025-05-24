package podcast.model;

    import jakarta.persistence.*;
    import java.time.LocalDateTime;
    import java.util.List;

    @Entity
    @Table(name = "Podcasts")
    public class Podcast {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @Column(nullable = false)
        private String title;

        private String description;
        private String coverImageUrl;
        private Boolean isActive = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        @ManyToOne
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @OneToMany(mappedBy = "podcast", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Episode> episodes;

        @ElementCollection(targetClass = Category.class)
        @CollectionTable(name = "CategoriesXPodcast", joinColumns = @JoinColumn(name = "podcast_id"))
        @Enumerated(EnumType.STRING)
        @Column(name = "category")
        private List<Category> categories;

        // getters y setters
    }