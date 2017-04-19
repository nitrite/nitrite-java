package org.dizitart.no2.filters;

import lombok.ToString;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.store.NitriteMap;

import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorCodes.IE_TEXT_FILTER_FIELD_NOT_INDEXED;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;

@ToString
class TextFilter extends StringFilter {
    TextFilter(String field, String value) {
        super(field, value);
    }

    @Override
    public Set<NitriteId> apply(NitriteMap<NitriteId, Document> documentMap) {
        if (nitriteService.hasIndex(field)
                && !nitriteService.isIndexing(field)
                && !isNullOrEmpty(value)) {
            return nitriteService.findTextWithIndex(field, value);
        } else {
            throw new IndexingException(errorMessage(field + " is not indexed",
                    IE_TEXT_FILTER_FIELD_NOT_INDEXED));
        }
    }
}
