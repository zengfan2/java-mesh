/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.premain.agent.template;

import java.lang.reflect.Method;
import java.util.List;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.huawei.apm.bootstrap.lubanops.Interceptor;

/**
 * 启动类实例方法模板
 * <p>启动类加载器加载类的实例方法如果需要增强，则需要使用该模板
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/27
 */
public class BootstrapInstTemplate {
    /**
     * luban拦截器列表
     */
    private static List<? extends Interceptor> originInterceptors;

    /**
     * 拦截器列表
     */
    private static List<? extends InstanceMethodInterceptor> interceptors;

    /**
     * 初始化两组拦截器
     *
     * @param originInterceptors luban拦截器列表
     * @param interceptors       拦截器列表
     */
    public static void prepare(List<? extends Interceptor> originInterceptors,
            List<? extends InstanceMethodInterceptor> interceptors) {
        BootstrapInstTemplate.originInterceptors = originInterceptors;
        BootstrapInstTemplate.interceptors = interceptors;
    }

    /**
     * 方法执行前调用
     * <p>由于类加载器限制，需要使用反射调用外部方法，需要构建出动态advice类的全限定名，再用当前类加载器加载
     * <p>由于jvm重定义的限制，不能添加静态属性，动态advice类只能通过局部参数传递
     *
     * @param obj       被增强对象
     * @param method    被增强方法
     * @param arguments 所有参数
     * @param adviceCls 动态advice类
     * @return 是否跳过主要流程
     * @throws Exception 发生异常
     */
    @Advice.OnMethodEnter(skipOn = Advice.OnDefaultValue.class)
    public static boolean onMethodEnter(
            @Advice.This(typing = Assigner.Typing.DYNAMIC) Object obj,
            @Advice.Origin Method method,
            @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] arguments,
            @Advice.Local(value = "ADVICE_CLS") Class<?> adviceCls) throws Exception {
        final StringBuilder sb = new StringBuilder()
                .append(method.getDeclaringClass().getName())
                .append('#')
                .append(method.getName())
                .append("(");
        for (Class<?> parameterType : method.getParameterTypes()) {
            sb.append(parameterType.getName()).append(',');
        }
        sb.append(')');
        final String adviceClsName = "com.huawei.apm.premain.agent.template.BootstrapInstTemplate_" +
                Integer.toHexString(sb.toString().hashCode());
        adviceCls = Thread.currentThread().getContextClassLoader().loadClass(adviceClsName);
        Object[] dynamicArgs = arguments;
        final Boolean res = (Boolean) adviceCls.getDeclaredMethod("beforeInstMethod", Object.class, Method.class,
                Object[].class).invoke(null, obj, method, dynamicArgs);
        arguments = dynamicArgs;
        return res;
    }

    /**
     * 调用luban拦截器的onStart方法和实例拦截器的before方法
     *
     * @param obj       被拦截的对象
     * @param method    被拦截的方法
     * @param arguments 所有参数
     * @return 是否跳过主要流程
     * @throws Exception 调用异常
     */
    public static boolean beforeInstMethod(Object obj, Method method, Object[] arguments) throws Exception {
        for (Interceptor interceptor : originInterceptors) {
            arguments = interceptor.onStart(obj, arguments, obj.getClass().getName(), method.getName());
        }
        final BeforeResult beforeResult = new BeforeResult();
        for (InstanceMethodInterceptor interceptor : interceptors) {
            interceptor.before(obj, method, arguments, beforeResult);
            if (!beforeResult.isContinue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 方法执行后调用
     *
     * @param obj       被拦截对象
     * @param method    被拦截方法
     * @param arguments 所有参数
     * @param result    调用结果
     * @param throwable 抛出异常
     * @param adviceCls 动态advice类
     * @throws Exception 调用异常
     */
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void OnMethodExit(
            @Advice.This(typing = Assigner.Typing.DYNAMIC) Object obj,
            @Advice.Origin Method method,
            @Advice.AllArguments Object[] arguments,
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
            @Advice.Thrown Throwable throwable,
            @Advice.Local(value = "ADVICE_CLS") Class<?> adviceCls) throws Exception {
        result = adviceCls.getDeclaredMethod("afterInstMethod", Object.class, Method.class, Object[].class,
                Object.class, Throwable.class).invoke(null, obj, method, arguments, result, throwable);
    }

    /**
     * 调用luban拦截器的onFinally、onError方法和实例拦截器的after、onThrow方法
     *
     * @param obj       被拦截的对象
     * @param method    被拦截方法
     * @param arguments 所有参数
     * @param result    调用结果
     * @param throwable 抛出异常
     * @return 调用结果
     * @throws Exception 调用异常
     */
    public static Object afterInstMethod(Object obj, Method method, Object[] arguments, Object result,
            Throwable throwable) throws Exception {
        if (throwable == null) {
            for (Interceptor interceptor : originInterceptors) {
                interceptor.onFinally(obj, arguments, result, obj.getClass().getName(), method.getName());
            }
            for (InstanceMethodInterceptor interceptor : interceptors) {
                result = interceptor.after(obj, method, arguments, result);
            }
        } else {
            for (Interceptor interceptor : originInterceptors) {
                interceptor.onError(obj, arguments, throwable, obj.getClass().getName(), method.getName());
            }
            for (InstanceMethodInterceptor interceptor : interceptors) {
                interceptor.onThrow(obj, method, arguments, throwable);
            }
        }
        return result;
    }
}
