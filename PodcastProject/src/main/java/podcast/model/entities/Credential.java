package podcast.model.entities;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Credential {


    private String email;

    private String username;

    private String password;
    private Boolean isVerified = false;
    private LocalDateTime lastLogin;
    private String resetToken;
    private LocalDateTime createdAt;
}