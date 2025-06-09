package podcast.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import podcast.model.entities.User;
import podcast.model.entities.dto.UserDTO;
import podcast.model.services.UserService;
import podcast.model.exceptions.UserNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/podcastUTN/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Filtrar usuarios por ID, nickname, profilePicture y bio (Invitado)
    @GetMapping("/filter")
    public ResponseEntity<List<UserDTO>> filterUsers(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String profilePicture,
            @RequestParam(required = false) String bio
    ) {
        List<UserDTO> filtered = userService.getAllUsers().stream()
                .filter(u -> id == null || u.getId().equals(id))
                .filter(u -> nickname == null || u.getNickname().equalsIgnoreCase(nickname))
                .filter(u -> profilePicture == null || (u.getProfilePicture() != null && u.getProfilePicture().equalsIgnoreCase(profilePicture)))
                .filter(u -> bio == null || (u.getBio() != null && u.getBio().toLowerCase().contains(bio.toLowerCase())))
                .map(User::toDTO)
                .toList();
        return ResponseEntity.ok(filtered);
    }

    // Obtener todos los usuarios (Invitado)
    @GetMapping("/{userId}")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(User::toDTO)
                .toList();
        return ResponseEntity.ok(users);
    }

    // Obtener un usuario por ID // Autorizacion rol:(<=Invitado)
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(user.toDTO()))
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + id));
    }

    // Crear un nuevo usuario (Invitado)
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserDTO userDTO) {
        userService.saveOrReplace(fromDTO(userDTO));
        return ResponseEntity.ok("Usuario creado correctamente");
    }

    // Actualizar un usuario existente (User Registrado)
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Integer id, @RequestBody UserDTO userDTO) {
        if (!id.equals(userDTO.getId())) {
            return ResponseEntity.badRequest().body("El id del path no coincide con el del usuario");
        }
        userService.saveOrReplace(fromDTO(userDTO));
        return ResponseEntity.ok("Usuario actualizado correctamente");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        userService.deleteById(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    // Método auxiliar para convertir UserDTO a User (ajusta según tus necesidades)
    private User fromDTO(UserDTO dto) {
        return User.builder()
                .id(dto.getId())
                .nickname(dto.getNickname())
                .profilePicture(dto.getProfilePicture())
                .bio(dto.getBio())
                .build();
    }
}