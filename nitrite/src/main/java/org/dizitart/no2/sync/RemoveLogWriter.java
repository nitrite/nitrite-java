package org.dizitart.no2.sync;

import lombok.Getter;
import lombok.Setter;
import org.dizitart.no2.Document;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.event.ChangeInfo;
import org.dizitart.no2.event.ChangeListener;
import org.dizitart.no2.event.ChangeType;
import org.dizitart.no2.event.ChangedItem;

import java.util.List;

import static org.dizitart.no2.Constants.*;
import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.util.Iterables.toList;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;

/**
 * @author Anindya Chatterjee
 */
class RemoveLogWriter implements ChangeListener {
    private final NitriteCollection changeLogRepository;

    @Getter @Setter
    private String collection;

    RemoveLogWriter(NitriteCollection changeLogRepository) {
        this.changeLogRepository = changeLogRepository;
    }

    @Override
    public void onChange(ChangeInfo changeInfo) {
        String threadName = changeInfo.getOriginatingThread();

        if (!isNullOrEmpty(threadName)
                && !threadName.contains(CollectionReplicator.class.getSimpleName())) {

            ChangeType action = changeInfo.getChangeType();
            Iterable<ChangedItem> changedItems = changeInfo.getChangedItems();

            if (action == ChangeType.REMOVE) {
                queueRemovedItems(toList(changedItems));
            }
        }
    }

    private void queueRemovedItems(List<ChangedItem> changedItems) {
        for (final ChangedItem item : changedItems) {
            Document logEntry = createDocument(COLLECTION, this.collection)
                    .put(DELETE_TIME, item.getChangeTimestamp())
                    .put(DELETED_ITEM, item.getDocument());
            changeLogRepository.insert(logEntry);
        }
    }
}
