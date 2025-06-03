package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import podcast.model.entities.Episode;

import java.util.List;

@Service
public interface IEpisodeRepository extends JpaRepository<Episode, Long> {
    List<Episode> findByPodcast_Id(Long podcastId);
    List<Episode> findByTitleIgnoreCase(String title);
    List<Episode> findByPodcast_IdAndTitleIgnoreCase(Long podcastId, String title);
    void deleteByTitleIgnoreCase(String title);
}
