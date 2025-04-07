package dev.jlipka.esp32sensorsapiclient.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SensorApiException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public SensorErrorResponse handleSensorApiException(SensorApiException err) {
        return new SensorErrorResponse(err.getMessage());
    }
}
