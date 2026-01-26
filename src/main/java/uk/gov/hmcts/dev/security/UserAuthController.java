package uk.gov.hmcts.dev.security;

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
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
class UserAuthController {
    private final UserAuthService authService;
    private final SuccessMessageHelper successHelper;

    @PostMapping("/")
    public ResponseEntity<ResponseData<AuthResponse>> authorize(@Valid @RequestBody AuthRequest loginDTO){
        return ResponseHandler.generateResponse(
                successHelper.loginSuccessMessage(),
                HttpStatus.OK,
                authService.login(loginDTO));
    }
}
