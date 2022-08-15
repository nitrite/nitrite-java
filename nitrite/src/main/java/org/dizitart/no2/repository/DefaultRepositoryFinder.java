package org.dizitart.no2.repository;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.dizitart.no2.NitriteConfig;
import org.dizitart.no2.common.meta.Attributes;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.repository.annotations.Id;

public class DefaultRepositoryFinder<T> implements IRepositoryFinder<T> {

	private final Class<T> type ; 
	private String idField = null;
	private String key = null;

	private RepositoryFactory repositoryFactory ; 
	private NitriteConfig nitriteConfig ; 
    
	
	public DefaultRepositoryFinder(NitriteConfig nitriteConfig ,Class<T> type , RepositoryFactory factory) { 
		this.nitriteConfig = nitriteConfig ; 
		this.type = type ; 
		this.repositoryFactory = factory ; 
	}
	
	
	@Override
	public IRepositoryFinder<T> withTypeId(String idField) {
		this.idField = idField;
		return this;
	}

	@Override
	public IRepositoryFinder<T> hasKey(String key) {
		this.key = key;
		return this;
	}
	

	@Override
	public ObjectRepository<T> get() {
	 
		Attributes attrs = new Attributes();
			
//		if(idField == null) { 
//			
//			Field[] typeFields = type.getDeclaredFields() ; 
//			
//			for(Field field : typeFields) { 
//				
//				if(field.isAnnotationPresent(Id.class)) { 
//					idField = "" ; 
//					break ; 
//				}
//			
//				// implicit id is the field with name 'id' , so if model type defines any field
//				// with name 'id' then we can consider
//				// this field as ObjectIdField even if is`t annotated with @id or declared
//				// manually by RepositoryFinder
//
//				if(field.getName().equals("id")) { 
//					idField = "id" ; 
//					attrs.set(RepositoryAttributes.ID_RECOGNITION_STRATEGY, IdRecognitionStrategy.BY_CONVENIENT.toString()) ; 
//				}
//				
//			}
//			
//		}else { 
//			System.out.println("Set ID Reco Strategy To " + IdRecognitionStrategy.BY_CLIENT.toString()) ; 
// 			attrs.set(RepositoryAttributes.ID_RECOGNITION_STRATEGY, IdRecognitionStrategy.BY_CLIENT.toString()) ; 
//			
//		}
//		
		attrs.set(RepositoryAttributes.ID_FIELD_NAME, idField == null ? "" : idField ) ; 
		
		return this.repositoryFactory.getRepository(nitriteConfig, type, key , attrs) ; 
	}

	
}
