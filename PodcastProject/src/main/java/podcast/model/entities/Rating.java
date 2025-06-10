
package podcast.model.entities;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class Rating {

    @Min(1)
    @Max(10)
    private Long rating; //  escala 1-10
    private LocalDateTime ratedAt; // Fecha de valoración

}
