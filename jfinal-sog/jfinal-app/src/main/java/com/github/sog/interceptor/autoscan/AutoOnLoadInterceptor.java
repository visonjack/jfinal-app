package com.github.sog.interceptor.autoscan;

import com.github.sog.annotation.AppInterceptor;
import japp.init.ctxbox.ClassBox;
import japp.init.ctxbox.ClassType;
import com.jfinal.aop.Interceptor;
import com.jfinal.config.Interceptors;
import japp.Logger;

import java.util.List;

/**
 * <p>
 * Interceptor annotation scan.
 * </p>
 *
 * @author sagyf yang
 * @version 1.0 2014-01-04 13:11
 * @since JDK 1.6
 */
public class AutoOnLoadInterceptor {

    private final Interceptors interceptors;


    public AutoOnLoadInterceptor(Interceptors interceptors) {
        this.interceptors = interceptors;
    }

    public void load() {
        List<Class> interceptorClass = ClassBox.getInstance().getClasses(ClassType.AOP);
        if (interceptorClass != null && !interceptorClass.isEmpty()) {
            AppInterceptor interceptor;

            for (Class interceptorClas : interceptorClass) {
                interceptor = (AppInterceptor) interceptorClas.getAnnotation(AppInterceptor.class);
                if (interceptor != null) {
                    try {
                        interceptors.add((Interceptor) interceptorClas.newInstance());
                    } catch (InstantiationException e) {
                        Logger.error("instance aop interceptor is error!", e);
                        throw new IllegalArgumentException("instance aop interceptor is error!");
                    } catch (IllegalAccessException e) {
                        Logger.error("instance aop interceptor is error!", e);
                        throw new IllegalArgumentException("instance aop interceptor is error!");
                    }
                }
            }
        }

    }
}
