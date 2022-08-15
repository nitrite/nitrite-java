package org.dizitart.no2.repository;

import static org.junit.Assert.*;

import java.io.Serializable;

import javax.validation.constraints.AssertTrue;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.exceptions.InvalidIdException;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.exceptions.ValidationException;
import org.dizitart.no2.repository.annotations.Entity;
import org.dizitart.no2.repository.annotations.Id;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DefaultRepositoryFinderTest {

	private static Nitrite db;

	@BeforeClass
	public static void init() {
		db = Nitrite.builder().openOrCreate();

	}


	@Test
	public void testFindRepoWhenClosed() {

		ObjectRepository<Item> repo = db.repository(Item.class).get();
		assertTrue(repo.isOpen());

		repo.close();

		assertFalse(repo.isOpen());

		ObjectRepository<Item> newRepo = db.repository(Item.class).get();

		assertNotSame(repo, newRepo);

		newRepo.close();

		assertFalse(newRepo.isOpen());
	}

	@Test
	public void testFindNotIdentifiableRepo() { 
		
		ObjectRepository<NotIdentifiableItem> repo = db.repository(NotIdentifiableItem.class).get() ;
		
		assertTrue(repo.isOpen());
		assertThrows(NotIdentifiableException.class, ()->repo.getById(1)) ; 
		
	}
	
	@Test
	public void testFindRepoByClient() {

		ObjectRepository<Item> repoByClient = db.repository(Item.class).withTypeId("id").get();

		assertTrue(repoByClient.isOpen());
		repoByClient.close();
	}
 

	@Test
	public void testFindRepoByClientWithKey() {

		ObjectRepository<Item> item = db.repository(Item.class).hasKey("test2").withTypeId("id").get();
		ObjectRepository<Item> itemRepo2 = db.repository(Item.class).hasKey("theKey").withTypeId("id").get() ; 
		ObjectRepository<Item> itemRepo3 = db.repository(Item.class).get() ; 
		ObjectRepository<Item> itemRepo4 = db.repository(Item.class).hasKey("theKey").get() ;  
				
		assertTrue(item.isOpen());
		assertTrue(itemRepo2.isOpen());
		assertTrue(itemRepo3.isOpen());
		assertTrue(itemRepo4.isOpen()) ; 
		
		assertNotSame(item, itemRepo2 );
		assertNotSame(itemRepo2 , itemRepo3) ; 
		assertNotSame(item , itemRepo3) ; 
		assertSame(itemRepo2 , itemRepo4) ; 
		
		itemRepo2.close();
		itemRepo3.close();
		item.close();

	}


	@Test
	public void testFindRepoByClientWithKeyAndInvalidID() {

		ObjectRepository<Item> item = db.repository(Item.class).hasKey("theKey").withTypeId("id").get();

		assertTrue(item.isOpen());
		
 
		assertThrows(InvalidIdException.class, ()->db.repository(Item.class).hasKey("theKey").withTypeId("itemName").get()) ; 
		
		item.close();

	}


	
	@Test
	public void testFindRepoByClientWithInvalidID() { 
		
		
		assertThrows(InvalidIdException.class,()->db.repository(Item.class).withTypeId("invalidId").get() ) ; 
	}


	
	@Test
	public void testFindRepoByConvenient() {

		ObjectRepository<Item> repoByConvenient = db.repository(Item.class).get();


		assertTrue(repoByConvenient.isOpen());

		repoByConvenient.close();
		assertFalse(repoByConvenient.isOpen());
	}

	@Test
	public void testFindRepoByConvWithKey() { 
	 
		ObjectRepository<Item> repoByConv = db.repository(Item.class).get() ; 
		ObjectRepository<Item> repoByConv2 = db.repository(Item.class).hasKey("theKey").get() ; 
		ObjectRepository<Item> repoByConv3 = db.repository(Item.class).hasKey("theKey2").get() ; 
		
		assertTrue(repoByConv.isOpen()) ; 
		assertTrue(repoByConv2.isOpen()) ; 
		assertTrue(repoByConv3.isOpen()) ; 
		
		assertNotSame(repoByConv , repoByConv2) ; 
		assertNotSame(repoByConv2 , repoByConv3) ; 
		assertNotSame(repoByConv , repoByConv3) ; 
		
		repoByConv.close(); 
		repoByConv2.close();
		repoByConv3.close();
		
		
	}

	@Test
	public void testFindRepoByConvWithKeyAndInvaliId() { 
		ObjectRepository<Item> repoByConv = db.repository(Item.class).hasKey("theKey").get() ; 
		
		assertTrue(repoByConv.isOpen());
		
		assertThrows(InvalidIdException.class , ()->db.repository(Item.class).hasKey("theKey").withTypeId("itemName").get()) ; 
		
		repoByConv.close();
		
	}
	
	@Test
	public void testFindRepoByAnnotation() {

		ObjectRepository<AnnotatedItem> itemRepo = db.repository(AnnotatedItem.class).get();
		assertTrue(itemRepo.isOpen());
		itemRepo.close();
	}
	
	
	@Test
	public void testFindRepoByAnnoWithKey( ) { 
 
		ObjectRepository<AnnotatedItem> annoRepo = db.repository(AnnotatedItem.class).get() ; 
		ObjectRepository<AnnotatedItem> annoRepo2 = db.repository(AnnotatedItem.class).hasKey("theKey").get() ; 
		ObjectRepository<AnnotatedItem> annoRepo3 = db.repository(AnnotatedItem.class).hasKey("theKey2").get() ; 
		
		assertTrue(annoRepo.isOpen());
		assertTrue(annoRepo2.isOpen());
		assertTrue(annoRepo3.isOpen());
		
		
		assertNotSame(annoRepo, annoRepo2);
		assertNotSame(annoRepo2, annoRepo3);
		assertNotSame(annoRepo, annoRepo3);
		
		
		annoRepo.close();
		annoRepo2.close();
		annoRepo3.close();
	}
	
	@Test
	public void testFindRepoByAnnoWithKeyAndInvalidId() { 
		ObjectRepository<AnnotatedItem> annoRepo = db.repository(AnnotatedItem.class).hasKey("theKey").get() ; 
		
		assertTrue(annoRepo.isOpen()) ; 
		
		
		assertThrows(InvalidIdException.class,()->db.repository(AnnotatedItem.class).hasKey("theKey").withTypeId("itemName").get()) ; 
		annoRepo.close();
	}

	@Test
	public void testFindRepoWithInvalidAnnotations() { 
		
		ObjectRepository<InvalidAnnotatedItem> itemRepo = db.repository(InvalidAnnotatedItem.class).withTypeId("id").get() ; 
		assertTrue(itemRepo.isOpen()) ; 
		
		assertThrows(NotIdentifiableException.class, ()->db.repository(InvalidAnnotatedItem.class).get()) ; 
		itemRepo.close(); 
	}
	
	@Test
	public void testFindRepoByClientWhenCachedByConv() {

		ObjectRepository<Item> itemRepoByConv = db.repository(Item.class).get();


		assertTrue(itemRepoByConv.isOpen());

		assertSame(itemRepoByConv, db.repository(Item.class).withTypeId("id").get());

		assertThrows(InvalidIdException.class, () -> {
			db.repository(Item.class).withTypeId("itemName").get();

		});
		itemRepoByConv.close();
	}

	@Test
	public void testFindRepoByClientWhenCachedByClient() {
		ObjectRepository<Item> itemRepoByClient = db.repository(Item.class).withTypeId("id").get();

		assertTrue(itemRepoByClient.isOpen());
		assertTrue(itemRepoByClient.getAttributes().get(RepositoryAttributes.ID_FIELD_NAME).equals("id"));

		assertSame(itemRepoByClient, db.repository(Item.class).withTypeId("id").get());

		assertThrows(InvalidIdException.class, () -> {

			db.repository(Item.class).withTypeId("itemName").get();

		});

		itemRepoByClient.close();

	}

	@Test
	public void testFindRepoByClientWhereCashedByAnnotation() {

		ObjectRepository<AnnotatedItem> itemRepoByClient = db.repository(AnnotatedItem.class).get();
		assertTrue(itemRepoByClient.isOpen());

		assertSame(itemRepoByClient, db.repository(AnnotatedItem.class).withTypeId("id").get());
		assertThrows(InvalidIdException.class, () -> {
			db.repository(AnnotatedItem.class).withTypeId("itemName").get();
		});

	}

	@Test
	public void testFindRepoByConvWhereCachedByConv() {
		ObjectRepository<Item> itemRepoByConv = db.repository(Item.class).get();
		assertTrue(itemRepoByConv.isOpen());

		assertSame(itemRepoByConv, db.repository(Item.class).get());
		itemRepoByConv.close();
	}

	@Test
	public void testFindRepoByConvWhereCashedByClient() {

		ObjectRepository<Item> itemRepoByClient = db.repository(Item.class).withTypeId("itemName").get();
		assertTrue(itemRepoByClient.isOpen());
		assertTrue(itemRepoByClient.getAttributes().get(RepositoryAttributes.ID_FIELD_NAME).equals("itemName"));
		//assertTrue(itemRepoByClient.getAttributes().get(RepositoryAttributes.ID_RECOGNITION_STRATEGY)
			//	.equals(IdRecognitionStrategy.BY_CLIENT.toString()));

		assertThrows(InvalidIdException.class, () -> {
			db.repository(Item.class).get();
		});

		itemRepoByClient.close();
	}

	@Test
	public void testFindRepoByConvWhereCashedByClient2() {

		ObjectRepository<Item> repoByClient = db.repository(Item.class).withTypeId("id").get();

		assertTrue(repoByClient.isOpen());
		assertTrue(repoByClient.getAttributes().get(RepositoryAttributes.ID_FIELD_NAME).equals("id"));

		assertSame(repoByClient, db.repository(Item.class).get());
		repoByClient.close();
	}
 
	@Test
	public void testFindRepoByAnnoWhenCachedByAnno() { 
		ObjectRepository<AnnotatedItem> repoByAnno = db.repository(AnnotatedItem.class).get() ; 
		assertSame(repoByAnno, db.repository(AnnotatedItem.class).get());
		repoByAnno.close();
		
	}
	@Test
	public void testFindRepoByAnnoWhenCachedByClient() { 
		
		ObjectRepository<AnnotatedItem> repoByClient = db.repository(AnnotatedItem.class).withTypeId("id").get() ; 
		
		assertSame(repoByClient, db.repository(AnnotatedItem.class).get());
		repoByClient.close();
		
		
	}

	@Test
	public void testFindRepoByAnnoWhenCachedByClient2() { 
		ObjectRepository<AnnotatedItem> repoByClient  = db.repository(AnnotatedItem.class).withTypeId("itemName").get() ; 
		
		assertTrue(repoByClient.isOpen());
		assertThrows(InvalidIdException.class, ()->{
			db.repository(AnnotatedItem.class).get() ; 
		});
		repoByClient.close();
		
	}
	
	
	


	@Test
	public void testCachedRepoWithKey() {

		ObjectRepository<Item> repoByImplictId = db.repository(Item.class).hasKey("test").get();


		assertTrue(repoByImplictId.isOpen());

		ObjectRepository<Item> repoByClient = db.repository(Item.class).hasKey("test").withTypeId("id").get();


		assertSame(repoByImplictId, repoByClient);
		repoByImplictId.close();

	}





	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	private static class AnnotatedItem implements Serializable {

		@Id
		private Integer id;

		private String itemName;
		private String repoKey;

	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	private static class Item implements Serializable {

		private Integer id;

		private String itemName;
		private String repoKey;


	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	private static class NotIdentifiableItem implements Serializable { 
		
		private Integer theId ; 
		private String itemName ; 
		private String repoKey ; 
		
	}
	
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Entity
	private static class InvalidAnnotatedItem implements Serializable { 
		
		@Id
		private Integer id ; 
		
		@Id
		private String itemName ; 
		
		private String repoKey ; 
	}
}
