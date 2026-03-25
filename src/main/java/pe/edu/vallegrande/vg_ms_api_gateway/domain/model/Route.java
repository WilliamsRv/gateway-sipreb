package pe.edu.vallegrande.vg_ms_api_gateway.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    private String id;
    private String uri;
    private List<String> paths;
    private List<String> methods;
}
