package uk.gov.hmcts.dev.security;

interface JwtConstant {
    String AUTHORIZATION = "Authorization";
    String BEARER = "Bearer ";
    String ROLE = "role";
    String TYP = "typ";
    String JWT = "JWT";
    String[] PUBLIC_URLS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/h2-console/**",
            "/api/v2/auth/**"
    };
}
