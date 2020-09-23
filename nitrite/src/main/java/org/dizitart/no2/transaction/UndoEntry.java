package org.dizitart.no2.transaction;

import lombok.Data;

/**
 * @author Anindya Chatterjee
 */
@Data
class UndoEntry {
    private String collectionName;
    private Command rollback;
}
