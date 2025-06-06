package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import podcast.model.entities.User;
@Repository
public interface IUserRepository extends JpaRepository <User, Integer> {
}
