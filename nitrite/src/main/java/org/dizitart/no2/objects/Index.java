package org.dizitart.no2.objects;

import org.dizitart.no2.IndexType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a field to be indexed.
 *
 * [[app-listing]]
 * [source,java]
 * .Example of Index annotation
 * --
 *  @Index(field = "companyName")
 *  public class Company implements Serializable {
 *
 *      @Id
 *      private long companyId;
 *
 *      private String companyName;
 *
 *  }
 * --
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 * @see ObjectRepository#createIndex(String, org.dizitart.no2.IndexOptions)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Index {
    /**
     * The field name to be indexed.
     *
     * @return the field name
     */
    String value();

    /**
     * Type of the index.
     *
     * @return the index type
     */
    IndexType type() default IndexType.Unique;
}
