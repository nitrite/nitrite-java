package org.dizitart.no2.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Anindya Chatterjee
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class JournalEntry {
    private ChangeType changeType;
    private Command commit;
    private Command rollback;
}
