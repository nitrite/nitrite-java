/*
 * Copyright (c) 2017-2020. Nitrite author or authors.
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

package org.dizitart.no2.repository.annotations;

import java.lang.annotation.*;

/**
 * Annotation to mark a class that should consider all index related
 * annotations - <code>@Id</code>, <code>@Index</code>,
 * <code>@Indices</code> from its superclass.
 *
 * @author Anindya Chatterjee
 * @since 2.1.0
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InheritIndices {
}
