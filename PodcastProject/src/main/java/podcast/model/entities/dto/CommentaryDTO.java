package podcast.model.entities.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentaryDTO {
    private String content;
    private String userName;
    private LocalDateTime createdAt;
}
