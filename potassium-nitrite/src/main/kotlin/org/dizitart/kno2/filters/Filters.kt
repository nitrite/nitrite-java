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

package org.dizitart.kno2.filters

import org.dizitart.no2.filters.Filter
import org.dizitart.no2.filters.FluentFilter
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Point
import kotlin.reflect.KProperty

/**
 * @since 2.1.0
 * @author Anindya Chatterjee
 */

/**
 * Creates an equality filter which matches documents where the value
 * of a field equals the specified [value].
 */
inline infix fun <reified T> String.eq(value: T?): Filter = FluentFilter.where(this).eq(value)

/**
 * Creates a greater than filter which matches those documents where the value
 * of the value is greater than (i.e. >) the specified [value].
 */
inline infix fun <reified T : Comparable<T>> String.gt(value: T?): Filter = FluentFilter.where(this).gt(value)

/**
 * Creates a greater equal filter which matches those documents where the value
 * of the value is greater than or equals to (i.e. >=) the specified [value].
 */
inline infix fun <reified T : Comparable<T>> String.gte(value: T?): Filter = FluentFilter.where(this).gte(value)

/**
 * Creates a lesser than filter which matches those documents where the value
 * of the value is less than (i.e. <) the specified [value].
 */
inline infix fun <reified T : Comparable<T>> String.lt(value: T?): Filter = FluentFilter.where(this).lt(value)

/**
 * Creates a lesser equal filter which matches those documents where the value
 * of the value is lesser than or equals to (i.e. <=) the specified [value].
 */
inline infix fun <reified T : Comparable<T>> String.lte(value: T?): Filter = FluentFilter.where(this).lte(value)

/**
 * Creates a between filter which matches those documents where the value
 * of the field is within the specified bound including the end values.
 */
inline fun <reified T : Comparable<T>> String.between(lowerBound: T, upperBound: T): Filter
    = FluentFilter.where(this).between(lowerBound, upperBound)

/**
 * Creates a between filter which matches those documents where the value
 * of the field is within the specified bound.
 * */
inline fun <reified T : Comparable<T>> String.between(lowerBound: T, upperBound: T, inclusive: Boolean): Filter
    = FluentFilter.where(this).between(lowerBound, upperBound, inclusive)

/**
 * Creates a between filter which matches those documents where the value
 * of the field is within the specified bound.
 * */
inline fun <reified T : Comparable<T>> String.between(lowerBound: T, upperBound: T, lowerInclusive: Boolean,
                                                      upperInclusive: Boolean): Filter
    = FluentFilter.where(this).between(lowerBound, upperBound, lowerInclusive, upperInclusive)

/**
 * Creates an in filter which matches the documents where
 * the value of a field equals any value in the specified array of [values].
 */
inline infix fun <reified T : Comparable<T>> String.within(values: Array<out T>): Filter = FluentFilter.where(this).`in`(*values)

/**
 * Creates an in filter which matches the documents where
 * the value of a field equals any value in the specified array of [values].
 */
inline infix fun <reified T : Comparable<T>> String.within(values: Iterable<T>): Filter = FluentFilter.where(this).`in`(*(values.toList().toTypedArray()))

/**
 * Creates an element match filter that matches documents that contain an array
 * value with at least one element that matches the specified [filter].
 */
infix fun String.elemMatch(filter: Filter): Filter = FluentFilter.where(this).elemMatch(filter)

/**
 * Creates a text filter which performs a text search on the content of the fields
 * indexed with a full-text index.
 */
infix fun String.text(value: String?): Filter = FluentFilter.where(this).text(value)

/**
 * Creates a string filter which provides regular expression capabilities
 * for pattern matching strings in documents.
 */
infix fun String.regex(value: String?): Filter = FluentFilter.where(this).regex(value)

inline infix fun <reified T : Geometry> String.within(value: T?): Filter = org.dizitart.no2.spatial.FluentFilter.where(this).within(value)

inline infix fun <reified T : Geometry> String.intersects(value: T?): Filter = org.dizitart.no2.spatial.FluentFilter.where(this).intersects(value)

inline fun <reified T : Coordinate> String.near(value: T?, distance: Double): Filter = org.dizitart.no2.spatial.FluentFilter.where(this).near(value, distance)

inline fun <reified T : Point> String.near(value: T?, distance: Double): Filter = org.dizitart.no2.spatial.FluentFilter.where(this).near(value, distance)

/**
 * Creates an and filter which performs a logical AND operation on two filters and selects
 * the documents that satisfy both filters.
 */
inline infix fun <reified T : Filter> Filter.and(filter: T): Filter = this.and(filter)

/**
 * Creates an or filter which performs a logical OR operation on two filters and selects
 * the documents that satisfy at least one of the filter.
 */
inline infix fun <reified T : Filter> Filter.or(filter: T): Filter = this.or(filter)

/**
 * Creates an equality filter which matches objects where the value
 * of a property equals the specified [value].
 */
inline infix fun <reified T> KProperty<T?>.eq(value: T?): Filter = FluentFilter.where(this.name).eq(value)

/**
 * Creates a greater than filter which matches those objects where the value
 * of the property is greater than (i.e. >) the specified [value].
 */
inline infix fun <reified T : Comparable<T>> KProperty<T?>.gt(value: T?): Filter = FluentFilter.where(this.name).gt(value)

/**
 * Creates a greater equal filter which matches those objects where the value
 * of the property is greater than or equals to (i.e. >=) the specified [value].
 */
inline infix fun <reified T : Comparable<T>> KProperty<T?>.gte(value: T?): Filter = FluentFilter.where(this.name).gte(value)

/**
 * Creates a lesser than filter which matches those objects where the value
 * of the property is less than (i.e. <) the specified [value].
 */
inline infix fun <reified T : Comparable<T>> KProperty<T?>.lt(value: T?): Filter = FluentFilter.where(this.name).lt(value)

/**
 * Creates a lesser equal filter which matches those objects where the value
 * of the property is lesser than or equals to (i.e. <=) the specified [value].
 */
inline infix fun <reified T : Comparable<T>> KProperty<T?>.lte(value: T?): Filter = FluentFilter.where(this.name).lte(value)

/**
 * Creates an in filter which matches the objects where
 * the value of a property equals any value in the specified array of [values].
 */
inline infix fun <reified T : Comparable<T>> KProperty<T?>.within(values: Array<T>): Filter = FluentFilter.where(this.name).`in`(*values)

/**
 * Creates an in filter which matches the objects where
 * the value of a property equals any value in the specified list of [values].
 */
inline infix fun <reified T : Comparable<T>> KProperty<T?>.within(values: Iterable<T>): Filter = FluentFilter.where(this.name).`in`(*(values.toList().toTypedArray()))

/**
 * Creates an element match filter that matches objects that contain an array
 * value with at least one element that matches the specified [filter].
 */
inline infix fun <reified T> KProperty<Iterable<T>?>.elemMatch(filter: Filter): Filter = FluentFilter.where(this.name).elemMatch(filter)

/**
 * Creates a text filter which performs a text search on the content of the property
 * indexed with a full-text index.
 */
infix fun KProperty<String?>.text(value: String?): Filter = FluentFilter.where(this.name).text(value)

/**
 * Creates a string filter which provides regular expression capabilities
 * for pattern matching strings in objects.
 */
infix fun KProperty<String?>.regex(value: String?): Filter = FluentFilter.where(this.name).regex(value)

inline infix fun <reified T : Geometry> KProperty<T?>.within(value: T?): Filter = org.dizitart.no2.spatial.FluentFilter.where(this.name).within(value)

inline infix fun <reified T : Geometry> KProperty<T?>.intersects(value: T?): Filter = org.dizitart.no2.spatial.FluentFilter.where(this.name).intersects(value)

inline fun <reified T : Geometry> KProperty<T?>.near(value: Point, distance: Double): Filter = org.dizitart.no2.spatial.FluentFilter.where(this.name).near(value, distance)

inline fun <reified T : Geometry> KProperty<T?>.near(value: Coordinate, distance: Double): Filter = org.dizitart.no2.spatial.FluentFilter.where(this.name).near(value, distance)
