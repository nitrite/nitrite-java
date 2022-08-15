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

package org.dizitart.no2.repository;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.collection.CollectionFactory;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.NitriteIOException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.store.NitriteStore;
import org.dizitart.no2.store.StoreCatalog;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.dizitart.no2.common.util.ObjectUtils.findRepositoryName;

/**
 * The {@link ObjectRepository} factory.
 *
 * @author Anindya Chatterjee
 * @since 1.0
 */
public class RepositoryFactory {
	private final Map<String, ObjectRepository<?>> repositoryMap;
	private final CollectionFactory collectionFactory;
	private final ReentrantLock lock;

	/**
	 * Instantiates a new {@link RepositoryFactory}.
	 *
	 * @param collectionFactory the collection factory
	 */
	public RepositoryFactory(CollectionFactory collectionFactory) {
		this.collectionFactory = collectionFactory;
		this.repositoryMap = new HashMap<>();
		this.lock = new ReentrantLock();
	}

	/**
	 * Gets an {@link ObjectRepository} by type.
	 *
	 * @param <T>           the type parameter
	 * @param nitriteConfig the nitrite config
	 * @param type          the type
	 * @return the repository
	 */
	public <T> ObjectRepository<T> getRepository(NitriteConfig nitriteConfig, Class<T> type) {
		return getRepository(nitriteConfig, type, null);
	}

	/**
	 * Gets an {@link ObjectRepository} by type and a key and specify the id field
	 * name.
	 * 
	 * this method allow you to specify the id field name for the passed model type
	 * 
	 * @param <T>           the type parameter
	 * @param nitriteConfig the nitrite config
	 * @param type          the type
	 * @param key           the key
	 * @param attrs         repository attributes , which can be used to control
	 *                      object repository behavior
	 * @throws InvalidIdException where id field name change after first creation of
	 *                            the repository .
	 * 
	 *                            <p>
	 *                            suppose the following scenario
	 * 
	 *                            <pre>
	 *                            class a {
	 * 
	 *                            	doSomeThing(){
	 * 
	 *                            	db.repository(Model.class)
	 *                            	.withKey("repo")
	 *                            	.withTypeId("model_id").get();
	 *                            	}
	 * 
	 *                            }
	 * 
	 *                            class b {
	 * 
	 *                            	doSomeThing(){
	 * 
	 *                            	db.repository(Model.class)
	 *                            	.withKey("repo")
	 *                            	.withTypeId("other_id").get();
	 * 
	 *                            	}
	 * 
	 *                            }
	 * 
	 *                            </pre>
	 *                            </p>
	 * 
	 * @return the repository
	 */
	public <T> ObjectRepository<T> getRepository(NitriteConfig nitriteConfig, Class<T> type, String key,
			Attributes attrs) {

		if (type == null) {
			throw new ValidationException("type cannot be null");
		}

		if (nitriteConfig == null) {
			throw new ValidationException("nitriteConfig cannot be null");
		}
		// if(attrs == null) {
		// attrs = new Attributes() ;
		// }

		String collectionName = findRepositoryName(type, key);

		try {
			lock.lock();
			if (repositoryMap.containsKey(collectionName)) {
				ObjectRepository<T> repository = (ObjectRepository<T>) repositoryMap.get(collectionName);
				if (repository.isDropped() || !repository.isOpen()) {
					repositoryMap.remove(collectionName);
					return createRepository(nitriteConfig, type, collectionName, key, attrs);
				} else {
					Attributes cachedRepoAttrs = repository.getAttributes();
					String idFieldName = (attrs != null) ? attrs.get(RepositoryAttributes.ID_FIELD_NAME) : "";
					String cachedIdFN = (cachedRepoAttrs != null)
							? cachedRepoAttrs.get(RepositoryAttributes.ID_FIELD_NAME)
							: "";

					boolean isValidId = true;

					if (StringUtils.isNullOrEmpty(idFieldName)) {

						if (!StringUtils.isNullOrEmpty(cachedIdFN)) {
							String typeId = getTypeId(type);
							isValidId = cachedIdFN.equals(typeId);
						}

					} else {

						if (StringUtils.isNullOrEmpty(cachedIdFN)) {
							String typeId = getTypeId(type);
							isValidId = idFieldName.equals(typeId);
						} else {
							isValidId = cachedIdFN.equals(idFieldName);
						}

					}

					if (!isValidId) {
						throw new InvalidIdException("Invalid id field name  { " + idFieldName
								+ " } id field name expected to be fixed during runtime  " + " , repository "
								+ collectionName + " already exists where id field name is { " + cachedIdFN + " } ");

					}

					return repository;
				}
			} else {
				System.out.println("Try To Get New Repo ");
				return createRepository(nitriteConfig, type, collectionName, key, attrs);
			}
		} finally {
			lock.unlock();
		}

	}

	private <T> String getTypeId(Class<T> type) {
		String fieldName = "";

		boolean idAnnoExists = false;
		for (Field f : type.getDeclaredFields()) {

			if (f.isAnnotationPresent(Id.class)) {
				if (!idAnnoExists) {
					fieldName = f.getName();
					idAnnoExists = true;
				} else {
					throw new NotIdentifiableException("Id annotation can not exists more than once");
				}
			}

			if (!idAnnoExists && f.getName().equals("id"))
				fieldName = "id";

		}

		return fieldName;
	}

	/**
	 * Gets an {@link ObjectRepository} by type and a key.
	 *
	 * @param <T>           the type parameter
	 * @param nitriteConfig the nitrite config
	 * @param type          the type
	 * @param key           the key
	 * @return the repository
	 */
	@SuppressWarnings("unchecked")
	public <T> ObjectRepository<T> getRepository(NitriteConfig nitriteConfig, Class<T> type, String key) {

		return getRepository(nitriteConfig, type, key, null);

//		if (type == null) {
//			throw new ValidationException("type cannot be null");
//		}
//
//		if (nitriteConfig == null) {
//			throw new ValidationException("nitriteConfig cannot be null");
//		}
//
//		String collectionName = findRepositoryName(type, key);
//
//		try {
//			lock.lock();
//			if (repositoryMap.containsKey(collectionName)) {
//				ObjectRepository<T> repository = (ObjectRepository<T>) repositoryMap.get(collectionName);
//				if (repository.isDropped() || !repository.isOpen()) {
//					repositoryMap.remove(collectionName);
//					return createRepository(nitriteConfig, type, collectionName, key);
//				} else {
//					return repository;
//				}
//			} else {
//				return createRepository(nitriteConfig, type, collectionName, key);
//			}
//		} finally {
//			lock.unlock();
//		}

	}

	/**
	 * Closes all opened {@link ObjectRepository}s and clear internal data from this
	 * class.
	 */
	public void clear() {
		try {
			lock.lock();
			for (ObjectRepository<?> repository : repositoryMap.values()) {
				repository.close();
			}
			repositoryMap.clear();
		} catch (Exception e) {
			throw new NitriteIOException("Failed to clear an object repository", e);
		} finally {
			lock.unlock();
		}
	}

	private <T> ObjectRepository<T> createRepository(NitriteConfig nitriteConfig, Class<T> type, String collectionName,
			String key, Attributes attrs) {

		NitriteMapper nitriteMapper = nitriteConfig.nitriteMapper();
		NitriteStore<?> store = nitriteConfig.getNitriteStore();
		if (nitriteMapper.isValueType(type)) {
			throw new ValidationException("Cannot create a repository for a value type");
		}

		if (store.getCollectionNames().contains(collectionName)) {
			throw new ValidationException("A collection with same entity name already exists");
		}

		NitriteCollection nitriteCollection = collectionFactory.getCollection(collectionName, nitriteConfig, false);

		if (attrs != null) {
			nitriteCollection.setAttributes(attrs);
		}
		ObjectRepository<T> repository = new DefaultObjectRepository<>(type, nitriteCollection, nitriteConfig);

		repositoryMap.put(collectionName, repository);

		writeCatalog(store, collectionName, key);
		return repository;
	}

	private void writeCatalog(NitriteStore<?> store, String name, String key) {
		StoreCatalog storeCatalog = store.getCatalog();
		if (StringUtils.isNullOrEmpty(key)) {
			storeCatalog.writeRepositoryEntry(name);
		} else {
			storeCatalog.writeKeyedRepositoryEntry(name);
		}
	}
}
