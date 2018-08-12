/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.objects.filters;

import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.internals.NitriteService;
import org.dizitart.no2.objects.ObjectFilter;

/**
 * An abstract implementation of {@link ObjectFilter}.
 *
 * @author Anindya Chatterjee.
 * @since 1.0
 */
public abstract class BaseObjectFilter implements ObjectFilter {
    /**
     * The Nitrite service.
     */
    protected NitriteService nitriteService;

    /**
     * The {@link NitriteMapper} instance.
     */
    protected NitriteMapper nitriteMapper;

    @Override
    public void setNitriteService(NitriteService nitriteService) {
        this.nitriteService = nitriteService;
    }

    @Override
    public void setNitriteMapper(NitriteMapper nitriteMapper) {
        this.nitriteMapper = nitriteMapper;
    }
}
