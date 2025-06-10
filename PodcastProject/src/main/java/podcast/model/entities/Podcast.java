package podcast.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank
    @Size(min = 1, max = 100, message = "Title must be less than 100 characters, and not blank")
    private String title;

    @NotBlank
    @Size(min = 1, max = 500, message = "Description must be less than 500 characters, and not blank")
    private String description;

    @Column(name = "image_url")
    private String ImageUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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