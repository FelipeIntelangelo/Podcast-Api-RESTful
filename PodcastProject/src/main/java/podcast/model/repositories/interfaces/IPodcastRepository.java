package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import podcast.model.entities.Podcast;

@Repository
public interface IPodcastRepository extends JpaRepository<Podcast, Long> {
    
}
