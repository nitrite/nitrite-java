package org.dizitart.no2.objects.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.objects.Id;

/**
 * @author Anindya Chatterjee.
 */
@EqualsAndHashCode
public class Note {
    @Id
    @Getter @Setter private Long noteId;

    @Getter @Setter private String text;
}
