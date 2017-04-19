package org.dizitart.no2.event;

import java.util.Collection;

/**
 * @author Anindya Chatterjee.
 */
class SampleListener implements ChangeListener {
    private ChangeType action;
    private Collection<ChangedItem> items;

    ChangeType getAction() {
        return action;
    }

    Collection<ChangedItem> getItems() {
        return items;
    }

    @Override
    public void onChange(ChangeInfo changeInfo) {
        this.action = changeInfo.getChangeType();
        this.items = changeInfo.getChangedItems();
    }
}
