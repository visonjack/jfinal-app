/**
 * Copyright (c) 2011-2013, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.core;

import com.jfinal.aop.Interceptor;
import com.jfinal.sog.kit.cst.AppFunc;
import com.jfinal.sog.kit.cst.StringPool;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Enumeration;

/**
 * ActionReporter
 */
final class ActionReporter {


    /**
     * Report action before action invoking when the common request coming
     */
    static boolean reportCommonRequest(Controller controller, Action action) {
        String content_type = controller.getRequest().getContentType();
        if (content_type == null || !content_type.toLowerCase().contains("multipart")) {    // if (content_type == null || content_type.indexOf("multipart/form-data") == -1) {
            doReport(controller, action);
            return false;
        }
        return true;
    }

    /**
     * Report action after action invoking when the multipart request coming
     */
    static void reportMultipartRequest(Controller controller, Action action) {
        doReport(controller, action);
    }

    private static void doReport(Controller controller, Action action) {
        StringBuilder sb = new StringBuilder("\nJFinal action report -------- ")
                .append(AppFunc.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS.format(new Date()))
                .append(" ------------------------------\n");
        Class<? extends Controller> cc = action.getControllerClass();
        sb.append("Controller  : ").append(cc.getName()).append(".(").append(cc.getSimpleName()).append(".java:1)");
        sb.append("\nMethod      : ").append(action.getMethodName()).append(StringPool.NEWLINE);

        String urlParas = controller.getPara();
        if (urlParas != null) {
            sb.append("UrlPara     : ").append(urlParas).append(StringPool.NEWLINE);
        }

        Interceptor[] inters = action.getInterceptors();
        if (inters.length > 0) {
            sb.append("Interceptor : ");
            for (int i = 0; i < inters.length; i++) {
                if (i > 0)
                    sb.append("\n              ");
                Interceptor inter = inters[i];
                Class<? extends Interceptor> ic = inter.getClass();
                sb.append(ic.getName()).append(".(").append(ic.getSimpleName()).append(".java:1)");
            }
            sb.append(StringPool.NEWLINE);
        }

        // print all parameters
        HttpServletRequest request = controller.getRequest();
        Enumeration<String> e = request.getParameterNames();
        if (e.hasMoreElements()) {
            sb.append("Parameter   : ");
            while (e.hasMoreElements()) {
                String name = e.nextElement();
                String[] values = request.getParameterValues(name);
                if (values.length == 1) {
                    sb.append(name).append(StringPool.EQUALS).append(values[0]);
                } else {
                    sb.append(name).append("[]={");
                    for (int i = 0; i < values.length; i++) {
                        if (i > 0)
                            sb.append(StringPool.COMMA);
                        sb.append(values[i]);
                    }
                    sb.append(StringPool.RIGHT_BRACE);
                }
                sb.append("  ");
            }
            sb.append(StringPool.NEWLINE);
        }
        sb.append("--------------------------------------------------------------------------------\n");
        System.out.print(sb.toString());
    }
}
