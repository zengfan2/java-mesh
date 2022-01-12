/*
 * Copyright (C) Ltd. 2021-2021. Huawei Technologies Co., All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.emergency.layout.config;

import com.huawei.common.exception.ApiException;
import com.huawei.emergency.layout.ElementProcessContext;
import com.huawei.emergency.layout.template.GroovyClassTemplate;
import com.huawei.emergency.layout.template.GroovyFieldTemplate;
import com.huawei.emergency.layout.template.GroovyMethodTemplate;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.Locale;

/**
 * @author y30010171
 * @since 2021-12-15
 **/
@Data
public class Counter extends Config {
    private static final String NAME_FORMAT = "counter%s";
    private static final String CONFIG_FORMAT = "new CounterConfig.Builder(startValue: %s, increment: %s, maxValue: %s, numberFormat: \"%s\", sharingMode: %s, resetEachIteration: %s).build()";

    private int startValue;
    private int increment;
    private int maxValue;
    private String numberFormat = "";
    private String exportVariableName;
    private boolean forEachUser;
    private boolean resetOnEachThreadGroup;

    @Override
    public void handle(ElementProcessContext context) {
        GroovyMethodTemplate currentMethod = context.getCurrentMethod(); // 当前方法块
        GroovyClassTemplate currentClass = context.getTemplate(); // 当前类
        String counterName = String.format(Locale.ROOT, NAME_FORMAT, context.getVariableCount().getAndIncrement());
        String configCreateStr;
        if (forEachUser) {
            currentClass.addFiled(GroovyFieldTemplate.create(String.format(Locale.ROOT, "def static %s = new CommonCounter();", counterName)));
            configCreateStr = String.format(Locale.ROOT, CONFIG_FORMAT, startValue, increment, maxValue, numberFormat, "SharingMode.ALL_THREADS", resetOnEachThreadGroup);
        } else {
            currentClass.addFiled(GroovyFieldTemplate.create(String.format(Locale.ROOT, "def %s = new CommonCounter();", counterName)));
            configCreateStr = String.format(Locale.ROOT, CONFIG_FORMAT, startValue, increment, maxValue, numberFormat, "SharingMode.CURRENT_THREAD", resetOnEachThreadGroup);
        }
        currentClass.getBeforeProcessMethod().addContent(String.format(Locale.ROOT, "counter%s.initConfig(%s);", counterName, configCreateStr), 2);
        if (StringUtils.isNotEmpty(exportVariableName)) { // 需要生成参数
            currentClass.addFiled(GroovyFieldTemplate.create(String.format(Locale.ROOT, "def %s;", exportVariableName, counterName)));
            currentMethod.addContent(String.format(Locale.ROOT, "%s = %s.nextNumber();", exportVariableName, counterName), 2);
        } else {
            currentMethod.addContent(String.format(Locale.ROOT, "%s.nextNumber();", counterName), 2);
        }
    }
}