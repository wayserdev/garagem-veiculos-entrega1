package br.edu.unirv.garagem.exception;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({ConflitoException.class, DataIntegrityViolationException.class,
            ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<Map<String, String>> conflito(Exception ex) {
        String mensagem = ex instanceof ConflitoException ? ex.getMessage() : "Conflito nos dados; tente novamente";
        return resposta(HttpStatus.CONFLICT, mensagem);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> validacao(Exception ex) {
        String mensagem = ex.getMessage();
        if (ex instanceof MethodArgumentNotValidException valid) {
            mensagem = valid.getBindingResult().getFieldErrors().stream()
                    .findFirst().map(e -> e.getDefaultMessage()).orElse("Dados inválidos");
        }
        return resposta(HttpStatus.BAD_REQUEST, mensagem);
    }

    private ResponseEntity<Map<String, String>> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("erro", mensagem));
    }
}
