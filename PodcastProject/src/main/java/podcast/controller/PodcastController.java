package podcast.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import podcast.model.entities.Podcast;
import podcast.model.entities.dto.PodcastDTO;
import podcast.model.entities.enums.Category;
import podcast.model.exceptions.AlreadyCreatedException;
import podcast.model.exceptions.PodcastNotFoundException;
import podcast.model.services.PodcastService;

import java.util.List;


@RestController
@RequestMapping(path = "podcastUTN/v1/podcasts")
public class PodcastController {

    //DEPENDENCIES
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

    // ---------------------------------------------------------------------------------------

    //START MAPPINGS

    //GET MAPPINGS
    @GetMapping
    public ResponseEntity<List<PodcastDTO>> getAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean orderByViews
    ) {
        List<PodcastDTO> podcasts = podcastService.getAllFiltered(title, userId, Category.valueOf(category), orderByViews);
        return ResponseEntity.ok(podcasts);
    }

    @GetMapping("/{podcastId}")
    public ResponseEntity<Podcast> getById(@PathVariable("podcastId") Long podcastId) {
        Podcast podcastPivot = podcastService.getPodcastById(podcastId);
        return ResponseEntity.ok(podcastPivot);
    }
    //Este get lo hago de esta manera, ya que el de arriba pienso entregar el DTO y en este el podcast con todos sus atributos

    //POST - PUT MAPPINGS
    @PostMapping
    public ResponseEntity<String> save(@RequestBody @Valid Podcast podcast) {
        podcastService.save(podcast);
        return ResponseEntity.ok("Podcast saved successfully");
    }

    @PutMapping("/{podcastId}")
    public ResponseEntity<String> update(@PathVariable("podcastId") Long podcastId, @RequestBody @Valid Podcast podcast) {
        if (!podcastId.equals(podcast.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Podcast ID in path does not match ID in body");
        }
        podcastService.update(podcast);
        return ResponseEntity.ok("Podcast updated successfully");
    }

    //DELETE MAPPINGS
    @DeleteMapping("/{podcastId}")
    public ResponseEntity<String> deleteById(@PathVariable("podcastId") Long podcastId) {
        podcastService.deleteById(podcastId);
        return ResponseEntity.ok("Podcast deleted successfully");
    }

    @DeleteMapping("/{title}")
    public ResponseEntity<String> deleteByTitle(@PathVariable("title") String title) {
        podcastService.deleteByTitle(title);
        return ResponseEntity.ok("Podcast with title '" + title + "' deleted successfully");
    }

    //END MAPPINGS

}
