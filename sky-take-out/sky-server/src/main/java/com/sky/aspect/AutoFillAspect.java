package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充
 */
@Aspect
@Component
@Slf4j       //这个用于经常需要日志来测试的类
public class AutoFillAspect {

    /**
     * 切入点：拦截 mapper 包下所有贴了 @AutoFill 注解的方法
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    /**
     * 前置通知：在SQL执行之前给公共字段赋值
     */
    //通知是挂在切入点上的，所以要找特定切入点
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        //jionpoint 是SpringBoot扫描到mapper的添加方法后，拦截到发现通知方法参数需要，就自动生成一个，填入
        log.info("开始进行公共字段自动填充...");

        //1. 拿到方法签名（强转成 MethodSignature 才能取到 Method 对象）
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        //2. 拿到方法上贴的 @AutoFill 注解，方便查看执行操作类型
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);

        //3. 读出注解里带的操作类型（INSERT 还是 UPDATE）
        OperationType operationType = autoFill.value();

        //4. 拿到方法参数（要填充的那个实体对象）
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];//这个数组是所有传进来的参数，对于只传进来一个参数的，当然是args[0]

        //5. 准备要赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();  //BaseContext是自己写的

        //6. 根据操作类型，用反射给对应的属性赋值
        if (operationType == OperationType.INSERT) {
            //插入操作：补 4 个字段
            try {
                Method setCreateTime = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                //第一个参数其实是个防错机制，输成常量的话，输错了会报错（小写的编译时是不会报错的）
                // 最后那个参数是用来确定是哪个类的方法的，防止重名的方法调用错了。
                Method setCreateUser = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                log.error("公共字段自动填充失败", e);
            }
        } else if (operationType == OperationType.UPDATE) {
            //更新操作：只补 2 个字段
            try {
                Method setUpdateTime = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                log.error("公共字段自动填充失败", e);
            }
        }
    }
}
