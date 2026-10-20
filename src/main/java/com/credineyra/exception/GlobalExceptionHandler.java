package com.credineyra.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>>
    manejarRecursoNoEncontrado(
            RecursoNoEncontradoException ex
    ) {

        return construirRespuesta(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    @ExceptionHandler(DatoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>>
    manejarDatoDuplicado(
            DatoDuplicadoException ex
    ) {

        return construirRespuesta(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>>
    manejarReglaNegocio(
            ReglaNegocioException ex
    ) {

        return construirRespuesta(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<Map<String, Object>>
    manejarOperacionNoPermitida(
            OperacionNoPermitidaException ex
    ) {

        return construirRespuesta(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
    manejarValidaciones(
            MethodArgumentNotValidException ex
    ) {

        Map<String, String> errores =
                new LinkedHashMap<>();

        for (FieldError error :
                ex.getBindingResult().getFieldErrors()) {

            errores.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        respuesta.put(
                "fecha",
                OffsetDateTime.now()
        );

        respuesta.put(
                "estado",
                HttpStatus.BAD_REQUEST.value()
        );

        respuesta.put(
                "error",
                "Error de validación"
        );

        respuesta.put(
                "mensaje",
                "Existen datos inválidos"
        );

        respuesta.put(
                "campos",
                errores
        );

        return ResponseEntity
                .badRequest()
                .body(respuesta);
    }

    /** Un ícono o archivo estático ausente no es un fallo interno del sistema. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> manejarRecursoEstaticoAusente(NoResourceFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
    manejarErrorGeneral(
            Exception ex
    ) {

        // El detalle se conserva únicamente en el log local del servidor. La
        // respuesta pública no revela SQL, rutas, credenciales ni datos internos.
        LOGGER.error("Error no controlado al procesar la solicitud", ex);

        return construirRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en el sistema"
        );
    }

    private ResponseEntity<Map<String, Object>>
    construirRespuesta(
            HttpStatus status,
            String mensaje
    ) {

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        respuesta.put(
                "fecha",
                OffsetDateTime.now()
        );

        respuesta.put(
                "estado",
                status.value()
        );

        respuesta.put(
                "error",
                status.getReasonPhrase()
        );

        respuesta.put(
                "mensaje",
                mensaje
        );

        return ResponseEntity
                .status(status)
                .body(respuesta);
    }
}
