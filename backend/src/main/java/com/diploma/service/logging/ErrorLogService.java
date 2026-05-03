package com.diploma.service.logging;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.diploma.entities.ErrorLog;
import com.diploma.repository.ErrorLogRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
@Slf4j
@AllArgsConstructor
public class ErrorLogService {

    private final ErrorLogRepository errorLogRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logError(String errorMessage, String userName, String controllerName, String handlerName) {
        ErrorLog errorLog = new ErrorLog();
        errorLog.setErrorMessage(errorMessage);
        errorLog.setUserName(userName);
        errorLog.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

        errorLog.setControllerName(controllerName);
        errorLog.setHandlerName(handlerName);
        errorLogRepository.save(errorLog);
        log.warn("User: {}, error saving in BD: {}, handler: {}", userName, errorMessage, handlerName);
    }
}
