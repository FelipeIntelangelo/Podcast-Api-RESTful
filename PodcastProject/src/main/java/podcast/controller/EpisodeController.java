package podcast.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import podcast.model.entities.Episode;
import podcast.model.exceptions.AlreadyCreatedException;
import podcast.model.exceptions.EpisodeNotFoundException;
import podcast.model.services.EpisodeService;

import java.util.List;

@RestController
@RequestMapping(path = "podcastUTN/v1/episodes")
public class EpisodeController {

    //DEPENDENCIES
    @Autowired
    private EpisodeService episodeService;

    //HANDLERS
    @ExceptionHandler(EpisodeNotFoundException.class)
    public ResponseEntity<String> handleEpisodeNotFound(EpisodeNotFoundException ex) {
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
    public ResponseEntity<List<Episode>> getAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long podcastId
    ) {
        List<Episode> episodes = episodeService.getAllFiltered(title, podcastId);
        return ResponseEntity.ok(episodes);
    }

    @GetMapping("/{episodeId}")
    public ResponseEntity<Episode> getById(@PathVariable("episodeId") Long episodeId) {
        Episode episodePivot = episodeService.getEpisodeById(episodeId);
        return ResponseEntity.ok(episodePivot);
    }

    //POST - PUT MAPPINGS
    @PostMapping
    public ResponseEntity<String> save(@RequestBody @Valid Episode episode){
        episodeService.save(episode);
        return ResponseEntity.ok("Episode saved successfully");
    }

    @PutMapping("/{episodeId}")
    public ResponseEntity<String> update(@PathVariable("episodeId") Long episodeId, @RequestBody @Valid Episode episode) {
        if (!episodeId.equals(episode.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Episode ID in path does not match ID in body");
        }
        episodeService.update(episode);
        return ResponseEntity.ok("Episode updated successfully");
    }

    //DELETE MAPPINGS
    @DeleteMapping("/{episodeId}")
    public ResponseEntity<String> deleteById(@PathVariable("episodeId") Long episodeId) {
        episodeService.deleteById(episodeId);
        return ResponseEntity.ok("Episode deleted successfully");
    }

    @DeleteMapping("/{title}")
    public ResponseEntity<String> deleteByTitle(@PathVariable("title") String title) {
        episodeService.deleteByTitle(title);
        return ResponseEntity.ok("Episode with title '" + title + "' deleted successfully");
    }

    //END MAPPINGS
}






















