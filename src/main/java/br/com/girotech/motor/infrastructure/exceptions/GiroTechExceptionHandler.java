package br.com.girotech.motor.infrastructure.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GiroTechExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex, WebRequest request) {
        log.warn("[GIRO TECH] Recurso nao encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(construirCorpo(HttpStatus.NOT_FOUND, "Recurso Nao Encontrado", ex.getMessage(), request));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> tratarParametroAusente(
            MissingServletRequestParameterException ex, WebRequest request) {
        log.warn("[GIRO TECH] Parametro ausente: {}", ex.getParameterName());
        String msg = "O parametro '" + ex.getParameterName() + "' e obrigatorio.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(construirCorpo(HttpStatus.BAD_REQUEST, "Parametro Obrigatorio Ausente", msg, request));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tratarTipoInvalido(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String tipo = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido";
        String msg = String.format("O parametro '%s' recebeu o valor '%s', que e invalido. Tipo esperado: %s.",
                ex.getName(), ex.getValue(), tipo);
        log.warn("[GIRO TECH] Tipo de argumento invalido: {}", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(construirCorpo(HttpStatus.BAD_REQUEST, "Argumento Invalido", msg, request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> tratarErroInterno(Exception ex, WebRequest request) {
        log.error("[GIRO TECH] Erro interno: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(construirCorpo(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erro Interno", "Ocorreu um erro inesperado. Contate o suporte Giro Tech.", request));
    }

    private Map<String, Object> construirCorpo(HttpStatus status, String erro, String mensagem, WebRequest request) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now().toString());
        corpo.put("status", status.value());
        corpo.put("erro", erro);
        corpo.put("mensagem", mensagem);
        corpo.put("caminho", request.getDescription(false).replace("uri=", ""));
        corpo.put("sistema", "Motor de Recomendacao Giro Tech");
        return corpo;
    }
}