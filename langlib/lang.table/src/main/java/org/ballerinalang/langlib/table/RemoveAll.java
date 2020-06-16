/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.table;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.TableValueImpl;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import static org.ballerinalang.util.BLangCompilerConstants.TABLE_VERSION;

/**
 * Native implementation of lang.table:removeAll(table&lt;Type&gt;).
 *
 * @since 1.3.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.table", version = TABLE_VERSION, functionName = "removeAll",
        args = {@Argument(name = "tbl", type = TypeKind.TABLE)},
        isPublic = true
)
public class RemoveAll {

    public static void removeAll(Strand strand, TableValueImpl tbl) {
        try {
            tbl.clear();
        } catch (org.ballerinalang.jvm.util.exceptions.BLangFreezeException e) {
            throw BallerinaErrors.createError(e.getMessage(), "Failed to remove all from table: " + e.getDetail());
        }
    }
}