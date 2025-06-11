package podcast.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import podcast.model.entities.Commentary;
import podcast.model.entities.Episode;
import podcast.model.exceptions.EpisodeNotFoundException;
import podcast.model.exceptions.ChapterOrSeasonInvalidException;
import podcast.model.exceptions.PodcastNotFoundException;
import podcast.model.repositories.interfaces.IEpisodeHistoryRepository;
import podcast.model.repositories.interfaces.IEpisodeRepository;
import podcast.model.repositories.interfaces.IPodcastRepository;
import podcast.model.repositories.interfaces.IUserRepository;

import java.util.List;

@Service
public class EpisodeService {

private final IEpisodeRepository episodeRepository;
private final IPodcastRepository podcastRepository;
private final IEpisodeHistoryRepository episodeHistoryRepository;
private final IUserRepository userRepository;

    @Autowired
    public EpisodeService(IEpisodeRepository episodeRepository,
                          IPodcastRepository podcastRepository,
                          IEpisodeHistoryRepository episodeHistoryRepository,
                          IUserRepository userRepository) {
        this.episodeRepository = episodeRepository;
        this.podcastRepository = podcastRepository;
        this.episodeHistoryRepository = episodeHistoryRepository;
        this.userRepository = userRepository;
    }

    // SAVE
    public void save(Episode episode) {
        podcastRepository.findById(episode.getPodcast().getId()).ifPresentOrElse(
                existingPodcast -> {
                    // VALIDA QUE NO EXISTA UN EPISODIO CON EL MISMO TÍTULO
                    long cantidad = existingPodcast.getEpisodes().stream()
                            .filter(p -> p.getTitle().equalsIgnoreCase(episode.getTitle()))
                            .count();
                    if (cantidad > 0) {
                        throw new IllegalArgumentException("The episode with the title " + episode.getTitle() + " already exists");
                    }
                    // BUSCA ULTIMO EPISODIO POR FECHA Y VALIDA SEASON Y CHAPTER
                    existingPodcast.getEpisodes().stream()
                            .max((e1, e2) -> e1.getCreatedAt().compareTo(e2.getCreatedAt()))
                            .ifPresent(ultimo -> {
                                if  (episode.getSeason() < ultimo.getSeason() ||
                                    (episode.getSeason().equals(ultimo.getSeason()) && episode.getChapter() <= ultimo.getChapter())) {
                                    throw new ChapterOrSeasonInvalidException("The episode must have a season and/or chapter greater than the last one (" +
                                            "Season: " + ultimo.getSeason() + ", Chapter: " + ultimo.getChapter() + ")");
                                }
                                if (episode.getSeason() > ultimo.getSeason() && episode.getChapter() != 1) {
                                    throw new ChapterOrSeasonInvalidException("If the season is greater, the chapter must be 1");
                                }
                            });
                    episodeRepository.save(episode);
                    existingPodcast.getEpisodes().add(episode);
                    podcastRepository.save(existingPodcast);
                },
                () -> {
                    throw new PodcastNotFoundException("Podcast with name " + episode.getPodcast().getTitle() + " not found");
                }
        );
    }

    // UPDATE
    public void update(Episode episode) {

        if (!episodeRepository.existsById(Long.valueOf(episode.getId()))) {
            throw new EpisodeNotFoundException("Episode with ID " + episode.getId() + " not found");
        }
        episodeRepository.save(episode);
    }

    // DELETE
    public void deleteById(Long episodeId) {
        if (!episodeRepository.existsById(episodeId)) {
            throw new EpisodeNotFoundException("Episode with ID " + episodeId + " not found");
        }
        episodeRepository.deleteById(episodeId);
    }

    public void deleteByTitle(String title){
        // PUEDE HABER MAS DE UN EPISODIO CON EL MISMO TÍTULO ???????
        List<Episode> episodes = episodeRepository.findByTitleIgnoreCase(title);
        if (episodes.isEmpty()) {
            throw new PodcastNotFoundException("Podcast with title " + title + " not found");
        }
        for (Episode episode : episodes) {
            episodeRepository.delete(episode);
        }
    }

    // MOSTRAR - GETS

    public Episode getEpisodeById(Long episodeId) {
        return episodeRepository.findById(episodeId).orElseThrow(() ->
                new EpisodeNotFoundException("Episode with ID " + episodeId + " not found"));
    }

    public String getAudioUrl(Long episodeId) {
        Episode episode = episodeRepository.findById(episodeId).orElseThrow(() ->
                new EpisodeNotFoundException("Episode with ID " + episodeId + " not found"));
        return episode.getAudioPath();
    }



    public List<Episode> getAllFiltered(String title, Long podcastId) {
        List<Episode> filtered;

        if (title == null && podcastId == null) {
            filtered = episodeRepository.findAll();
        } else if (podcastId != null) {
            filtered = episodeRepository.findByPodcast_Id(podcastId);
            if (filtered.isEmpty()) {
                throw new PodcastNotFoundException("No episodes found for podcast ID " + podcastId);
            }
        } else {
            filtered = episodeRepository.findByTitleIgnoreCase(title);
            if (filtered.isEmpty()) {
                throw new PodcastNotFoundException("No episodes found with title " + title);
            }
        }
        return filtered;
    }


    public void commentEpisode(Long episodeId, String comment, String username) {
        episodeHistoryRepository.findByEpisode_IdAndUser_Id(episodeId, Long.valueOf(username))
                .orElseThrow(() -> new EpisodeNotFoundException("Episode not viewed for: " + episodeId + " and user ID: " + username));
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new EpisodeNotFoundException("Episode not found for ID: " + episodeId));
        Commentary commentary = Commentary.builder()
                .content(comment)
                .user(userRepository.findByCredentialUsernameFetch(username)
                        .orElseThrow(() -> new EpisodeNotFoundException("User not found with username: " + username)))
                .episode(episode)
                .build();
        episode.getCommentaries().add(commentary);
        episodeRepository.save(episode);
    }

    public List<Episode> getEpisodesByMostViews() {
        if (episodeRepository.findAll().isEmpty()) {
            throw new IllegalArgumentException("No episodes found");
        }
        return episodeRepository.findAllByOrderByViewsDesc();
    }
}
