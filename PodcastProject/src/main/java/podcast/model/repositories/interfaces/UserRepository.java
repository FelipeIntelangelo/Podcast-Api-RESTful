package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import podcast.model.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository <User, Integer> {
    Optional<User> save(User user);

}
