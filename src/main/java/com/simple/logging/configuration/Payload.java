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
    public String httpMethod;
    public String requestUrl;
    public String requestHandler;
    public Integer httpStatus;
    public String requestBody;
    public String responseBody;
}
