package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import podcast.model.entities.User;

public interface UserRepository extends JpaRepository <User, Integer> {
}
