package podcast.model.entities.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private Integer id;
    private String nickname;
    private String profilePicture;
    private String bio;
}