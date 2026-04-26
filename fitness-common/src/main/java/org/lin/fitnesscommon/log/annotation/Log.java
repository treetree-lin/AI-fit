package org.lin.fitnesscommon.log.annotation;

/**
 * @author lin
 * @date 2026-03-25
 */

import org.lin.fitnesscommon.log.enums.OperateType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作描述
     */
    String value() default "";

    /**
     * 操作类型
     */
    OperateType type() default OperateType.OTHER;

    /**
     * 是否保存请求参数
     */
    boolean saveParams() default true;

    /**
     * 是否保存返回结果
     */
    boolean saveResult() default false;
}