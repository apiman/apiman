/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.manager.api.core.logging;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import io.apiman.common.logging.IApimanLogger;
import org.apache.commons.lang.ObjectUtils.Null;

/**
 * Allows injection of an {@link IApimanLogger} instance, with a class passed as the requester
 * <tt><pre>@Inject @ApimanLogger(SomeClass.klazz) IApimanLogger logger;</pre></tt>.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, PARAMETER})
public @interface ApimanLogger {
    /**
     * @return the requesting class
     */
    @Nonbinding Class<?> value() default Null.class;
}
