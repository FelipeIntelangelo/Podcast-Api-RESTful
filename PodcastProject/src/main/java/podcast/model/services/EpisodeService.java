package podcast.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import podcast.model.entities.Episode;
import podcast.model.entities.Podcast;
import podcast.model.exceptions.PodcastNotFoundException;
import podcast.model.repositories.interfaces.IEpisodeRepository;
import podcast.model.repositories.interfaces.IPodcastRepository;

import java.util.List;

@Service
public class EpisodeService {

private final IEpisodeRepository episodeRepository;
private final IPodcastRepository podcastRepository;

    @Autowired
    public EpisodeService(IEpisodeRepository episodeRepository, IPodcastRepository podcastRepository) {
        this.episodeRepository = episodeRepository;
        this.podcastRepository = podcastRepository;
    }

    // SAVE
    public void save(Episode episode) {
        podcastRepository.findById(episode.getPodcast().getId()).ifPresentOrElse(
                existingPodcast -> {
                    long cantidad = existingPodcast.getEpisodes().stream()
                            .filter(p -> p.getTitle().equalsIgnoreCase(episode.getTitle()))
                            .count();
                    if (cantidad > 0) {
                        throw new IllegalArgumentException("Ya existe un episodio con el título " + episode.getTitle() + " en este podcast");
                    }
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
            throw new IllegalArgumentException("Episode with ID " + episode.getId() + " not found");
        }
        episodeRepository.save(episode);
    }

    // DELETE
    public void deleteById(Long episodeId) {
        if (!episodeRepository.existsById(episodeId)) {
            throw new IllegalArgumentException("Episode with ID " + episodeId + " not found");
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
    public List<Episode> getAllEpisodes() {
        List<Episode> episodes = episodeRepository.findAll();
        if (episodes.isEmpty()) {
            throw new IllegalArgumentException("No episodes found");
        }
        return episodes;
    }

    public Episode getEpisodeById(Long episodeId) {
        return episodeRepository.findById(episodeId).orElseThrow(() ->
                new IllegalArgumentException("Episode with ID " + episodeId + " not found"));
    }

    public List<Episode> getEpisodesByPodcastTitle(String podcastTitle) {
        Podcast podcast = podcastRepository.findAll().stream()
                .filter(p -> p.getTitle().equalsIgnoreCase(podcastTitle))
                .findFirst()
                .orElseThrow(() -> new PodcastNotFoundException("Podcast with title " + podcastTitle + " not found"));
        return podcast.getEpisodes();
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
}
