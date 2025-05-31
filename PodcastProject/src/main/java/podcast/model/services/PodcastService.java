package podcast.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import podcast.model.entities.Podcast;
import podcast.model.exceptions.PodcastNotFoundException;
import podcast.model.repositories.interfaces.IPodcastRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PodcastService {
    @Autowired
    IPodcastRepository podcastRepository;


public void saveOrReplace(Podcast podcast) {
    podcastRepository.findAll().stream()
            .filter(podcastpvt -> podcastpvt.getTitle().equals(podcast.getTitle()))
            .findFirst()
            .ifPresent(podcastpvt -> {
                throw new PodcastNotFoundException("Podcast with name " + podcast.getTitle() + " already exists");
            });
    podcastRepository.save(podcast);
}
    public List<Podcast> getAllPodcasts() {
        List<Podcast> podcasts = podcastRepository.findAll();
        if (podcasts.isEmpty()) {
            throw new PodcastNotFoundException("No podcasts found");
        }
        return podcasts;
    }

    public Podcast getPodcastById(Long podcastId) {
        return podcastRepository.findById(podcastId).orElseThrow( () ->
                new PodcastNotFoundException("Podcast with ID " + podcastId + " not found"));
    }


    public void deleteById(Long podcastId) {
        podcastRepository.deleteById(podcastId);
    }

}
