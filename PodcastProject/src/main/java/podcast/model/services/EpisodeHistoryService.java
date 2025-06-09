package podcast.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import podcast.model.entities.Episode;
import podcast.model.entities.EpisodeHistory;
import podcast.model.exceptions.EpisodeNotFoundException;
import podcast.model.exceptions.UserNotFoundException;
import podcast.model.repositories.interfaces.IEpisodeHistoryRepository;
import podcast.model.repositories.interfaces.IEpisodeRepository;
import podcast.model.repositories.interfaces.IUserRepository;

import java.time.LocalDateTime;

@Service
public class EpisodeHistoryService {


    private final IEpisodeHistoryRepository episodeHistoryRepository;
    private final IEpisodeRepository episodeRepository;
    private final IUserRepository userRepository;

    @Autowired
    public EpisodeHistoryService(IEpisodeHistoryRepository episodeHistoryRepository, IEpisodeRepository episodeRepository, IUserRepository userRepository) {
        this.episodeHistoryRepository = episodeHistoryRepository;
        this.episodeRepository = episodeRepository;
        this.userRepository = userRepository;
    }

    public void saveEpisodeHistory(EpisodeHistory episodeHistory) {
        episodeHistory.setListenedAt(LocalDateTime.now());
        episodeHistoryRepository.save(episodeHistory);
    }

    public void rateEpisode(Long episodeId, Long rating, String username) {
        Long userId = Long.valueOf(userRepository.findByCredentialUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username))
                .getId());

        EpisodeHistory episodeHistory = episodeHistoryRepository.findByEpisode_IdAndUser_Id(episodeId, userId)
                .orElseThrow(() -> new EpisodeNotFoundException("Episode history not found for ID: " + episodeId));

        episodeHistory.getRating().setRating(rating);
        episodeHistory.getRating().setRatedAt(LocalDateTime.now());
        episodeHistoryRepository.save(episodeHistory);

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new EpisodeNotFoundException("Episode not found for ID: " + episodeId));
        episode.setAverageRating(episodeRepository.findAverageRatingByEpisodeId(episodeId));
        episodeRepository.save(episode);
    }


}
