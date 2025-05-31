package podcast.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import podcast.model.entities.Podcast;
import podcast.model.exceptions.PodcastNotFoundException;
import podcast.model.services.PodcastService;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "podcastUTN/v1/podcasts")
public class PodcastController {

    @Autowired
    private PodcastService podcastService;

    //HANDLERS
    @ExceptionHandler(PodcastNotFoundException.class)
    public ResponseEntity<String> handlePodcastNotFound(PodcastNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    //END HANDLERS

    //START MAPPINGS
    @GetMapping
    public ResponseEntity<List<Podcast>> getAll() {
        List<Podcast> podcasts = podcastService.getAllPodcasts();
        return ResponseEntity.ok(podcasts);
    }

    @GetMapping("/{podcastId}")
    public Podcast getById(@PathVariable("podcastId") Long podcastId) {
        return podcastService.getPodcastById(podcastId).orElseThrow( () ->
            new PodcastNotFoundException("Podcast with ID " + podcastId + " not found"));
    }


    @PostMapping
    public ResponseEntity<String> saveOrReplace(@RequestBody Podcast podcast) {
        podcastService.saveOrReplace(podcast);
        return ResponseEntity.ok("Podcast saved successfully");
    }

    @DeleteMapping("/{podcastId}")
    public ResponseEntity<String> deleteById(@PathVariable("podcastId") Long podcastId) {
        podcastService.deleteById(podcastId);
        return ResponseEntity.ok("Podcast deleted successfully");
    }
    //END MAPPINGS

}
