package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import podcast.model.entities.Podcast;

public interface PodcastRepository extends JpaRepository<Podcast, Long> {
    
}
