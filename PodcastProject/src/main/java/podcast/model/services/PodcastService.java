package podcast.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import podcast.model.entities.Podcast;
import podcast.model.entities.enums.Category;
import podcast.model.exceptions.AlreadyCreatedException;
import podcast.model.exceptions.PodcastNotFoundException;
import podcast.model.repositories.interfaces.IPodcastRepository;

import java.util.List;

@Service
public class PodcastService {
    @Autowired
    IPodcastRepository podcastRepository;


    public void save(Podcast podcast) {
        podcastRepository.findAll().stream()
                .filter(podcastpvt -> podcastpvt.getTitle().equals(podcast.getTitle()))
                .findFirst()
                .ifPresent(podcastpvt -> {
                    throw new AlreadyCreatedException("Podcast with name " + podcast.getTitle() + " already exists");
                });
        podcastRepository.save(podcast);
    }

    public void update(Podcast podcast) {
        if (!podcastRepository.existsById(podcast.getId())) {
            throw new PodcastNotFoundException("Podcast with ID " + podcast.getId() + " not found");
        }
        podcastRepository.save(podcast);
    }

    public List<Podcast> getAllFiltered(String title, Integer userId, Category category) {
        List<Podcast> filtered;

        if (title == null && userId == null) {
            filtered = podcastRepository.findAll();
        } else {
            filtered = podcastRepository.findByUser_IdOrTitleIgnoreCaseOrCategories(userId, title, category);
            if (filtered.isEmpty()) {
                filtered = podcastRepository.findAll();
            }
        }
        if (filtered.isEmpty()) {
            throw new PodcastNotFoundException("No podcasts found");
        }
        return filtered;
    }

    public Podcast getPodcastById(Long podcastId) {
        return podcastRepository.findById(podcastId).orElseThrow( () ->
                new PodcastNotFoundException("Podcast with ID " + podcastId + " not found"));
    }


    public void deleteById(Long podcastId) {
        if (!podcastRepository.existsById(podcastId)) {
            throw new PodcastNotFoundException("Podcast with ID " + podcastId + " not found");
        }
        podcastRepository.deleteById(podcastId);
    }

    public void deleteByTitle(String title) {
        if(!podcastRepository.existsByTitle(title)) {
            throw new PodcastNotFoundException("Podcast with title " + title + " not found");
        }
        podcastRepository.deleteByTitle(title);
    }

}
