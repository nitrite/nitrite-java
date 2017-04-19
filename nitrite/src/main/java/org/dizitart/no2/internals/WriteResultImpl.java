package org.dizitart.no2.internals;

import lombok.ToString;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.WriteResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Anindya Chatterjee.
 */
@ToString
class WriteResultImpl implements WriteResult {
    private List<NitriteId> nitriteIdList;

    void setNitriteIdList(List<NitriteId> nitriteIdList) {
        this.nitriteIdList = nitriteIdList;
    }

    void addToList(NitriteId nitriteId) {
        if (nitriteIdList == null) {
            nitriteIdList = new ArrayList<>();
        }
        nitriteIdList.add(nitriteId);
    }

    public int getAffectedCount() {
        if (nitriteIdList == null) return 0;
        return nitriteIdList.size();
    }

    @Override
    public Iterator<NitriteId> iterator() {
        return nitriteIdList.iterator();
    }
}
