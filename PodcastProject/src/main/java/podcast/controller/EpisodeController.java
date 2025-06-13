package podcast.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import podcast.model.entities.Commentary;
import podcast.model.entities.Episode;
import podcast.model.entities.dto.CommentaryDTO;
import podcast.model.entities.dto.EpisodeDTO;
import podcast.model.entities.dto.UpdateEpisodeDTO;
import podcast.model.exceptions.AlreadyCreatedException;
import podcast.model.exceptions.EpisodeNotFoundException;
import podcast.model.exceptions.PodcastNotFoundException;
import podcast.model.exceptions.UnauthorizedException;
import podcast.model.services.EpisodeHistoryService;
import podcast.model.services.EpisodeService;

import java.util.List;

@RestController
@RequestMapping(path = "podcastUTN/v1/episodes")
@Tag(name = "Episodios", description = "API para gestionar episodios de podcasts")
public class EpisodeController {

    private final EpisodeService episodeService;
    private final EpisodeHistoryService episodeHistoryService;

    @Autowired
    public EpisodeController(EpisodeService episodeService, EpisodeHistoryService episodeHistoryService) {
        this.episodeService = episodeService;
        this.episodeHistoryService = episodeHistoryService;
    }

    @ExceptionHandler(EpisodeNotFoundException.class)
    public ResponseEntity<String> handleEpisodeNotFound(EpisodeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(AlreadyCreatedException.class)
    public ResponseEntity<String> handleAlreadyCreated(AlreadyCreatedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("Validation error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ExceptionHandler(PodcastNotFoundException.class)
    public ResponseEntity<String> handlePodcastNotFound(PodcastNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String errorMessage = "Invalid value for parameter '" + ex.getName() + "': " + ex.getValue();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @Operation(
            summary = "Obtener todos los episodios",
            description = "Obtiene una lista de episodios en formato DTO con filtros opcionales. Los resultados se devuelven como EpisodeDTO para proteger datos sensibles y optimizar la respuesta."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de episodios encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EpisodeDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Parámetros de filtro inválidos")
    })
    @GetMapping
    public ResponseEntity<List<EpisodeDTO>> getAll(
            @Parameter(description = "Título del episodio para filtrar") @RequestParam(required = false) String title,
            @Parameter(description = "ID del podcast") @RequestParam(required = false) Long podcastId
    ) {
        List<Episode> episodes = episodeService.getAllFiltered(title, podcastId);
        return ResponseEntity.ok(episodes.stream().map(Episode::toDTO).toList());
    }

    @Operation(
            summary = "Obtener episodio por ID",
            description = "Recupera un episodio específico por su identificador único"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Episodio encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Episode.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Episodio no encontrado")
    })
    @GetMapping("/{episodeId}")
    public ResponseEntity<Episode> getById(
            @Parameter(description = "ID del episodio") @PathVariable("episodeId") Long episodeId) {
        Episode episodePivot = episodeService.getEpisodeById(episodeId);
        return ResponseEntity.ok(episodePivot);
    }

    @Operation(
            summary = "Reproducir episodio",
            description = "Reproduce un episodio y registra la reproducción en el historial del usuario"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "URL del audio del episodio",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "https://storage.com/audio/episode123.mp3")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Episodio no encontrado")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{episodeId}/play")
    public ResponseEntity<String> playEpisode(
            @Parameter(description = "ID del episodio") @PathVariable("episodeId") Long episodeId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        episodeHistoryService.registerPlay(episodeId, userDetails.getUsername());
        String audioUrl = episodeService.getAudioUrl(episodeId);
        return ResponseEntity.ok(audioUrl);
    }

    @Operation(
            summary = "Obtener comentarios de un episodio",
            description = "Recupera todos los comentarios asociados a un episodio específico"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de comentarios encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommentaryDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Episodio no encontrado")
    })
    @GetMapping("/{episodeId}/commentaries")
    public ResponseEntity<List<CommentaryDTO>> getComments(
            @Parameter(description = "ID del episodio") @PathVariable("episodeId") Long episodeId) {
        List<Commentary> comments = episodeService.getComments(episodeId);
        return ResponseEntity.ok(comments.stream().map(Commentary::toDTO).toList());
    }

    @Operation(
            summary = "Guardar nuevo episodio",
            description = "Crea un nuevo episodio en el sistema"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Episodio guardado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Episode saved successfully")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Datos del episodio inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "409", description = "Episodio ya existe")
    })
    @PreAuthorize("isAuthenticated")
    @PostMapping
    public ResponseEntity<String> save(
            @Parameter(
                    description = "Datos del episodio a crear",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Episode.class))
            )
            @RequestBody @Valid Episode episode) {
        episodeService.save(episode);
        return ResponseEntity.ok("Episode saved successfully");
    }

    @Operation(
            summary = "Calificar episodio",
            description = "Permite a un usuario autenticado calificar un episodio con un valor del 1 al 5"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Episodio calificado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Episode rated successfully")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Calificación inválida"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Episodio no encontrado")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{episodeId}/rate")
    public ResponseEntity<String> rateEpisode(
            @Parameter(description = "ID del episodio") @PathVariable("episodeId") Long episodeId,
            @Parameter(description = "Calificación (1-5)", schema = @Schema(type = "integer", minimum = "1", maximum = "5"))
            @RequestParam("rating") Long rating,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        episodeHistoryService.rateEpisode(episodeId, rating, userDetails.getUsername());
        return ResponseEntity.ok("Episode rated successfully");
    }

    @Operation(
            summary = "Comentar episodio",
            description = "Permite a un usuario autenticado agregar un comentario a un episodio"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comentario agregado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Comment added successfully")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Comentario inválido"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Episodio no encontrado")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{episodeId}/comment")
    public ResponseEntity<String> commentEpisode(
            @Parameter(description = "ID del episodio") @PathVariable("episodeId") Long episodeId,
            @Parameter(description = "Texto del comentario") @RequestParam("comment") String comment,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        episodeService.commentEpisode(episodeId, comment, userDetails.getUsername());
        return ResponseEntity.ok("Comment added successfully");
    }

    @Operation(
        summary = "Actualizar un episodio existente",
        description = "Actualiza los datos de un episodio. Solo el creador o un administrador pueden realizar esta operación.",
        parameters = {
            @Parameter(
                name = "episodeId",
                description = "ID del episodio a actualizar",
                required = true,
                example = "1"
            )
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos a actualizar del episodio",
            required = true,
            content = @Content(schema = @Schema(implementation = UpdateEpisodeDTO.class))
        )
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Episodio actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EpisodeDTO.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT faltante o inválido"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - No tiene permisos para actualizar este episodio"),
        @ApiResponse(responseCode = "404", description = "Episodio no encontrado")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CREATOR')")
    @PatchMapping("/{episodeId}")
    public ResponseEntity<EpisodeDTO> updateEpisode(
            @Parameter(hidden = true) @PathVariable Long episodeId,
            @RequestBody @Valid UpdateEpisodeDTO updates,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        EpisodeDTO updatedEpisode = episodeService.updateEpisode(episodeId, updates, userDetails);
        return ResponseEntity.ok(updatedEpisode);
    }

    @Operation(
            summary = "Eliminar episodio",
            description = "Elimina un episodio existente. Solo disponible para creadores y administradores"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Episodio eliminado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Episode deleted successfully")
                    )
            ),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Episodio no encontrado")
    })
    @PreAuthorize("hasRole('ROLE_CREATOR') or hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{episodeId}")
    public ResponseEntity<String> deleteById(
            @Parameter(description = "ID del episodio") @PathVariable("episodeId") Long episodeId) {
        episodeService.deleteById(episodeId);
        return ResponseEntity.ok("Episode deleted successfully");
    }
}