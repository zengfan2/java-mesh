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
import lombok.Data;

import java.util.Locale;

/**
 * @author y30010171
 * @since 2021-12-15
 **/
@Data
public class CsvDataSetConfig extends Config {

    private static final String NEW_CSV_FORMAT = "    static def %s = new CsvParameterized();";
    private static final String CSV_CONFIG_FORMAT = "new ParameterizedConfig.Builder(parameterizedNames: \"%s\".split(\"%s\") as List,parameterizedFile: \"%s\",parameterizedDelimiter: \"%s\",ignoreFirstLine: %s,sharingMode: %s,allowQuotedData: %s,recycleOnEof: %s).build()";
    private static final String CSV_INIT_FORMAT = "%s.initConfig(%s);";

    private String fileName;
    private String fileEncoding; // 暂时无用
    private String variableNames; // not empty
    private boolean ignoreFirstLine;
    private String delimiter = ",";
    private boolean allowQuoteData;
    private boolean recycleOnEof = true;
    private boolean stopOnEof; // 暂时无用
    private String sharingMode;

    @Override
    public void handle(ElementProcessContext context) {
        GroovyClassTemplate classTemplate = context.getTemplate();
        classTemplate.addImport("import com.huawei.test.configelement.impl.CsvParameterized;");
        classTemplate.addImport("import com.huawei.test.configelement.config.ParameterizedConfig;");
        classTemplate.addImport("import com.huawei.test.configelement.enums.SharingMode;");
        String sharingModeStr = "";
        if ("agent".equals(sharingMode)) {
            sharingModeStr = "SharingMode.CURRENT_AGENT";
        } else if ("process".equals(sharingMode)) {
            sharingModeStr = "SharingMode.CURRENT_PROCESS";
        } else if ("thread".equals(sharingMode)) {
            sharingModeStr = "SharingMode.CURRENT_THREAD";
        } else {
            sharingModeStr = "SharingMode.ALL_THREADS";
        }
        String csvVariableName = "csvDatasetConfig" + context.getVariableCount().getAndIncrement();
        classTemplate.addFiled(GroovyFieldTemplate.create(String.format(Locale.ROOT, NEW_CSV_FORMAT, csvVariableName))); // 声明csv变量
        String csvConfig = String.format(Locale.ROOT, CSV_CONFIG_FORMAT, variableNames, delimiter, fileName, delimiter, ignoreFirstLine, sharingModeStr, allowQuoteData, recycleOnEof);
        classTemplate.getBeforeProcessMethod().addContent(String.format(Locale.ROOT, CSV_INIT_FORMAT, csvVariableName, csvConfig), 2); // 初始化csv变量

        // 获取csv解析的参数
        String csvLineValuesVariableName = "csvLineValue" + context.getVariableCount().getAndIncrement();
        classTemplate.getBeforeMethod().addContent(String.format(Locale.ROOT, "def %s = %s.nextLineValue();", csvLineValuesVariableName, csvVariableName), 2);
        // 通过参数名称声明实例变量 并赋值
        for (String name : variableNames.split(delimiter)) {
            classTemplate.addFiled(GroovyFieldTemplate.create(String.format(Locale.ROOT, "    def %s = \"\";", name)));
            classTemplate.getBeforeMethod().addContent(String.format(Locale.ROOT, "%s = %s.get(\"%s\");", name, csvLineValuesVariableName, name), 2);
        }
    }
}