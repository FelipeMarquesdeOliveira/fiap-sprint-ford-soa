package br.com.ford.vinshare.infra;

import br.com.ford.vinshare.domain.cliente.ClienteNotFoundException;
import br.com.ford.vinshare.domain.concessionaria.ConcessionariaNotFoundException;
import br.com.ford.vinshare.domain.servico.ServicoNotFoundException;
import br.com.ford.vinshare.domain.veiculo.VeiculoNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleEntityNotFoundException() {
    }

    @ExceptionHandler(ClienteNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleClienteNotFoundException(ClienteNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("mensagem", ex.getMessage());
        return body;
    }

    @ExceptionHandler(ConcessionariaNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleConcessionariaNotFoundException(ConcessionariaNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("mensagem", ex.getMessage());
        return body;
    }

    @ExceptionHandler(VeiculoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleVeiculoNotFoundException(VeiculoNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("mensagem", ex.getMessage());
        return body;
    }

    @ExceptionHandler(ServicoNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleServicoNotFoundException(ServicoNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("mensagem", ex.getMessage());
        return body;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> body = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                body.put(violation.getPropertyPath().toString(), violation.getMessage())
        );
        return body;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> body = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                body.put(error.getField(), error.getDefaultMessage())
        );
        return body;
    }
}