package uk.gov.hmcts.dev.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AuthResponse (
    String accessToken,
    String tokenType
){}
