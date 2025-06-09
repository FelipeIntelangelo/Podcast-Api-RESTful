package podcast.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import podcast.model.entities.enums.Category;
import podcast.model.entities.dto.PodcastDTO;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString
@Builder
@Table(name = "Podcasts")

public class Podcast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;
    private String coverImageUrl;
    private Boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties("podcasts")
    private User user;

    @OneToMany(mappedBy = "podcast", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("podcasts")
    private List<Episode> episodes;

    @ElementCollection(targetClass = Category.class)
    @CollectionTable(name = "CategoriesXPodcast", joinColumns = @JoinColumn(name = "podcast_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private List<Category> categories;


    Long calcularViewsPromedio() {
        if (episodes == null || episodes.isEmpty()) {
            return 0L;
        }
        long totalViews = episodes.stream()
                .mapToLong(Episode::getViews)
                .sum();
        return totalViews / episodes.size();
    }

    Double calcularRatingPromedio() {
        if (episodes == null || episodes.isEmpty()) {
            return 0.0;
        }
        double totalRating = episodes.stream().
                mapToDouble(Episode::getAverageRating)
                .filter(rating -> rating > 0) // Exclude null ratings
                .sum();
        return totalRating / episodes.size();
    }

    public PodcastDTO toDTO() {
        return new PodcastDTO(this.getId(), this.getTitle(), this.getDescription(), this.getCategories(),
                calcularViewsPromedio(), calcularRatingPromedio());
    }

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