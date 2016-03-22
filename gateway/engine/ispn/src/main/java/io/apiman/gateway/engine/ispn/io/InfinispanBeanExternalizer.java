/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.gateway.engine.ispn.io;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.Set;

import org.infinispan.commons.marshall.AdvancedExternalizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

/**
 * A simple Jackson/JSON based externalizer base class for all apiman gateway
 * bean externalizers.  This class turns a bean into JSON and then back again.
 * @author eric.wittmann@gmail.com
 */
public abstract class InfinispanBeanExternalizer<T> implements AdvancedExternalizer<T> {
    
    private static final long serialVersionUID = 3682358477036938263L;
    
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.setDateFormat(new ISO8601DateFormat());
    }
    
    /**
     * @see org.infinispan.commons.marshall.Externalizer#writeObject(java.io.ObjectOutput, java.lang.Object)
     */
    @Override
    public void writeObject(ObjectOutput output, T bean) throws IOException {
        output.writeUTF(mapper.writerFor(bean.getClass()).writeValueAsString(bean));
    }
    
    /**
     * @see org.infinispan.commons.marshall.Externalizer#readObject(java.io.ObjectInput)
     */
    @Override
    public T readObject(ObjectInput input) throws IOException, ClassNotFoundException {
        String json = input.readUTF();
        return mapper.reader(getBeanClass()).readValue(json);
    }

    /**
     * @return the class this externalizer is responsible for
     */
    protected abstract Class<T> getBeanClass();
    
    /**
     * @see org.infinispan.commons.marshall.AdvancedExternalizer#getTypeClasses()
     */
    @Override
    public Set<Class<? extends T>> getTypeClasses() {
        return Collections.singleton(getBeanClass());
    }

}
