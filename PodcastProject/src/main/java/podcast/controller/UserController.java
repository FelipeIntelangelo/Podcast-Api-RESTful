package podcast.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import podcast.cfg.JwtUtil;
import podcast.model.entities.User;
import podcast.model.entities.dto.*;
import podcast.model.exceptions.AlreadyCreatedException;
import podcast.model.exceptions.PodcastNotFoundException;
import podcast.model.exceptions.UnauthorizedException;
import podcast.model.services.EpisodeHistoryService;
import podcast.model.services.UserDetailsServiceImpl;
import podcast.model.services.UserService;
import podcast.model.exceptions.UserNotFoundException;

import java.util.List;
import java.util.Map;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/podcastUTN/v1/users")
public class UserController {

    // ── Inyeccion De Dependencias Necesarias ─────────────────────────────────────────

    private final EpisodeHistoryService episodeHistoryService;
    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // ── Constructor ──────────────────────────────────────────────────────────────────

    @Autowired
    public UserController(
            EpisodeHistoryService episodeHistoryService,
            UserService userService,
            UserDetailsServiceImpl userDetailsService,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil
    ) {
        this.episodeHistoryService = episodeHistoryService;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // ── Handlers ─────────────────────────────────────────────────────────────────────

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
    @ExceptionHandler(AlreadyCreatedException.class)
    public ResponseEntity<String> handleAlreadyCreated(AlreadyCreatedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
    @ExceptionHandler(PodcastNotFoundException.class)
    public ResponseEntity<String> handlePodcastNotFound(PodcastNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
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
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(500).body(Map.of("error", "Ocurrió un error inesperado: " + ex.getMessage()));
    }

    // ── Get ──────────────────────────────────────────────────────────────────────────

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myProfile")  // Obtiene el usuario autenticado
    public User getAuthenticatedUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getAuthenticatedUser(userDetails.getUsername());
    }

    @GetMapping // Obtiene todos los usuarios como DTO
    public ResponseEntity<List<UserDTO>> getAllUsers() { // Obtiene todos los usuarios como DTO
        return ResponseEntity.ok(userService.getAllUsersAsDTO());
    }

    @GetMapping("/{userId}")  // Obtiene un usuario por id como DTO
    public ResponseEntity<UserDTO> getUserById(@PathVariable ("userId") Long userId) {
        return ResponseEntity.ok(userService.getUserByIdAsDTO(userId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/credential/{userId}")  // Obtiene un usuario por id con credenciales
    public ResponseEntity<User> getUserWithCredentialsById(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(userService.getUserWithCredentialsById(userId));
    }

    @PreAuthorize("IsAuthenticated()")
    @GetMapping("/myHistory")  // Obtiene el historial de reproduccion del usuario autenticado
    public ResponseEntity<List<EpisodeHistoryDTO>> getMyHistory(@AuthenticationPrincipal UserDetails userDetails) {
        List<EpisodeHistoryDTO> history = episodeHistoryService.getHistoryByUsername(userDetails.getUsername());
        return ResponseEntity.ok(history);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myFavorites")  // Obtiene los podcasts favoritos del usuario autenticado
    public ResponseEntity<List<PodcastDTO>> getMyFavorites(@AuthenticationPrincipal UserDetails userDetails) {
        List<PodcastDTO> favorites = userService.getFavoritesByUsername(userDetails.getUsername());
        return ResponseEntity.ok(favorites);
    }

    // ── Post ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody @Valid User user) {
        userService.save(user);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/favorites/{podcastId}")
    public ResponseEntity<String> addPodcastToFavorites(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long podcastId
    ) {
        userService.addPodcastToFavorites(userDetails.getUsername(), podcastId);
        return ResponseEntity.ok("Podcast agregado a favoritos correctamente");
    }

    // ── Patch ────────────────────────────────────────────────────────────────────────

    @PreAuthorize("IsAuthenticated()")
    @PatchMapping("/myProfile")  // Actualiza el perfil del usuario autenticado
    public ResponseEntity<UserDTO> updateProfile(
    @AuthenticationPrincipal UserDetails userDetails,
    @RequestBody @Valid UpdateUserDTO updates) {
        User updatedUser = userService.updateAuthenticatedUser(userDetails.getUsername(), updates);
        return ResponseEntity.ok(updatedUser.toDTO());
    }

    // ── Delete ───────────────────────────────────────────────────────────────────────

    @DeleteMapping("/myProfile")  // Elimina el perfil del usuario autenticado
    public ResponseEntity<String> deleteAuthenticatedUser(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteAuthenticatedUser(userDetails.getUsername());
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/favorites/{podcastId}")
    public ResponseEntity<String> removePodcastFromFavorites(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable Long podcastId
    ) {
        userService.removePodcastFromFavorites(userDetails.getUsername(), podcastId);
        return ResponseEntity.ok("Podcast eliminado de favoritos correctamente");
    }
}