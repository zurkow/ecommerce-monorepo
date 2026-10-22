package com.ecommerce.common;

import com.ecommerce.common.error.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Registered in META-INF/spring/...AutoConfiguration.imports, so any service that
 * has this jar on its classpath gets the shared beans automatically - no
 * extra @ComponentScan needed in the services.
 */
@AutoConfiguration
@Import(GlobalExceptionHandler.class)
public class CommonAutoConfiguration {
}
