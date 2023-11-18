package org.logitrack.utils.payStack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Log {

    @JsonProperty("start_time")
    private Long startTime;

    @JsonProperty("time_spent")
    private Integer timeSpent;

    @JsonProperty("attempts")
    private Integer attempts;

    @JsonProperty("errors")
    private Integer errors;

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("mobile")
    private Boolean mobile;

    @JsonProperty("input")
    private List<Object> input;

    @JsonProperty("history")
    private List<History> history;
}
