package podcast.model.services;

import podcast.model.entities.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import podcast.model.entities.Podcast;
import podcast.model.entities.User;
import podcast.model.entities.dto.PodcastDTO;
import podcast.model.entities.enums.Category;
import podcast.model.exceptions.AlreadyCreatedException;
import podcast.model.exceptions.PodcastNotFoundException;
import podcast.model.repositories.interfaces.IPodcastRepository;
import podcast.model.repositories.interfaces.IUserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PodcastService {

    private final IPodcastRepository podcastRepository;
    private final IUserRepository userRepository;

    @Autowired
    public PodcastService(IPodcastRepository podcastRepository, IUserRepository userRepository) {
        this.podcastRepository = podcastRepository;
        this.userRepository = userRepository;
    }

    public void save(Podcast podcast) {
        podcastRepository.findAll().stream()
                .filter(podcastpvt -> podcastpvt.getTitle().equals(podcast.getTitle()))
                .findFirst()
                .ifPresent(podcastpvt -> {
                    throw new AlreadyCreatedException("Podcast with name " + podcast.getTitle() + " already exists");
                });

        User user = userRepository.findById( podcast.getUser().getId())
                .orElseThrow(() -> new PodcastNotFoundException("User with ID " + podcast.getUser().getId() + " not found"));
        user.getCredential().getRoles().add(Role.ROLE_CREATOR);
        userRepository.save(user);
        podcast.setCreatedAt(LocalDateTime.now());
        podcastRepository.save(podcast);
    }

    public void update(Podcast podcast) {
        if (!podcastRepository.existsById(podcast.getId())) {
            throw new PodcastNotFoundException("Podcast with ID " + podcast.getId() + " not found");
        }
        podcast.setUpdatedAt(LocalDateTime.now());
        podcastRepository.save(podcast);
    }

    public List<PodcastDTO> getAllFiltered(String title, Integer userId, Category category, Boolean orderByViews) {
        List<Podcast> filtered;

        if (title == null && userId == null && category == null) {
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
        List<PodcastDTO> filteredDTO = filtered.stream()
                .map(Podcast::toDTO)
                .toList();
        if (orderByViews){
            filteredDTO.sort((p1, p2) -> Long.compare(p2.getAverageViews(), p1.getAverageViews()));
        }
        return filteredDTO;
    }

    public Podcast getPodcastById(Long podcastId) {
        return podcastRepository.findById(podcastId).orElseThrow( () ->
                new PodcastNotFoundException("Podcast with ID " + podcastId + " not found"));
    }

    public List<Podcast> getByUsername(String username) {
        List<Podcast> podcasts = podcastRepository.findByUser_Credential_Username(username);
        if (podcasts.isEmpty()) {
            throw new PodcastNotFoundException("No podcasts found for user " + username);
        }
        return podcasts;
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
