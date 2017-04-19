package org.dizitart.no2.tool;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents export options.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 * @see Exporter
 */
@Getter @Setter
public class ExportOptions {

    /**
     * Indicates if the export operation exports indices information.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: Default value is `true`.
     *
     * @param exportIndices a value indicating if indices information will be exported.
     * @return `true` if indices information is exported; otherwise, `false`.
     * */
    private boolean exportIndices = true;

    /**
     * Indicates if the export operation exports collection data.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: Default value is `true`.
     *
     * @param exportData a value indicating if collection data will be exported.
     * @return `true` if collection data is exported; otherwise, `false`.
     * */
    private boolean exportData = true;

    /**
     * Specifies a list of {@link PersistentCollection}s to be exported.
     *
     * [icon="{@docRoot}/note.png"]
     * NOTE: If empty, all collections will be exported.
     *
     * @param collections list of all collections to be exported.
     * @return list of collections.
     * */
    private List<PersistentCollection<?>> collections = new ArrayList<>();
}
