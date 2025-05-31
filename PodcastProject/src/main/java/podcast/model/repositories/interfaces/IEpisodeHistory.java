package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import podcast.model.entities.EpisodeHistory;

@Repository
public interface IEpisodeHistory extends JpaRepository<EpisodeHistory, Long> {

}
