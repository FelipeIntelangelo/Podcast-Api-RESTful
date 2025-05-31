package podcast.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import podcast.model.entities.Podcast;
import podcast.model.exceptions.AlreadyCreatedException;
import podcast.model.exceptions.PodcastNotFoundException;
import podcast.model.services.PodcastService;

import java.util.List;


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
    @ExceptionHandler(AlreadyCreatedException.class)
    public ResponseEntity<String> handleAlreadyCreated(AlreadyCreatedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("Validation error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }
    //END HANDLERS

    // -------------------------------------------------------------------------------------------------------------------

    //START MAPPINGS

    @GetMapping
    public ResponseEntity<List<Podcast>> getAll() {
        List<Podcast> podcasts = podcastService.getAllPodcasts();
        return ResponseEntity.ok(podcasts);
    }

    @GetMapping("/{podcastId}")
    public ResponseEntity<Podcast> getById(@PathVariable("podcastId") Long podcastId) {
        Podcast podcastPivot = podcastService.getPodcastById(podcastId);
        return ResponseEntity.ok(podcastPivot);
    }


    @PostMapping
    public ResponseEntity<String> saveOrReplace(@RequestBody @Valid Podcast podcast) {
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
