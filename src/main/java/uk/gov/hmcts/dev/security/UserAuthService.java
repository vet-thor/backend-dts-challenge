package uk.gov.hmcts.dev.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.dev.dto.AuthRequest;
import uk.gov.hmcts.dev.dto.AuthResponse;
import uk.gov.hmcts.dev.dto.JwtUserDetails;

@Service
@RequiredArgsConstructor
class UserAuthService {
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(AuthRequest request){
        var authenticate = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        SecurityContextHolder.getContext().setAuthentication(authenticate);
        var userDetails = (JwtUserDetails) authenticate.getPrincipal();
        var jwt = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType(JwtConstant.BEARER)
                .build();
    }
}
