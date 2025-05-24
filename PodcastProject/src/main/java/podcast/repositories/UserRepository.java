package podcast.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import podcast.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository <User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    boolean existsById(Integer id);
}
