package com.simple.logging.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private LocalDateTime timestamp;
    private String uuid;
}
