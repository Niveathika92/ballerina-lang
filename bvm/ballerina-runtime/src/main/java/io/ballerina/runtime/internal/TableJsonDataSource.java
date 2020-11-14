/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.values.ArrayValue;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import io.ballerina.runtime.internal.values.MapValueImpl;
import io.ballerina.runtime.internal.values.TupleValueImpl;

import java.io.IOException;

/**
 * {@link JsonDataSource} implementation for table.
 *
 * @since 1.0
 */
public class TableJsonDataSource implements JsonDataSource {

    private BTable tableValue;
    private JsonObjectGenerator objGen;

    public TableJsonDataSource(BTable tableValue) {
        this(tableValue, new DefaultJsonObjectGenerator());
    }

    private TableJsonDataSource(BTable tableValue, JsonObjectGenerator objGen) {
        this.tableValue = tableValue;
        this.objGen = objGen;
    }

    @Override
    public void serialize(JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        while (this.hasNext()) {
            gen.serialize(this.next());
        }
        gen.writeEndArray();
    }

    @Override
    public boolean hasNext() {
        return this.tableValue.getIterator().hasNext();
    }

    @Override
    public Object next() {
        return this.tableValue.getIterator().next();
    }

    @Override
    public Object build() {
        ArrayValue values = new ArrayValueImpl(new BArrayType(PredefinedTypes.TYPE_JSON));
        BIterator itr = this.tableValue.getIterator();
        while (itr.hasNext()) {
            TupleValueImpl tupleValue = (TupleValueImpl) itr.next();
            MapValueImpl record = ((MapValueImpl) tupleValue.get(0));
            try {
                values.append(this.objGen.transform(record));
            } catch (IOException e) {
                throw new BallerinaException(e);
            }
        }
        return values;
    }
}
