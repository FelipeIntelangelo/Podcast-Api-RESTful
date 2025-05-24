package podcast.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    private String profilePicture;
    private String bio;

    @Embedded
    private Credential credential;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user")
    private List<Podcast> podcasts;

    @ManyToMany
    @JoinTable(
        name = "Favorites",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "podcast_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "podcast_id"})
    )
    private List<Podcast> favorites;

    // getters y setters
}