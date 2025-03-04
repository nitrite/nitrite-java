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

package org.dizitart.no2.filters;

import java.util.List;

/**
 * Represents a filter which can be flattened or otherwise consists of multiple constituent filters.
 *
 * <p> [PR notes]
 * <ul>
 *     <li>We can't use {@code instanceof SomeClass} to trigger the application of "and"-like flattening,
 *     because any classes defined in submodules aren't available here in the `nitrite` module at compile-time.
 *     that interface of course needs to be here in the `nitrite` module, just like IndexOnlyFilter is.</li>
 *
 *     <li>There are plenty of things we could call this. "Andlike" was the first thing I came up with but
 *     that sounded terrible. Flattenable is at least a "functional" naming, in that it says what it is, but this
 *     also feels like it's asking for a {@code flatten} method to be added to the interface. But that then implies
 *     that some of the logic in FindOptimizer would end up distributed across multiple classes. In its current state,
 *     it's very helpful that all the related logic is right there in one file. </li>
 *
 *     <li>If we want to keep {@code getFilters} as the interface, then names like CompoundFilter and CompositeFilter
 *     come to mind. However, that would leave FindOptimizer with a strange asymmetry where `and` is just a special
 *     case of this interface but `or` is still its own thing with separate handling.</li>
 * </ul>
 *
 * // TODO add a "@since" tag
 */
public interface FlattenableFilter {
    /**
     * [PR note] Clearly this is not the ideal contract for this interface, but it allowed existing code to
     * be leveraged to get the proof-of-concept working.
     */
    List<Filter> getFilters();
}
