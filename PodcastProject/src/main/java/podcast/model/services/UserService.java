// src/main/java/podcast/model/services/UserService.java
package podcast.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import podcast.model.entities.User;
import podcast.model.exceptions.AlreadyCreatedException;
import podcast.model.exceptions.UserNotFoundException;
import podcast.model.repositories.interfaces.IUserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final IUserRepository userRepository;

    @Autowired
    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveOrReplace(User user) {
        boolean existsNickname = userRepository.findAll().stream()
                .anyMatch(u -> u.getNickname().equalsIgnoreCase(user.getNickname()) && !u.getId().equals(user.getId()));
        boolean existsEmail = userRepository.findAll().stream()
                .anyMatch(u -> u.getCredential() != null && user.getCredential() != null &&
                        u.getCredential().getEmail().equalsIgnoreCase(user.getCredential().getEmail()) &&
                        !u.getId().equals(user.getId()));

        if (existsNickname) {
            throw new AlreadyCreatedException("Ya existe un usuario con el nickname: " + user.getNickname());
        }
        if (existsEmail) {
            throw new AlreadyCreatedException("Ya existe un usuario con el email: " + user.getCredential().getEmail());
        }

        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new UserNotFoundException("No hay usuarios registrados");
        }
        return users;
    }

    public Optional<User> getUserById(Integer userId) {
        return userRepository.findById(userId)
                .or(() -> { throw new UserNotFoundException("Usuario no encontrado con id: " + userId); });
    }

    public void deleteById(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Usuario no encontrado con id: " + userId);
        }
        userRepository.deleteById(userId);
    }
}