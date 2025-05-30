package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import podcast.model.entities.User;

public interface IUserRepository extends JpaRepository <User, Integer> {
}
