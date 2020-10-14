package org.dizitart.no2.collection.operation;

import lombok.Data;
import lombok.experimental.Accessors;
import org.dizitart.no2.common.Fields;
import org.dizitart.no2.common.NullOrder;

import java.text.Collator;

/**
 * @author Anindya Chatterjee
 */
@Data
@Accessors(fluent = true, chain = true)
public class FindOptions {
    private Fields sortBy;
    private Collator collator;
    private NullOrder nullOrder;
    private long skip;
    private long limit;
}
