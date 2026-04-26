package org.lin.fitnesscommon.log.service;

/**
 * @author lin
 * @date 2026-03-25
 */

import org.lin.fitnesscommon.log.model.LogRecord;

public interface LogService {

    /**
     * 保存日志
     */
    void saveLog(LogRecord logRecord);

    /**
     * 异步保存日志
     */
    void saveLogAsync(LogRecord logRecord);
}