package podcast.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Commentaries")
public class Commentary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "episode_id", nullable = false)
    private Episode episode;
}