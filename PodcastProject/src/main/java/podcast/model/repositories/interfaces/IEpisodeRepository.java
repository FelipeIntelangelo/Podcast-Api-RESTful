package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import podcast.model.entities.Episode;

import java.util.List;

@Repository
public interface IEpisodeRepository extends JpaRepository<Episode, Long> {
    List<Episode> findByPodcast_Id(Long podcastId);
    List<Episode> findByTitleIgnoreCase(String title);
    List<Episode> findByPodcast_IdAndTitleIgnoreCase(Long podcastId, String title);
    void deleteByTitleIgnoreCase(String title);
}
