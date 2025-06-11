package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import podcast.model.entities.User;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository <User, Long> {
    @Query("SELECT u FROM User u JOIN FETCH u.credential WHERE u.credential.username = :username")
    Optional<User> findByCredentialUsernameFetch(String username);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByCredentialEmail(String email);

    boolean existsByCredentialEmail(String email);

    boolean existsByCredentialUsername(String username);

    boolean existsByCredentialResetToken(String resetToken);

}
