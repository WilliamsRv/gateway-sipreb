package pe.edu.vallegrande.vg_ms_api_gateway.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RateLimitResult {
    private boolean allowed;
    private long limit;
    private long remaining;
}
