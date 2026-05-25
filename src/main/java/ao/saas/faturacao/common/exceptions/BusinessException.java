package ao.saas.faturacao.common.exceptions;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(message, HttpStatus.NOT_FOUND);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(message, HttpStatus.CONFLICT);
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(message, HttpStatus.FORBIDDEN);
    }
}
