package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import podcast.model.entities.Podcast;
import podcast.model.entities.enums.Category;

import java.util.List;

@Repository
public interface IPodcastRepository extends JpaRepository<Podcast, Long> {
    List<Podcast> findByUser_IdOrTitleIgnoreCaseOrCategories(Integer userId, String title, Category category);
    List<Podcast> findByUser_Credential_Username(String username);
    boolean existsByUserId(Long id);
    List<Podcast> findByUser_Id(Long userId);
    @Modifying
    @Query(value = "DELETE FROM podcasts WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserIdNative(@Param("userId") Long userId);
    @Modifying
    @Query(value = "DELETE FROM categoriesxpodcast WHERE podcast_id IN :podcastIds", nativeQuery = true)
    void deleteCategoriesByPodcastIdsNative(@Param("podcastIds") List<Long> podcastIds);
    @Modifying
    @Query(value = "DELETE FROM favorites WHERE podcast_id IN :podcastIds", nativeQuery = true)
    void deleteFavoritesByPodcastIdsNative(@Param("podcastIds") List<Long> podcastIds);
}
