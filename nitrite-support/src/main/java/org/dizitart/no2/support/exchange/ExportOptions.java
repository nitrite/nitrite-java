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

 package org.dizitart.no2.support.exchange;

 import com.fasterxml.jackson.core.JsonFactory;
 import lombok.Getter;
 import lombok.Setter;
 
 import java.util.*;
 
 /**
  * Represents export options.
  *
  * @author Anindya Chatterjee
  * @see Exporter
  * @since 1.0
  */
 @Getter
 @Setter
 public class ExportOptions {
     /**
      * Specifies a {@link NitriteFactory} to create a {@link org.dizitart.no2.Nitrite} instance. This
      * instance will be used to export the collections and data.
      * <p>
      * This is a mandatory field. If not specified, the export operation will fail.
      * The {@link NitriteFactory} instance must be able to create a {@link org.dizitart.no2.Nitrite}, so
      * the database must not be open elsewhere. Upon completion of the export operation, the
      * {@link org.dizitart.no2.Nitrite} instance will be closed.
      *
      * @param nitriteFactory the nitriteFactory.
      * @return the nitriteFactory.
      */
     private NitriteFactory nitriteFactory;
 
     /**
      * Specifies a {@link JsonFactory} to create a {@link com.fasterxml.jackson.core.JsonGenerator} instance.
      * This instance will be used to write the export data to a file.
      * <p>
      * This is an optional field. If not specified, a default one will be created.
      *
      * @param jsonFactory the jsonFactory.
      * @return the jsonFactory.
      */
     private JsonFactory jsonFactory;
 
     /**
      * Indicates if the export operation exports indices information.
      * <p>
      * If `true`, the export operation will export indices information. If `false`, the export
      * operation will not export indices information.
      * <p>
      * This is an optional field. If not specified, it will be set to `true`.
      *
      * @param exportIndices a value indicating if indices information will be exported.
      * @return `true` if indices information is exported; otherwise, `false`.
      */
     private boolean exportIndices = true;
 
     /**
      * Indicates if the export operation exports collection data.
      * <p>
      * If `true`, the export operation will export collection data. If `false`, the export
      * operation will not export collection data.
      * <p>
      * This is an optional field. If not specified, it will be set to `true`.
      *
      * @param exportData a value indicating if collection data will be exported.
      * @return `true` if collection data is exported; otherwise, `false`.
      */
     private boolean exportData = true;
 
     /**
      * Specifies a list of {@link org.dizitart.no2.collection.NitriteCollection} names to be exported.
      * <p>
      *
      * <b>Please see the rules for specifying the collections to be exported</b>
      * <ul>
      *     <li>If null is specified, all collections will be exported</li>
      *     <li>If an empty list is specified, no collection will be exported</li>
      *     <li>If a non-empty list is specified, only the collections in the list will be exported</li>
      * </ul>
      *
      * @param collections list of all collection names to be exported.
      * @return list of collection names.
      */
     private List<String> collections;
 
     /**
      * Specifies a list of {@link org.dizitart.no2.repository.ObjectRepository} names to be exported.
      * <p>
      * <b>Please see the rules for specifying the repositories to be exported</b>
      * <ul>
      *     <li>If null is specified, all repositories will be exported</li>
      *     <li>If an empty list is specified, no repositories will be exported</li>
      *     <li>If a non-empty list is specified, only the repositories in the list will be exported</li>
      * </ul>
      *
      * @param repositories list of all repositories names to be exported.
      * @return list of repositories names.
      */
     private List<String> repositories;
 
     /**
      * Specifies a list of keyed {@link org.dizitart.no2.repository.ObjectRepository} names to be exported.
      * <p>
      * <b>Please see the rules for specifying the keyed-repositories to be exported</b>
      * <ul>
      *     <li>If null is specified, all keyed-repositories will be exported</li>
      *     <li>If an empty map is specified, no keyed-repositories will be exported</li>
      *     <li>If a non-empty map is specified, only the keyed-repositories in the map will be exported</li>
      * </ul>
      *
      * @param keyedRepositories list of all keyed repositories names to be exported.
      * @return list of keyed repositories names.
      */
     private Map<String, Set<String>> keyedRepositories;
 }
 