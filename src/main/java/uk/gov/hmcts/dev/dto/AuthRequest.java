package uk.gov.hmcts.dev.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credentials for user authentication")
public record AuthRequest(
        @Schema(description = "The registered username", example = "staff")
        String username,
        @Schema(description = "The user password", example = "pass123")
        String password
) {
}
