package pe.edu.vallegrande.vg_ms_api_gateway.domain.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
