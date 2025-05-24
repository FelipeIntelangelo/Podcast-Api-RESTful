package podcast.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Credential {
    private String email;
    private String username;
    private String password;
    private Boolean isVerified = false;
    private LocalDateTime lastLogin;
    private String resetToken;
    private LocalDateTime createdAt;
}