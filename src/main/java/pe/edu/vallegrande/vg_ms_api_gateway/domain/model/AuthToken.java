package pe.edu.vallegrande.vg_ms_api_gateway.domain.model;

import lombok.Value;

@Value
public class AuthToken {
    String value;

    public AuthToken(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        this.value = value;
    }
}
