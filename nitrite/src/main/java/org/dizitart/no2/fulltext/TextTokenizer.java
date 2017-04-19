package org.dizitart.no2.fulltext;

import java.io.IOException;
import java.util.Set;

/**
 * A stop-word based string tokenizer.
 *
 * @since 1.0
 * @author Anindya Chatterjee.
 * @see TextIndexingService
 * @see EnglishTextTokenizer
 * @see org.dizitart.no2.NitriteBuilder#textTokenizer(TextTokenizer)
 */
public interface TextTokenizer {
    /**
     * Tokenize a `text` and discards all stop-words from it.
     *
     * @param text the text to tokenize
     * @return the set of tokens.
     * @throws IOException if a low-level I/O error occurs.
     */
    Set<String> tokenize(String text) throws IOException;

    /**
     * Gets all stop-words for a language.
     *
     * @return the set of all stop-words.
     */
    Set<String> stopWords();
}
