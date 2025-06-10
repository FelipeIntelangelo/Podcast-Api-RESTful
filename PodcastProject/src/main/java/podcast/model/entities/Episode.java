package podcast.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import podcast.model.entities.dto.EpisodeDTO;
import podcast.model.entities.helpers.DurationConverter;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "Episodes")

public class Episode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @NotBlank
    @Size(min = 3, max = 50, message = "El título debe tener entre 3 y 100 caracteres")
    private String title;

    @Column(nullable = false, length = 500)
    @NotBlank
    @Size(min = 5, max = 500, message = "La descripción debe tener entre 5 y 500 caracteres")
    private String description;

    @Column(nullable = false)
    private LocalDateTime publicationDate;

    @PositiveOrZero
    private Integer views;

    @Positive
    @Max(value = 10, message = "La calificación promedio debe ser entre 1 y 10")
    private Double averageRating;

    private String imageUrl;

    @NotBlank
    @Min(value = 1, message = "La temporada debe ser al menos 1")
    private Integer season;

    @NotBlank
    @Min(value = 1, message = "El capítulo debe ser al menos 1")
    private Integer chapter;

    @Column(nullable = false)
    @NotBlank(message = "La ruta del audio es obligatoria")
    private String audioPath;

    @Column(nullable = false)
    @Convert(converter = DurationConverter.class)
    private Duration duration;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "podcast_id", nullable = false)
    @JsonIgnoreProperties("episodes")
    private Podcast podcast;

    @OneToMany(mappedBy = "episode")
    @JsonIgnoreProperties("episodes")
    private List<Commentary> commentaries;

    public EpisodeDTO toDTO() {
        return EpisodeDTO.builder()
                .title(this.title)
                .description(this.description)
                .publicationDate(this.publicationDate)
                .views(this.views)
                .averageRating(this.averageRating)
                .season(this.season)
                .chapter(this.chapter)
                .audioPath(this.audioPath)
                .duration(this.duration)
                .imageUrl(this.getImageUrl())
                .podcastTitle(this.podcast.getTitle())
                .build();
    }

}