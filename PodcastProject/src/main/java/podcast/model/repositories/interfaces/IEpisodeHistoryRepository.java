package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import podcast.model.entities.EpisodeHistory;

import java.util.Optional;

@Repository
public interface IEpisodeHistoryRepository extends JpaRepository<EpisodeHistory, Long> {
    Optional<EpisodeHistory> findByEpisode_IdAndUser_Id(Long episodeId, Long userId);

}
