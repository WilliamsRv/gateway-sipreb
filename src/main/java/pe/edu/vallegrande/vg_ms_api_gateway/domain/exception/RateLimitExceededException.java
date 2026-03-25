package pe.edu.vallegrande.vg_ms_api_gateway.domain.exception;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
