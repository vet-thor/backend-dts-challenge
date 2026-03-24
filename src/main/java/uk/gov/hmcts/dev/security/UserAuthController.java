package uk.gov.hmcts.dev.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.dev.dto.AuthRequest;
import uk.gov.hmcts.dev.dto.AuthResponse;
import uk.gov.hmcts.dev.dto.ResponseData;
import uk.gov.hmcts.dev.dto.ResponseHandler;
import uk.gov.hmcts.dev.util.helper.SuccessMessageHelper;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
class UserAuthController {
    private final UserAuthService authService;
    private final SuccessMessageHelper successHelper;

    @PostMapping
    @Operation(
            summary = "User Login / Authentication",
            description = "Exchanges user credentials (username/password) for a JWT Access Token. This endpoint is public and does not require an Authorization header.",
            tags = {"Authentication"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful - Token returned",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid username or password",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Missing required fields",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    public ResponseEntity<ResponseData<AuthResponse>> authorize(@Valid @RequestBody AuthRequest loginDTO){
        return ResponseHandler.generateResponse(
                successHelper.loginSuccessMessage(),
                HttpStatus.OK,
                authService.login(loginDTO));
    }
}
