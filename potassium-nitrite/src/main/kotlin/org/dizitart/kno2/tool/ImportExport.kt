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

package org.dizitart.kno2.tool

import org.dizitart.no2.Nitrite
import org.dizitart.no2.tool.ExportOptions
import org.dizitart.no2.tool.Exporter
import org.dizitart.no2.tool.Importer
import java.io.*


/**
 * @since 2.1.1
 * @author Anindya Chatterjee.
 */

/**
 * Exports data to a [Writer].
 */
infix fun Nitrite.exportTo(writer: Writer) {
    val exporter = Exporter.of(this)
    exporter.exportTo(writer)
}

/**
 * Exports data to a [Writer] with [options].
 */
fun Nitrite.exportTo(writer: Writer, options: ExportOptions) {
    val exporter = Exporter.of(this)
    exporter.withOptions(options)
    exporter.exportTo(writer)
}

/**
 * Exports data to a [OutputStream].
 */
infix fun Nitrite.exportTo(stream: OutputStream) {
    val exporter = Exporter.of(this)
    exporter.exportTo(stream)
}

/**
 * Exports data to a [OutputStream] with [options].
 */
fun Nitrite.exportTo(stream: OutputStream, options: ExportOptions) {
    val exporter = Exporter.of(this)
    exporter.withOptions(options)
    exporter.exportTo(stream)
}

/**
 * Exports data to a [File].
 */
infix fun Nitrite.exportTo(file: File) {
    val exporter = Exporter.of(this)
    exporter.exportTo(file)
}

/**
 * Exports data to a [File] with [options].
 */
fun Nitrite.exportTo(file: File, options: ExportOptions) {
    val exporter = Exporter.of(this)
    exporter.withOptions(options)
    exporter.exportTo(file)
}

/**
 * Exports data to a [file].
 */
infix fun Nitrite.exportTo(file: String) {
    val exporter = Exporter.of(this)
    exporter.exportTo(file)
}

/**
 * Exports data to a [file] with [options].
 */
fun Nitrite.exportTo(file: String, options: ExportOptions) {
    val exporter = Exporter.of(this)
    exporter.withOptions(options)
    exporter.exportTo(file)
}

/**
 * Imports data from a [file].
 */
infix fun Nitrite.importFrom(file: String) {
    val importer = Importer.of(this)
    importer.importFrom(file)
}

/**
 * Imports data from a [File].
 */
infix fun Nitrite.importFrom(file: File) {
    val importer = Importer.of(this)
    importer.importFrom(file)
}

/**
 * Imports data from a [InputStream].
 */
infix fun Nitrite.importFrom(stream: InputStream) {
    val importer = Importer.of(this)
    importer.importFrom(stream)
}

/**
 * Imports data from a [Reader].
 */
infix fun Nitrite.importFrom(reader: Reader) {
    val importer = Importer.of(this)
    importer.importFrom(reader)
}