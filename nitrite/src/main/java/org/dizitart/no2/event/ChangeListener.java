package org.dizitart.no2.event;

/**
 * An interface when implemented makes an object be
 * able to listen to any changes in a {@link org.dizitart.no2.NitriteCollection}
 * or {@link org.dizitart.no2.objects.ObjectRepository}.
 *
 * [[app-listing]]
 * [source,java]
 * .Example
 * --
 *
 *  // observe any change to the collection
 *  collection.register(new ChangeListener() {
 *
 *      @Override
 *      public void onChange(ChangeInfo changeInfo) {
 *          System.out.println("Action - " + changeInfo.getChangeType());
 *
 *          System.out.println("List of affected ids:");
 *          for (NitriteId id : changeInfo.getChangedItems()) {
 *              System.out.println("Id - " + id);
 *          }
 *      }
 *  });
 *
 * --
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 */
public interface ChangeListener {

    /**
     * Listener routine to be invoked for each change event.
     *
     * @param changeInfo the change information
     */
    void onChange(ChangeInfo changeInfo);
}
