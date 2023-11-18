package org.logitrack.utils.payStack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("email")
    private String email;

    @JsonProperty("customer_code")
    private String customerCode;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("metadata")
    private Object metadata;

    @JsonProperty("risk_action")
    private String riskAction;

    @JsonProperty("international_format_phone")
    private Object internationalFormatPhone;
}
