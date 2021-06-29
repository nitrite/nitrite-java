package org.dizitart.no2.migration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a migration step.
 *
 * @author Anindya Chatterjee
 * @since 4.0
 */
@Getter(AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class MigrationStep {
    private InstructionType instructionType;
    private Object arguments;
}
