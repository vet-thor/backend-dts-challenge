package uk.gov.hmcts.dev.security;

interface JwtConstant {
    String BEARER = "Bearer ";
    String ROLE = "role";
    String TYP = "typ";
    String JWT = "JWT";
    String[] PUBLIC_URLS = {
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**"
    };
}
