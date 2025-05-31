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
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private Boolean isVerified = false;
    private LocalDateTime lastLogin;
    private String resetToken;
    private LocalDateTime createdAt;
}