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
    @Autowired
    IEpisodeRepository episodeRepository;
    IPodcastRepository podcastRepository;

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

    public List<Episode> getAllEpisodes() {
        List<Episode> episodes = episodeRepository.findAll();
        if (episodes.isEmpty()) {
            throw new IllegalArgumentException("No episodes found");
        }
        return episodes;
    }

    public List<Episode> getEpisodesByPodcastTitle(String podcastTitle) {
        Podcast podcast = podcastRepository.findAll().stream()
                .filter(p -> p.getTitle().equalsIgnoreCase(podcastTitle))
                .findFirst()
                .orElseThrow(() -> new PodcastNotFoundException("Podcast with title " + podcastTitle + " not found"));
        return podcast.getEpisodes();
    }

    public void update(Episode episode) {

        if (!episodeRepository.existsById(episode.getId())) {
            throw new IllegalArgumentException("Episode with ID " + episode.getId() + " not found");
        }
        episodeRepository.save(episode);
    }

    public Episode getEpisodeById(Long episodeId) {
        return episodeRepository.findById(episodeId).orElseThrow(() ->
                new IllegalArgumentException("Episode with ID " + episodeId + " not found"));
    }


}
