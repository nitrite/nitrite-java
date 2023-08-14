/*
 * Copyright (c) 2017-2021 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.common.processors;

import org.dizitart.no2.collection.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a {@link Processor} chain. The processors are executed in the order
 * they are added to the chain.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
public class ProcessorChain implements Processor {
    private final List<Processor> processors;

    /**
     * Instantiates a new Processor chain.
     */
    public ProcessorChain() {
        processors = new ArrayList<>();
    }

    /**
     * Adds a processor to the chain.
     *
     * @param processor the processor
     */
    public void add(Processor processor) {
        processors.add(processor);
    }

    /**
     * Removes a processor from the chain.
     *
     * @param processor the processor
     */
    public void remove(Processor processor) {
        processors.remove(processor);
    }

    @Override
    public Document processBeforeWrite(Document document) {
        Document processed = document;
        for (Processor processor : processors) {
            processed = processor.processBeforeWrite(processed);
        }
        return processed;
    }

    @Override
    public Document processAfterRead(Document document) {
        Document processed = document;
        for (Processor processor : processors) {
            processed = processor.processAfterRead(processed);
        }
        return processed;
    }
}
