 package podcast.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import podcast.model.entities.User;
import podcast.model.entities.enums.Role;
import podcast.model.exceptions.AlreadyCreatedException;
import podcast.model.exceptions.UserNotFoundException;
import podcast.model.repositories.interfaces.IUserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final IUserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(IUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void save(User user) {
        // Verifica que el id sea nulo pq es autoincremental en la bdd
        if (user.getId() != null) {
            throw new AlreadyCreatedException("No se debe enviar un ID al registrar un usuario nuevo");
        }
        // Asigna rol por defecto si no tiene
        user.getCredential().getRoles().clear();
        user.getCredential().getRoles().add(Role.ROLE_USER);
        // Encripta la contraseña
        String rawPassword = user.getCredential().getPassword();
        user.getCredential().setPassword(passwordEncoder.encode(rawPassword));

        // Asigna fecha de creación si es nuevo
        if (user.getCredential().getCreatedAt() == null) {
            user.getCredential().setCreatedAt(LocalDateTime.now());
        }

        userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + id));
    }
}