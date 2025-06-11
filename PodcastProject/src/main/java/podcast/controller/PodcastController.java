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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import podcast.model.entities.Podcast;
import podcast.model.entities.dto.PodcastDTO;
import podcast.model.entities.enums.Category;
import podcast.model.exceptions.AlreadyCreatedException;
import podcast.model.exceptions.PodcastNotFoundException;
import podcast.model.exceptions.UnauthorizedException;
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

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String errorMessage = "Invalid value for parameter '" + ex.getName() + "': " + ex.getValue();
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
        Category categoryEnum = (category != null) ? Category.valueOf(category) : null;
        List<PodcastDTO> podcasts = podcastService.getAllFiltered(title, userId, categoryEnum, orderByViews);
        return ResponseEntity.ok(podcasts);
    }


    @GetMapping("/{podcastId}")
    public ResponseEntity<Podcast> getById(@PathVariable("podcastId") Long podcastId) {
        Podcast podcastPivot = podcastService.getPodcastById(podcastId);
        return ResponseEntity.ok(podcastPivot);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CREATOR')")
    @GetMapping("/myPodcasts")
    public ResponseEntity<List<PodcastDTO>> getMyPodcasts(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        List<Podcast> podcasts = podcastService.getByUsername(username);
        return ResponseEntity.ok(podcasts.stream().map(Podcast::toDTO).toList());
    }

    //Este get lo hago de esta manera, ya que el de arriba pienso entregar el DTO y en este el podcast con todos sus atributos

    //POST - PUT MAPPINGS
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<String> save(@RequestBody @Valid Podcast podcast) {
        podcastService.save(podcast);
        return ResponseEntity.ok("Podcast saved successfully");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CREATOR')")
    @PutMapping("/{podcastId}")
    public ResponseEntity<String> update(@PathVariable("podcastId") Long podcastId,
                                         @RequestBody @Valid Podcast podcast,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        if (!podcastId.equals(podcast.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Podcast ID in path does not match ID in body");
        }
        if (!podcastService.getPodcastById(podcastId).getUser().getCredential().getUsername().equals(userDetails.getUsername())
        && !userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to update this podcast");
        } // Verifica que el usuario sea el creador del podcast o admin

        podcastService.update(podcast);
        return ResponseEntity.ok("Podcast updated successfully");
    }

    //DELETE MAPPINGS
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CREATOR')")
    @DeleteMapping("/{podcastId}")
    public ResponseEntity<String> deleteById(@PathVariable("podcastId") Long podcastId) {
        podcastService.deleteById(podcastId);
        return ResponseEntity.ok("Podcast deleted successfully");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CREATOR')")
    @DeleteMapping("/{title}")
    public ResponseEntity<String> deleteByTitle(@PathVariable("title") String title) {
        podcastService.deleteByTitle(title);
        return ResponseEntity.ok("Podcast with title '" + title + "' deleted successfully");
    }

    //END MAPPINGS

}
