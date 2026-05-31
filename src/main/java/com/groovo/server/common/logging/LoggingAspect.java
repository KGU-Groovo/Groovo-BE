package com.groovo.server.common.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

  private static final int MAX_ARGUMENT_LOG_LENGTH = 500;

  @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
  public void restControllerMethods() {}

  @Around("restControllerMethods()")
  public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
    long startedAt = System.currentTimeMillis();
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String handler = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    String requestId = currentRequestId();
    String arguments = summarizeArguments(joinPoint.getArgs());

    try {
      Object result = joinPoint.proceed();
      long elapsed = System.currentTimeMillis() - startedAt;
      log.info(
          "[METHOD] requestId={} handler={} elapsedMs={} args={} result={}",
          requestId,
          handler,
          elapsed,
          arguments,
          summarizeResult(result));
      return result;
    } catch (Throwable throwable) {
      long elapsed = System.currentTimeMillis() - startedAt;
      log.warn(
          "[EXCEPTION] requestId={} handler={} elapsedMs={} args={} exception={} message={}",
          requestId,
          handler,
          elapsed,
          arguments,
          throwable.getClass().getSimpleName(),
          throwable.getMessage());
      throw throwable;
    }
  }

  private String currentRequestId() {
    if (RequestContextHolder.getRequestAttributes()
        instanceof ServletRequestAttributes attributes) {
      Object requestId =
          attributes.getRequest().getAttribute(RequestLoggingFilter.REQUEST_ID_ATTRIBUTE);
      if (requestId != null) {
        return requestId.toString();
      }
    }
    return "-";
  }

  private String summarizeArguments(Object[] arguments) {
    return Arrays.stream(arguments)
        .map(this::summarizeArgument)
        .collect(Collectors.joining(", ", "[", "]"));
  }

  private String summarizeArgument(Object argument) {
    if (argument == null) {
      return "null";
    }
    if (argument instanceof HttpServletRequest || argument instanceof HttpServletResponse) {
      return argument.getClass().getSimpleName();
    }
    return truncate(String.valueOf(argument));
  }

  private String summarizeResult(Object result) {
    if (result == null) {
      return "null";
    }
    return result.getClass().getSimpleName();
  }

  private String truncate(String value) {
    String singleLine = value.replace("\r", "\\r").replace("\n", "\\n");
    if (singleLine.length() <= MAX_ARGUMENT_LOG_LENGTH) {
      return singleLine;
    }
    return singleLine.substring(0, MAX_ARGUMENT_LOG_LENGTH) + "...[truncated]";
  }
}
