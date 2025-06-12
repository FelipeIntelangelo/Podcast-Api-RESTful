package podcast.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import podcast.model.entities.Episode;
import podcast.model.entities.EpisodeHistory;
import podcast.model.entities.User;
import podcast.model.entities.dto.EpisodeDTO;
import podcast.model.entities.dto.EpisodeHistoryDTO;
import podcast.model.exceptions.EpisodeNotFoundException;
import podcast.model.exceptions.UserNotFoundException;
import podcast.model.repositories.interfaces.IEpisodeHistoryRepository;
import podcast.model.repositories.interfaces.IEpisodeRepository;
import podcast.model.repositories.interfaces.IUserRepository;

import java.time.LocalDateTime;
import java.util.List;

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

    public List<EpisodeHistoryDTO> getHistoryByUsername(String username) {
        User user = userRepository.findByCredentialUsernameFetch(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        List<EpisodeHistory> history = episodeHistoryRepository.findEpisodesByUserId(user.getId());
        if (history.isEmpty()) {
            throw new EpisodeNotFoundException("No episode history found for user: " + username);
        }
        return history.stream().map(EpisodeHistory::toDTO).toList();
    }

    public void rateEpisode(Long episodeId, Long rating, String username) {
        Long userId = userRepository.findByCredentialUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username))
                .getId();

        EpisodeHistory episodeHistory = episodeHistoryRepository.findByEpisode_IdAndUser_Id(episodeId, userId)
                .orElseThrow(() -> new EpisodeNotFoundException("Episode history not found for ID: " + episodeId + " and user ID: " + userId));

        episodeHistory.getRating().setRating(rating);
        episodeHistory.getRating().setRatedAt(LocalDateTime.now());
        episodeHistoryRepository.save(episodeHistory);

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new EpisodeNotFoundException("Episode not found for ID: " + episodeId));
        episode.setAverageRating(episodeRepository.findAverageRatingByEpisodeId(episodeId));
        episodeRepository.save(episode);
    }

    public void registerPlay(Long episodeId, String username) {
        Long userId = userRepository.findByCredentialUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username))
                .getId();

        EpisodeHistory episodeHistory = episodeHistoryRepository.findByEpisode_IdAndUser_Id(episodeId, userId)
                .orElseThrow(() -> new EpisodeNotFoundException("Episode history not found for ID: " + episodeId + " and user ID: " + userId));

        episodeHistory.setListenedAt(LocalDateTime.now());
        episodeHistoryRepository.save(episodeHistory);

        Episode episode = episodeRepository.findById(episodeId) //aumento la view del episodio
                .orElseThrow(() -> new EpisodeNotFoundException("Episode not found for ID: " + episodeId));
        episode.setViews(episode.getViews() + 1);
        episodeRepository.save(episode);
    }



}
