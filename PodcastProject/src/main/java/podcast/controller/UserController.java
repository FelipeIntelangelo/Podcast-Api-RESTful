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
import podcast.model.entities.dto.UserDTO;
import podcast.model.services.UserDetailsServiceImpl;
import podcast.model.services.UserService;
import podcast.model.exceptions.UserNotFoundException;

import java.util.List;

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

    // ── Registrarse ──────────────────────────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody @Valid User user) {
        userService.save(user);
        return ResponseEntity.ok("Usuario registrado correctamente");
    }

    // ── Mi Perfil ────────────────────────────────────────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService
            .getAllUsers()
            .stream()
            .filter(u -> u.getCredential().getUsername().equals(userDetails.getUsername()))
            .findFirst()
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        return ResponseEntity.ok(user.toDTO());
    }

    // ── Actualizar Perfil ────────────────────────────────────────────────────────────

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(@AuthenticationPrincipal UserDetails userDetails,
                                                     @RequestBody UserDTO userDTO) {
        User user = userService
            .getAllUsers()
            .stream()
            .filter(u -> u.getCredential().getUsername().equals(userDetails.getUsername()))
            .findFirst()
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // Actualiza los campos permitidos
        user.setNickname(userDTO.getNickname());
        user.setProfilePicture(userDTO.getProfilePicture());
        user.setBio(userDTO.getBio());
        //userService.replace(user);

        return ResponseEntity.ok(user.toDTO());
    }

    // ── Obtener Todos Los Usuarios (sin Credenciales) ────────────────────────────────
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers()
                .stream()
                .map(User::toDTO)
                .toList();
        return ResponseEntity.ok(users);
    }

    // ── Obtener Todos Los Usuarios (con Credenciales) ────────────────────────────────
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsersWithCredentials() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

     // ── Obtener Usuario Por Id ──────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user.toDTO());
    }

    // ── Obtener Usuario Con Credenciales Por Id (solo Admin) ─────────────────────────
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all/{id}")
    public ResponseEntity<User> getUserWithCredentialsById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

     // ── Metodo Auxiliar Para Convertir Userdto A User ───────────────────────────────
    private User fromDTO(UserDTO dto) {
        return User.builder()
                .id(dto.getId())
                .nickname(dto.getNickname())
                .profilePicture(dto.getProfilePicture())
                .bio(dto.getBio())
                .build();
    }
}