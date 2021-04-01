package org.dizitart.no2.transaction;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Data
class UndoEntry {
    private String collectionName;
    private Command rollback;
}
