package podcast.entities;

import jakarta.persistence.*;
import lombok.*;
import podcast.entities.enums.Category;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Podcast podcast = (Podcast) o;
        return id != null && id.equals(podcast.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}