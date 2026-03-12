package br.com.girotech.motor.infrastructure.exceptions;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GiroTechExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GiroTechExceptionHandler.class);
    private static final String SISTEMA = "Motor de Recomendação Giro Tech";

    @ExceptionHandler({RecursoNaoEncontradoException.class})
    public ResponseEntity<Map<String, Object>> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex, WebRequest request) {
        log.warn("[GIRO TECH ERROR] Recurso não encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(this.construirCorpo(HttpStatus.NOT_FOUND, "Recurso Não Encontrado", ex.getMessage(), request));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    public ResponseEntity<Map<String, Object>> tratarParametroAusente(MissingServletRequestParameterException ex, WebRequest request) {
        log.warn("[GIRO TECH ERROR] Parâmetro ausente: {}", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(this.construirCorpo(HttpStatus.BAD_REQUEST, "Parâmetro Obrigatório Ausente", "O parâmetro '" + ex.getParameterName() + "' é obrigatório.", request));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> tratarTipoInvalido(MethodArgumentTypeMismatchException ex, WebRequest request) {
        String mensagem = String.format("O parâmetro '%s' recebeu o valor '%s', que é inválido. Tipo esperado: %s.", ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido");
        log.warn("[GIRO TECH ERROR] Tipo de argumento inválido: {}", mensagem);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(this.construirCorpo(HttpStatus.BAD_REQUEST, "Argumento Inválido", mensagem, request));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Map<String, Object>> tratarErroInterno(Exception ex, WebRequest request) {
        log.error("[GIRO TECH ERROR] Erro interno inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(this.construirCorpo(HttpStatus.INTERNAL_SERVER_ERROR, "Erro Interno do Motor Giro Tech", "Ocorreu um erro inesperado. Contate o suporte Giro Tech.", request));
    }

    private Map<String, Object> construirCorpo(HttpStatus status, String erro, String mensagem, WebRequest request) {
        Map<String, Object> corpo = new LinkedHashMap();
        corpo.put("timestamp", LocalDateTime.now().toString());
        corpo.put("status", status.value());
        corpo.put("erro", erro);
        corpo.put("mensagem", mensagem);
        corpo.put("caminho", request.getDescription(false).replace("uri=", ""));
        corpo.put("sistema", "Motor de Recomendação Giro Tech");
        return corpo;
    }
}