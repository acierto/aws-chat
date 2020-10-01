package com.acierto.awschat.server;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class CustomErrorResponse {
    String errorMsg;
    @JsonFormat
    int status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    LocalDateTime timestamp;

    public CustomErrorResponse(HttpStatus status, String errorMsg) {
        this.status = status.value();
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
