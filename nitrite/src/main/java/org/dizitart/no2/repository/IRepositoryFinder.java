package org.dizitart.no2.repository;

public interface IRepositoryFinder<T> {

	
	public IRepositoryFinder<T> withTypeId(String idField) ; 
	public IRepositoryFinder<T> hasKey(String key) ; 
	public ObjectRepository<T> get() ; 
	
}
