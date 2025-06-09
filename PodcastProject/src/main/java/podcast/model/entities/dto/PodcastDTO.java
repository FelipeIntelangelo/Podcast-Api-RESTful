package podcast.model.entities.dto;

import podcast.model.entities.enums.Category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Getter
@Setter
@ToString

public class PodcastDTO {
    private Long id;
    private String title;
    private String description;
    private List<Category> category;
    private Long averageViews;
    private Double averageRating;
}