package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import podcast.model.entities.Commentary;

public interface ICommentaryRepository extends JpaRepository<Commentary, Long> {
}
