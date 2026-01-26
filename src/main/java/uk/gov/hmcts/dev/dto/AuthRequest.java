package uk.gov.hmcts.dev.dto;

public record AuthRequest(
        String username,
        String password
) {
}
