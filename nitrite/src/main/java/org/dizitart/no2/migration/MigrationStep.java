package org.dizitart.no2.migration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A class representing a migration step in Nitrite database.
 * 
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class MigrationStep {
    /**
     * Returns the instruction type of the migration instruction.
     */
    private InstructionType instructionType;

    /**
     * Returns the arguments passed to the migration function.
     */    
    private Object arguments;
}
