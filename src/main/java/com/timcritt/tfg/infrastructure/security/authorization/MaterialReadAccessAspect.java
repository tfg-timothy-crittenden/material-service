package com.timcritt.tfg.infrastructure.security.authorization;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class MaterialReadAccessAspect {

    private final MaterialReadAuthorizationService authorizationService;

    @Around("@annotation(com.timcritt.tfg.infrastructure.security.authorization.RequireMaterialReadAccess)")
    public Object enforceMaterialReadAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        Long materialId = extractMaterialId(joinPoint);
        authorizationService.assertCanRead(materialId);
        return joinPoint.proceed();
    }

    private Long extractMaterialId(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        Annotation[][] annotations = method.getParameterAnnotations();

        for (int i = 0; i < annotations.length; i++) {
            for (Annotation annotation : annotations[i]) {
                if (annotation.annotationType().equals(MaterialId.class)) {
                    Object value = args[i];
                    if (value == null) {
                        throw new IllegalArgumentException("@MaterialId parameter must not be null");
                    }
                    if (value instanceof Number number) {
                        return number.longValue();
                    }
                    throw new IllegalArgumentException("@MaterialId parameter must be numeric");
                }
            }
        }
        throw new IllegalArgumentException("Method annotated with @RequireMaterialReadAccess must define one @MaterialId parameter");
    }
}

