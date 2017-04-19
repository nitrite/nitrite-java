package org.dizitart.no2;


/**
 * An interface to iterate over database {@code find()} results. It provides a
 * mechanism to iterate over all {@link NitriteId}s of the result.
 *
 * [[app-listing]]
 * [source,java]
 * . Example of {@link Cursor}
 * --
 *  // createId/open a database
 *  Nitrite db = Nitrite.builder()
 *         .openOrCreate("user", "password");
 *
 *  // createId a collection named - test
 *  NitriteCollection collection = db.getCollection("test");
 *
 *  // returns all ids un-filtered
 *  Cursor result = collection.find();
 *
 *  for (Document doc : result) {
 *      // use your logic with the retrieved doc here
 *  }
 *
 *
 * --
 *
 * [icon="{@docRoot}/note.png"]
 * NOTE: To createId an iterator over the documents instead of the ids,
 * call on the {@link Cursor}.
 *
 *  @author Anindya Chatterjee
 *  @since 1.0
 */
public interface Cursor extends RecordIterable<Document> {

    /**
     * Gets a lazy iterable containing all the selected keys of the result documents.
     *
     * @param projection the selected keys of a result document.
     * @return a lazy iterable of documents.
     */
    RecordIterable<Document> project(Document projection);
}
