package org.lin.fitnesscommon.log.service;

/**
 * @author lin
 * @date 2026-03-25
 */

import lombok.extern.slf4j.Slf4j;
import org.lin.fitnesscommon.log.model.LogRecord;
import org.lin.fitnesscommon.log.service.LogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LogServiceImpl implements LogService {

    @Override
    public void saveLog(LogRecord logRecord) {
        // 这里可以保存到数据库、文件或发送到消息队列
        log.info("保存日志: {}", logRecord);
        // TODO: 后续添加保存到数据库
    }

    @Async
    @Override
    public void saveLogAsync(LogRecord logRecord) {
        // 异步保存，不阻塞主业务
        saveLog(logRecord);
    }
}