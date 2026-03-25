package pe.edu.vallegrande.vg_ms_api_gateway.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.model.AuthToken;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.in.ValidateTokenUseCase;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.out.TokenValidationPort;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ValidateTokenService implements ValidateTokenUseCase {

    private final TokenValidationPort tokenValidationPort;

    @Override
    public Mono<Boolean> validate(AuthToken token) {
        return tokenValidationPort.isValid(token);
    }
}
