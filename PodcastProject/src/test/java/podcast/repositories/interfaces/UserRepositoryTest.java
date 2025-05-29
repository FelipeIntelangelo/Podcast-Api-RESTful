package podcast.repositories.interfaces;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import podcast.model.repositories.interfaces.UserRepository;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveUser() {
    }

    @Test
    void findByEmail() {
    }

    @Test
    void findByUsername() {
    }

    @Test
    void findByUsernameOrEmail() {
    }

    @Test
    void existsByUsername() {
    }

    @Test
    void existsByEmail() {
    }

    @Test
    void existsById() {
    }
}