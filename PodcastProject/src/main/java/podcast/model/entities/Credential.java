package podcast.model.entities;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import podcast.model.entities.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Credential {

    private String email;

    private String username;

    private String password;

    @ElementCollection(targetClass = Role.class)
    private Set<Role> roles;

    private Boolean isVerified = false;
    private LocalDateTime lastLogin;
    private String resetToken;
    private LocalDateTime createdAt;
}