package org.dizitart.no2.objects.data;

import lombok.Data;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.objects.Id;

/**
 * @author Anindya Chatterjee
 */
@Data
public class WithNitriteId {
    @Id
    public NitriteId idField;
    public String name;
}
