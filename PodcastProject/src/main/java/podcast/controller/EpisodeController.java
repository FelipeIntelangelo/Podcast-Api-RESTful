package podcast.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import podcast.model.entities.Commentary;
import podcast.model.entities.Episode;
import podcast.model.entities.dto.CommentaryDTO;
import podcast.model.entities.dto.EpisodeDTO;
import podcast.model.exceptions.AlreadyCreatedException;
import podcast.model.exceptions.EpisodeNotFoundException;
import podcast.model.services.EpisodeHistoryService;
import podcast.model.services.EpisodeService;

import java.util.List;

@RestController
@RequestMapping(path = "podcastUTN/v1/episodes")
public class EpisodeController {

    //DEPENDENCIES
    private final EpisodeService episodeService;
    private final EpisodeHistoryService episodeHistoryService;

    @Autowired
    public EpisodeController(EpisodeService episodeService, EpisodeHistoryService episodeHistoryService) {
        this.episodeService = episodeService;
        this.episodeHistoryService = episodeHistoryService;
    }

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
    public ResponseEntity<List<EpisodeDTO>> getAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long podcastId
    ) {
        List<Episode> episodes = episodeService.getAllFiltered(title, podcastId);
        return ResponseEntity.ok(episodes.stream().map(Episode::toDTO).toList());
    }

    @GetMapping("/{episodeId}")
    public ResponseEntity<Episode> getById(@PathVariable("episodeId") Long episodeId) {
        Episode episodePivot = episodeService.getEpisodeById(episodeId);
        return ResponseEntity.ok(episodePivot);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{episodeId}/play")
    public ResponseEntity<String> playEpisode(@PathVariable("episodeId") Long episodeId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        episodeHistoryService.registerPlay(episodeId,userDetails.getUsername());
        String audioUrl = episodeService.getAudioUrl(episodeId);
        return ResponseEntity.ok(audioUrl);
    }

    @GetMapping("/{episodeId}/commentaries")
    public ResponseEntity<List<CommentaryDTO>> getComments(@PathVariable("episodeId") Long episodeId) {
        List<Commentary> comments = episodeService.getComments(episodeId);
        return ResponseEntity.ok(comments.stream().map(Commentary::toDTO).toList());
    }


    //POST - PUT MAPPINGS
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<String> save(@RequestBody @Valid Episode episode){
        episodeService.save(episode);
        return ResponseEntity.ok("Episode saved successfully");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{episodeId}/rate")
    public ResponseEntity<String> rateEpisode(@PathVariable("episodeId") Long episodeId,
                                              @RequestParam("rating") Long rating,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        episodeHistoryService.rateEpisode(episodeId, rating, userDetails.getUsername());
        return ResponseEntity.ok("Episode rated successfully");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{episodeId}/comment")
    public ResponseEntity<String> commentEpisode(@PathVariable("episodeId") Long episodeId,
                                                 @RequestParam("comment") String comment,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        episodeService.commentEpisode(episodeId, comment, userDetails.getUsername());
        return ResponseEntity.ok("Comment added successfully");
    }

    @PreAuthorize( "hasRole('ROLE_CREATOR') or hasRole('ROLE_ADMIN')")
    @PutMapping("/{episodeId}")
    public ResponseEntity<String> update(@PathVariable("episodeId") Long episodeId, @RequestBody @Valid Episode episode) {
        if (!episodeId.equals(episode.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Episode ID in path does not match ID in body");
        }
        episodeService.update(episode);
        return ResponseEntity.ok("Episode updated successfully");
    }

    //DELETE MAPPINGS
    @PreAuthorize("hasRole('ROLE_CREATOR') or hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{episodeId}")
    public ResponseEntity<String> deleteById(@PathVariable("episodeId") Long episodeId) {
        episodeService.deleteById(episodeId);
        return ResponseEntity.ok("Episode deleted successfully");
    }

}






















