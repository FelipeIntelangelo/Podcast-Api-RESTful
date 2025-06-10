package podcast.model.entities;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import podcast.model.entities.enums.Role;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Credential {

    // ── Atributos ────────────────────────────────────────────────────────────────────
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    private String email;

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 20, message = "El username debe tener entre 3 y 20 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "El username solo puede contener letras, números y guiones bajos")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @ElementCollection(targetClass = Role.class)
    private Set<Role> roles;

    private LocalDateTime createdAt;
}