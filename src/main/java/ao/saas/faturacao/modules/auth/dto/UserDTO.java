package ao.saas.faturacao.modules.auth.dto;

import java.util.UUID;

import ao.saas.faturacao.common.enums.UserRole;
import ao.saas.faturacao.modules.users.entity.User;
import lombok.Builder;
import lombok.Data;

@Data @Builder public class UserDTO {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UserRole role;
    private String status;

    public static UserDTO from(User u) {
        return UserDTO.builder()
            .id(u.getId()).email(u.getEmail())
            .firstName(u.getFirstName()).lastName(u.getLastName())
            .phone(u.getPhone()).role(u.getRole()).status(u.getStatus().name())
            .build();
    }
}