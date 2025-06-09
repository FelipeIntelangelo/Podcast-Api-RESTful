package podcast.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
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
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDateTime publicationDate;

    private Integer views;

    private Double averageRating;

    private Integer season;

    private Integer chapter;

    @Column(nullable = false)
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
    private List<Commentary> commentaries;

}