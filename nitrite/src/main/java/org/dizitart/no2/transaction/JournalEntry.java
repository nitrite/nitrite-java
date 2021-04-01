package org.dizitart.no2.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a transaction journal entry.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class JournalEntry {
    private ChangeType changeType;
    private Command commit;
    private Command rollback;
}
