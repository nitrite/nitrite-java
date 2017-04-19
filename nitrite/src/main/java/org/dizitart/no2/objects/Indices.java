package org.dizitart.no2.objects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies multiple indexed fields for a class.
 * 
 * [[app-listing]]
 * [source,java]
 * .Example of Indices annotation
 * --
 *
 * @Indices({
 *      @Index(field = "joinDate", type = IndexType.NonUnique),
 *      @Index(field = "address", type = IndexType.Fulltext)
 * })
 * public class Employee implements Serializable {
 *
 *      @Id private long empId;
 *      private Date joinDate;
 *      private String address;
 * }
 *
 * --
 *
 * @author Anindya Chatterjee.
 * @see Index
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Indices {
    /**
     * Returns an array of {@link Index}.
     *
     * @return the array of {@link Index}.
     */
    Index[] value();
}
