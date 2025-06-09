package podcast.model.entities.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {
    // Getters y setters
    private String username;
    private String password;

}