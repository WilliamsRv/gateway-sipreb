package pe.edu.vallegrande.vg_ms_api_gateway.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RequestContext {
    private String id;
    private String path;
    private String method;
    private Map<String, String> headers;
    private AuthToken token;
    private String clientIp;
}
