 package podcast.model.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import podcast.model.entities.*;
import podcast.model.entities.dto.PodcastDTO;
import podcast.model.entities.dto.UpdateUserDTO;
import podcast.model.entities.dto.UserDTO;
import podcast.model.entities.enums.Role;
import podcast.model.exceptions.AlreadyCreatedException;
import podcast.model.exceptions.PodcastNotFoundException;
import podcast.model.exceptions.UserNotFoundException;
import podcast.model.repositories.interfaces.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    // ── Inyeccion De Dependencias Necesarias ─────────────────────────────────────────

    private final IUserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final IPodcastRepository podcastRepository;
    private final IEpisodeHistoryRepository episodeHistoryRepository;
    private final IRatingRepository ratingRepository;
    private final ICommentaryRepository commentaryRepository;
    private final IEpisodeRepository episodeRepository;
    private EntityManager entityManager;


    // ── Constructor ──────────────────────────────────────────────────────────────────

    @Autowired
    public UserService(IUserRepository userRepository, PasswordEncoder passwordEncoder, IPodcastRepository podcastRepository,
                       IEpisodeHistoryRepository episodeHistoryRepository, EntityManager entityManager, IRatingRepository ratingRepository,
                       ICommentaryRepository commentaryRepository, IEpisodeRepository episodeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.podcastRepository = podcastRepository;
        this.episodeHistoryRepository = episodeHistoryRepository;
        this.entityManager = entityManager;
        this.ratingRepository = ratingRepository;
        this.commentaryRepository = commentaryRepository;
        this.episodeRepository = episodeRepository;
    }

    // ── Logica De Negocio ────────────────────────────────────────────────────────────


    // ── Get ──────────────────────────────────────────────────────────────────────────

    public User getAuthenticatedUser(String username) {
        return userRepository.findByCredentialUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con username: " + username));
    }

    public List<UserDTO> getAllUsersAsDTO() {
        return userRepository.findAll()
                .stream()
                .map(User::toDTO)
                .toList();
    }

    public UserDTO getUserByIdAsDTO(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + id));
        return user.toDTO();
    }

    public User getUserWithCredentialsById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + id));
    }

    public List<PodcastDTO> getFavoritesByUsername(String username) {
        User user = userRepository.findByCredentialUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con username: " + username));
        return user.getFavorites().stream().map(Podcast::toDTO).toList();
    }

    // ── Post ─────────────────────────────────────────────────────────────────────────

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

    public boolean existsByUsername(String username) {
        return userRepository.existsByCredentialUsername(username);
    }

    public void addPodcastToFavorites(String username, Long podcastId) {
        User user = userRepository.findByCredentialUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con username: " + username));
        Podcast podcast = podcastRepository.findById(podcastId)
                .orElseThrow(() -> new PodcastNotFoundException("Podcast no encontrado con id: " + podcastId));

        if (user.getFavorites().contains(podcast)) {
            throw new IllegalArgumentException("El podcast ya está en la lista de favoritos");
        }

        user.getFavorites().add(podcast);
        userRepository.save(user);
    }

    // ── Patch ────────────────────────────────────────────────────────────────────────

    public User updateAuthenticatedUser(String username, UpdateUserDTO updates) {
        User existingUser = userRepository.findByCredentialUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con username: " + username));

        // Actualiza solo los campos proporcionados y no vacíos
        if (updates.getNickname() != null && !updates.getNickname().isBlank()) {
            existingUser.setNickname(updates.getNickname());
        }
        if (updates.getProfilePicture() != null && !updates.getProfilePicture().isBlank()) {
            existingUser.setProfilePicture(updates.getProfilePicture());
        }
        if (updates.getBio() != null && !updates.getBio().isBlank()) {
            existingUser.setBio(updates.getBio());
        }
        if (updates.getEmail() != null && !updates.getEmail().isBlank()) {
            existingUser.getCredential().setEmail(updates.getEmail());
        }
        if (updates.getPassword() != null && !updates.getPassword().isBlank()) {
            existingUser.getCredential().setPassword(passwordEncoder.encode(updates.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    // ── Delete ───────────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteAuthenticatedUser(String username) {
        System.out.println(">> Entró a UserService.deleteAuthenticatedUser con username: " + username);

        try {
            User user = userRepository.findByCredentialUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con username: " + username));

            System.out.println(">> User cargado: " + user.getId());

            // Verificar podcasts activos (para el mensaje de error)
            List<Podcast> filtered = podcastRepository.findByUser_Id(user.getId())
                    .stream()
                    .filter(podcast -> Boolean.TRUE.equals(podcast.getIsActive()))
                    .toList();

            if (!filtered.isEmpty()) {
                throw new IllegalArgumentException("No se puede eliminar el usuario porque es dueño de uno o más podcasts.");
            }

            System.out.println(">> Verificación de podcasts activos OK");

            // Obtener todos los podcasts del usuario (activos e inactivos)
            List<Podcast> allPodcasts = podcastRepository.findByUser_Id(user.getId());
            List<Long> podcastIds = allPodcasts.stream().map(Podcast::getId).toList();

            if (!podcastIds.isEmpty()) {
                // ⬇️ BORRAR EN ORDEN: primero las relaciones, luego las entidades principales

                // 1. Borrar categorías de los podcasts (tabla categoriesxpodcast)
                System.out.println(">> Borrando categorías de los podcasts...");
                podcastRepository.deleteCategoriesByPodcastIdsNative(podcastIds);
                entityManager.flush();
                System.out.println(">> Categorías borradas OK");

                // 2. Borrar TODOS los favoritos que referencian estos podcasts (de todos los usuarios)
                System.out.println(">> Borrando favoritos de los podcasts...");
                podcastRepository.deleteFavoritesByPodcastIdsNative(podcastIds);
                entityManager.flush();
                System.out.println(">> Favoritos de podcasts borrados OK");

                // 3. Borrar comentarios y ratings de los episodios
                System.out.println(">> Borrando comentarios y ratings de los episodios...");
                for (Podcast podcast : allPodcasts) {
                    // Cargar episodios si no están cargados
                    podcast.getEpisodes().size(); // fuerza carga lazy
                    for (Episode episode : podcast.getEpisodes()) {
                        commentaryRepository.deleteByEpisodeIdNative(episode.getId());
                        ratingRepository.deleteByEpisodeIdNative(episode.getId());
                    }
                }
                entityManager.flush();
                System.out.println(">> Comentarios y ratings de episodios borrados OK");

                // 4. Borrar episodios
                System.out.println(">> Borrando episodios...");
                for (Long podcastId : podcastIds) {
                    episodeRepository.deleteByPodcastIdNative(podcastId);
                }
                entityManager.flush();
                System.out.println(">> Episodios borrados OK");

                // 5. Borrar todos los podcasts del usuario (activos e inactivos)
                System.out.println(">> Borrando todos los podcasts del usuario...");
                podcastRepository.deleteByUserIdNative(user.getId());
                entityManager.flush();
                System.out.println(">> Podcasts borrados OK");
            }

            // ⬇️ BORRAR OTRAS ENTIDADES RELACIONADAS

            // 1. Borrar historial de episodios
            System.out.println(">> Borrando historial de episodios...");
            episodeHistoryRepository.deleteByUserIdNative(user.getId());
            entityManager.flush();
            System.out.println(">> Historial borrado OK");

            // 2. Borrar comentarios (por si acaso quedó alguno)
            System.out.println(">> Borrando comentarios...");
            commentaryRepository.deleteByUserIdNative(user.getId());
            entityManager.flush();
            System.out.println(">> Comentarios borrados OK");

            // 3. Borrar ratings
            System.out.println(">> Borrando ratings...");
            ratingRepository.deleteByUserIdNative(user.getId());
            entityManager.flush();
            System.out.println(">> Ratings borrados OK");

            // 4. Limpiar favoritos del usuario (relación ManyToMany)
            System.out.println(">> Limpiando favoritos del usuario...");
            userRepository.deleteFavoritesByUserIdNative(user.getId());
            entityManager.flush();
            System.out.println(">> Favoritos del usuario limpiados OK");

            // 5. Borrar roles del usuario
            System.out.println(">> Borrando roles del usuario...");
            userRepository.deleteUserRolesByUserIdNative(user.getId());
            entityManager.flush();
            System.out.println(">> Roles borrados OK");

            // 6. Borrar el usuario directamente con SQL
            System.out.println(">> Borrando usuario con SQL nativo...");
            userRepository.deleteUserByIdNative(user.getId());
            entityManager.flush();
            System.out.println(">> Usuario borrado OK - TODO OK");

        } catch (Exception e) {
            System.out.println(">> ERROR en deleteAuthenticatedUser:");
            System.out.println(">> Tipo: " + e.getClass().getName());
            System.out.println(">> Mensaje: " + e.getMessage());
            System.out.println(">> Stacktrace completo:");
            e.printStackTrace();
            throw e;
        }
    }

    public void removePodcastFromFavorites(String username, Long podcastId) {
        User user = userRepository.findByCredentialUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con username: " + username));
        Podcast podcast = podcastRepository.findById(podcastId)
                .orElseThrow(() -> new PodcastNotFoundException("Podcast no encontrado con id: " + podcastId));

        if (!user.getFavorites().contains(podcast)) {
            throw new IllegalArgumentException("El podcast no está en la lista de favoritos");
        }

        user.getFavorites().remove(podcast);
        userRepository.save(user);
    }

    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + userId));

        boolean isOwnerOfPodcasts = podcastRepository.existsByUserId(user.getId());
        if (isOwnerOfPodcasts) {
            throw new IllegalArgumentException("No se puede eliminar el usuario porque es dueño de uno o más podcasts.");
        }

        userRepository.delete(user);
    }
}