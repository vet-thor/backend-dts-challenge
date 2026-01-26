package uk.gov.hmcts.dev.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Component
@AllArgsConstructor
class JWTFilter extends OncePerRequestFilter {
    private UserInfoConfigManager userDetailsService;
    private HandlerExceptionResolver handlerExceptionResolver;
    private JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain chain) throws ServletException, IOException {

        try {
            var token = extractToken(request);
            authenticate(token, request);
            chain.doFilter(request, response);
        }catch (ExpiredJwtException | UsernameNotFoundException e){
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    private void authenticate(String token, HttpServletRequest request){
        UUID id = null;

        if(nonNull(token)) {
            id = jwtUtil.extractId(token);
        }

        if (nonNull(id) && jwtUtil.validateToken(token)) {
            var userDetails = userDetailsService.loadUserById(id);
            var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    private String extractToken(HttpServletRequest request) {
        var header = request.getHeader(JwtConstant.AUTHORIZATION);

        if (nonNull(header) && header.startsWith(JwtConstant.BEARER)) {
            return header.substring(7);
        }

        return null;
    }
}
