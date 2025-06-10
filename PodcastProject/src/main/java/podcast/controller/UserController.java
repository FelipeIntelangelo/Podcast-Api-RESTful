package podcast.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import podcast.cfg.JwtUtil;
import podcast.model.entities.User;
import podcast.model.entities.dto.LoginRequest;
import podcast.model.entities.dto.LoginResponse;
import podcast.model.entities.dto.UpdateUserDTO;
import podcast.model.entities.dto.UserDTO;
import podcast.model.services.UserDetailsServiceImpl;
import podcast.model.services.UserService;
import podcast.model.exceptions.UserNotFoundException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/podcastUTN/v1/users")
public class UserController {

    // ── Inyeccion De Dependencias Necesarias ─────────────────────────────────────────

    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


    // ── Constructor ──────────────────────────────────────────────────────────────────
    public UserController(
            UserService userService,
            UserDetailsServiceImpl userDetailsService,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil
    ) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // ── Get ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/me")  // Obtiene el usuario autenticado
    public User getAuthenticatedUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getAuthenticatedUser(userDetails.getUsername());
    }

    @GetMapping("/search/all") // Obtiene todos los usuarios como DTO
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsersAsDTO());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search/all/with-credentials") // Obtiene todos los usuarios con credenciales
    public ResponseEntity<List<User>> getAllUsersWithCredentials() {
        return ResponseEntity.ok(userService.getAllUsersWithCredentials());
    }

    @GetMapping("/search")  // Obtiene un usuario por id o nickname como DTO
    public ResponseEntity<UserDTO> getUserByIdOrNickname(
        @RequestParam(required = false) Long id,
        @RequestParam(required = false) String nickname
    ) {
        if (id != null) {
            return ResponseEntity.ok(userService.getUserByIdAsDTO(id));
        } else if (nickname != null) {
            return ResponseEntity.ok(userService.getUserByNicknameAsDTO(nickname));
        } else {
            throw new IllegalArgumentException("Debe proporcionar un id o un nickname para realizar la búsqueda");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search/with-credentials") // Obtiene un usuario por id o nickname con credenciales
    public ResponseEntity<User> getUserWithCredentialsByIdOrNickname(
        @RequestParam(required = false) Long id,
        @RequestParam(required = false) String nickname
    ) {
        if (id != null) {
            return ResponseEntity.ok(userService.getUserWithCredentialsById(id));
        } else if (nickname != null) {
            return ResponseEntity.ok(userService.getUserWithCredentialsByNickname(nickname));
        } else {
            throw new IllegalArgumentException("Debe proporcionar un id o un nickname para realizar la búsqueda");
        }
    }

    // ── Post ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody @Valid User user) {
        userService.save(user);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    // ── Patch ────────────────────────────────────────────────────────────────────────

    @PatchMapping("/me")
    public ResponseEntity<UserDTO> updateProfile(
    @AuthenticationPrincipal UserDetails userDetails,
    @RequestBody @Valid UpdateUserDTO updates) {
        User updatedUser = userService.updateAuthenticatedUser(userDetails.getUsername(), updates);
        return ResponseEntity.ok(updatedUser.toDTO());
    }

    // ── Delete ───────────────────────────────────────────────────────────────────────

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteAuthenticatedUser(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteAuthenticatedUser(userDetails.getUsername());
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }


}