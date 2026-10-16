package com.chatboot.documents;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class UploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<UploadRejection> tooLarge(MaxUploadSizeExceededException ignored) {
        return ResponseEntity.badRequest()
                .body(new UploadRejection(UploadRejectedException.Reason.TOO_LARGE.name()));
    }
}
