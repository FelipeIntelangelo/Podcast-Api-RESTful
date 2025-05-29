package podcast.model.entities;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder

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

@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Commentary that = (Commentary) o;
    return id != null && id.equals(that.id);
}

@Override
public int hashCode() {
    return id != null ? id.hashCode() : 0;
}
}