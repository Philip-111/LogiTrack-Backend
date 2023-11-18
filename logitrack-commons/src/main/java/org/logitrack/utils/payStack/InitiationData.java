package org.logitrack.utils.payStack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiationData {
     @JsonProperty("authorization_url")
     private String authorization_url;

     @JsonProperty("access_code")
     private String access_code;

     @JsonProperty("reference")
     private String reference;
}
