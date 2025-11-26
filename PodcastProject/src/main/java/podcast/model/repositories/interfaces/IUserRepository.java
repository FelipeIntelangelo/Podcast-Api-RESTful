package podcast.model.repositories.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import podcast.model.entities.User;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository <User, Long> {

    Optional<User> findByCredentialUsername(String username);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByCredentialEmail(String email);

    boolean existsByCredentialEmail(String email);

    boolean existsByCredentialUsername(String username);

    boolean existsByCredentialResetToken(String resetToken);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.credential.roles WHERE u.id = :id")
    Optional<User> findByIdWithCredentialAndRoles(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM favorites WHERE user_id = :userId", nativeQuery = true)
    void deleteFavoritesByUserIdNative(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE user_id = :userId", nativeQuery = true)
    void deleteUserRolesByUserIdNative(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM users WHERE id = :userId", nativeQuery = true)
    void deleteUserByIdNative(@Param("userId") Long userId);

}
