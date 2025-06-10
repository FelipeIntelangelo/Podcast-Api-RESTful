package podcast.model.entities.dto;

import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EpisodeDTO {
    private String title;
    private String description;
    private String audioPath;
    private String imageUrl;
    private Duration duration;
    private Integer views;
    private Double averageRating;
    private Integer season;
    private Integer chapter;
    private LocalDateTime publicationDate;
    private String podcastTitle;

}
