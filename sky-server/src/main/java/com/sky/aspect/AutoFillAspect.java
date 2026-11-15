package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class AutoFillAspect {

    //切入点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void pointCut(){}

    //通知
    @Before("pointCut()")
    public void AutoFill(JoinPoint joinPoint){
        log.info("自动填充字段");

        //获取签名，判断是新增还是修改
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //获取目标对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0){
            return;
        }

        Object element = args[0];

//        因为 joinPoint.getArgs() 返回的是 Object[]，所以 element 在这段通用切面代码里只被当作 Object。Object 没有 setCreateTime()、setUpdateUser() 这些方法，编译器也不知道它实际是 Category、
//        Employee 还是其他实体，因此不能直接写set方法

        //填充公共字段
        if(operationType == OperationType.INSERT){
            try {
                element.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class).invoke(element, LocalDateTime.now());
                element.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class).invoke(element, BaseContext.getCurrentId());
                element.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class).invoke(element, LocalDateTime.now());
                element.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(element, BaseContext.getCurrentId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (operationType == OperationType.UPDATE) {
            try {
                element.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class).invoke(element, LocalDateTime.now());
                element.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class).invoke(element, BaseContext.getCurrentId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
