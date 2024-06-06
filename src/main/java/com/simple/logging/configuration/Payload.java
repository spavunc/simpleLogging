package com.simple.logging.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payload {
    private String httpMethod;
    private String requestUrl;
    private String requestHandler;
    private Integer httpStatus;
    private String requestBody;
    private String responseBody;
}
