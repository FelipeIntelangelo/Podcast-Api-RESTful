package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import podcast.model.entities.Commentary;

import java.util.List;

@Repository
public interface ICommentaryRepository extends JpaRepository<Commentary, Long> {
    List<Commentary> findByUserId(Long userId);
    @Modifying
    @Query(value = "DELETE FROM commentaries WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserIdNative(@Param("userId") Long userId);
    @Modifying
    @Query(value = "DELETE FROM commentaries WHERE episode_id = :episodeId", nativeQuery = true)
    void deleteByEpisodeIdNative(@Param("episodeId") Integer episodeId);

}
