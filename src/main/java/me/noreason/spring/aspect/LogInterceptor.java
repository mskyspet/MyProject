package me.noreason.spring.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;

/**
 * Created by MSK on 2015/6/25.
 */
@Aspect
public class LogInterceptor {
    
//    @Around("execution(* me.noreason.spring.bean.*.*(..))")
    public Object log(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        System.out.println("Before");
//        System.out.println(Arrays.toString(proceedingJoinPoint.getSignature().getDeclaringTypeName()));
        System.out.println(proceedingJoinPoint.getSignature().getName());
        Object value = proceedingJoinPoint.proceed();
        System.out.println("After");
        return value;
    }
}
