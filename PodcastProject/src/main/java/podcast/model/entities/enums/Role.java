package podcast.model.entities.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ADMIN,
    CREATOR,
    USER,
    GUEST;

    @Override
    public String getAuthority() {
        return name();
    }
}
