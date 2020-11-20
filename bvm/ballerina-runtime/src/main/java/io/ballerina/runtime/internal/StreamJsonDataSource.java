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

package io.ballerina.runtime.internal;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BStream;
import io.ballerina.runtime.api.values.BValue;
import io.ballerina.runtime.internal.scheduling.Scheduler;
import io.ballerina.runtime.internal.types.BArrayType;
import io.ballerina.runtime.internal.util.exceptions.BallerinaException;
import io.ballerina.runtime.internal.values.MapValue;
import io.ballerina.runtime.internal.values.MapValueImpl;

import java.io.IOException;


/**
 * {@link JsonDataSource} implementation for streams.
 */
public class StreamJsonDataSource implements JsonDataSource {
    private BStream streamValue;
    private JsonObjectGenerator objGen;
    private Object nextObject = null;
    private boolean hasNext = true;

    StreamJsonDataSource(BStream streamValue) {
        this(streamValue, new DefaultJsonObjectGenerator());
    }

    private StreamJsonDataSource(BStream streamValue, JsonObjectGenerator objGen) {
        this.streamValue = streamValue;
        this.objGen = objGen;
        processNextElement();
    }

    // Here always the next element is accessed before the next() is called. This is to check state of hasNext()
    private void processNextElement() {
        BValue nextElement =
                ((BValue) ((BObject) this.streamValue.getIteratorObj()).call(Scheduler.getStrand(), "next"));
        if (nextElement == null) {
            this.hasNext = false;
            this.nextObject = null;
        } else if (nextElement.getType().getTag() != TypeTags.ERROR_TAG){
            this.nextObject = ((MapValue) nextElement).getMapValue(StringUtils.fromString("value"));
        } else {
            this.nextObject = nextElement;
        }
    }

    @Override
    public void serialize(JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStartArray();
        while (this.hasNext()) {
            Object next = this.next();
            if (((BObject) next).getType().getTag() == TypeTags.ERROR_TAG) {
                throw new IOException("Error serialising stream '" + ((BError) next).getMessage());
            } else {
                jsonGenerator.serialize(next);
            }
        }
        jsonGenerator.writeEndArray();
    }

    @Override
    public boolean hasNext() {
        return this.hasNext;
    }

    @Override
    public Object next() {
        Object currentNextValue = this.nextObject;
        processNextElement();
        return currentNextValue;
    }

    @Override
    public Object build() {
        BArray values = ValueCreator.createArrayValue(new BArrayType(PredefinedTypes.TYPE_JSON));
        while (this.hasNext()) {
            Object next = this.next();
            if (((BObject) next).getType().getTag() == TypeTags.ERROR_TAG) {
                throw (BError) next;
            }
            MapValueImpl recordValue = (MapValueImpl) next;
            MapValueImpl record = ((MapValueImpl) recordValue.getMapValue(StringUtils.fromString("value")));
            try {
                values.append(this.objGen.transform(record));
            } catch (IOException e) {
                throw new BallerinaException(e);
            }
        }
        return values;
    }
}
