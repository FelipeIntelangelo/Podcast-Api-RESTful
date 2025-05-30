package podcast.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import podcast.model.entities.Podcast;
import podcast.model.services.PodcastService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "podcastUTN/v1/podcasts")
public class PodcastController {

    @Autowired
    private PodcastService podcastService;

    @GetMapping
    public List<Podcast> getAll() {
        return podcastService.getAllPodcasts();
    }

    @GetMapping("/{podcastId}")
    public Optional<Podcast> getById(@PathVariable("podcastId") Long podcastId) {
        return podcastService.getPodcastById(podcastId);
    }

    @PostMapping
    public void saveOrReplace(@RequestBody Podcast podcast) {
        podcastService.saveOrReplace(podcast);
    }

    @DeleteMapping("/{podcastId}")
    public void deleteById(@PathVariable("podcastId") Long podcastId) {
        podcastService.deleteById(podcastId);
    }

}
