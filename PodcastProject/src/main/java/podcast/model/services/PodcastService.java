package podcast.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import podcast.model.entities.Podcast;
import podcast.model.repositories.interfaces.IPodcastRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PodcastService {
    @Autowired
    IPodcastRepository podcastRepository;

    public void saveOrReplace(Podcast podcast){
        podcastRepository.save(podcast);
    }

    public List<Podcast> getAllPodcasts() {
        return podcastRepository.findAll();
    }

    public Optional<Podcast> getPodcastById(Long podcastId) {
        return podcastRepository.findById(podcastId);
    }


    public void deleteById(Long podcastId) {
        podcastRepository.deleteById(podcastId);
    }

}
