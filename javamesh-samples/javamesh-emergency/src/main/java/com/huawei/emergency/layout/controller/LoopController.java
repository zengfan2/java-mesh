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

package com.huawei.emergency.layout.controller;

import com.huawei.emergency.layout.TestElement;
import com.huawei.emergency.layout.ElementProcessContext;
import com.huawei.emergency.layout.template.GroovyMethodTemplate;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author y30010171
 * @since 2021-12-16
 **/
@Data
public class LoopController implements Controller{

    private String title;
    private String comment;
    private boolean isForever;
    private int loopCount;
    private List<TestElement> testElements = new ArrayList<>();

    @Override
    public void handle(ElementProcessContext context) {
        GroovyMethodTemplate currentMethod = context.getCurrentMethod();
        if (loopCount <= 0){
            throw new IllegalArgumentException("循环控制器的次数不能小于等于0");
        }
        currentMethod.addContent(String.format(Locale.ROOT,"for (i in 0..< %s) {",loopCount),2);
        testElements.forEach(handler -> {
            context.setCurrentMethod(currentMethod);
            handler.handle(context);
        });
        currentMethod.addContent("}",2);
    }

    @Override
    public List<TestElement> nextElements() {
        return testElements;
    }
}