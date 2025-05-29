
package podcast.model.entities;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
@JoinColumn(nullable = true) // Aclaro este valor, para mayor legibilidad, aunque no es necesario
public class Rating {

    private Double rating; // Valoración (1-5) escala 1-10
    private String comment; // Comentario opcional
    private LocalDateTime ratedAt; // Fecha de valoración

}
