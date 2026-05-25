package ao.saas.faturacao.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private int statusCode;
    private T data;
    private String message;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(200)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(201)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(200)
                .data(data)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ApiResponse<Void> ok(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .statusCode(200)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
