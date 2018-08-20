/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.services;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.exceptions.IndexingException;
import org.dizitart.no2.index.TextIndexer;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.StringUtils.isNullOrEmpty;

public class LuceneService implements TextIndexer {
    private static final String CONTENT_ID = "content_id";
    private static final int MAX_SEARCH = Byte.MAX_VALUE;

    private IndexWriter indexWriter;
    private ObjectMapper keySerializer;
    private Analyzer analyzer;
    private Directory indexDirectory;

    public LuceneService() {
        try {
            this.keySerializer = new ObjectMapper();
            keySerializer.setVisibility(keySerializer
                .getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));

            indexDirectory = new RAMDirectory();
            analyzer = new StandardAnalyzer();

            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            indexWriter = new IndexWriter(indexDirectory, iwc);
            commit();
        } catch (IOException e) {
            throw new IndexingException(errorMessage("could not create full-text index", 0), e);
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
    }

    @Override
    public void writeIndex(NitriteId id, String field, String text, boolean unique) {
        try {
            Document document = new Document();
            String jsonId = keySerializer.writeValueAsString(id);
            Field contentField = new TextField(field, text, Field.Store.NO);
            Field idField = new StringField(CONTENT_ID, jsonId, Field.Store.YES);

            document.add(idField);
            document.add(contentField);

            synchronized (this) {
                indexWriter.addDocument(document);
                commit();
            }
        } catch (IOException ioe) {
            throw new IndexingException(errorMessage("could not write full-text index data for " + text, 0), ioe);
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
    }

    @Override
    public void updateIndex(NitriteId id, String field, String text, String oldText, boolean unique) {
        try {
            String jsonId = keySerializer.writeValueAsString(id);
            Document document = getDocument(jsonId);
            if (document == null) {
                document = new Document();
                Field idField = new StringField(CONTENT_ID, jsonId, Field.Store.YES);
                document.add(idField);
            }
            Field contentField = new TextField(field, text, Field.Store.YES);

            document.add(contentField);

            synchronized (this) {
                indexWriter.updateDocument(new Term(CONTENT_ID, jsonId), document);
                commit();
            }
        } catch (IOException ioe) {
            throw new IndexingException(errorMessage("could not update full-text index for " + text, 0), ioe);
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
    }

    @Override
    public void removeIndex(NitriteId id, String field, String text) {
        try {
            String jsonId = keySerializer.writeValueAsString(id);
            Term idTerm = new Term(CONTENT_ID, jsonId);

            synchronized (this) {
                indexWriter.deleteDocuments(idTerm);
                commit();
            }
        } catch (IOException ioe) {
            throw new IndexingException(errorMessage("could not remove full-text index for " + id, 0));
        } catch (VirtualMachineError vme) {
            handleVirtualMachineError(vme);
        }
    }

    @Override
    public void dropIndex(String field) {
        if (!isNullOrEmpty(field)) {
            try {
                Query query;
                QueryParser parser = new QueryParser(field, analyzer);
                parser.setAllowLeadingWildcard(true);
                try {
                    query = parser.parse("*");
                } catch (ParseException e) {
                    throw new IndexingException(errorMessage("could not remove full-text index for value " + field, 0));
                }

                synchronized (this) {
                    indexWriter.deleteDocuments(query);
                    commit();
                }
            } catch (IOException ioe) {
                throw new IndexingException(errorMessage("could not remove full-text index for value " + field, 0));
            } catch (VirtualMachineError vme) {
                handleVirtualMachineError(vme);
            }
        }
    }

    @Override
    public void rebuildIndex(String field, boolean unique) {
        // nothing to be done
    }

    @Override
    public Set<NitriteId> findText(String field, String searchString) {
        IndexReader indexReader = null;
        try {
            QueryParser parser = new QueryParser(field, analyzer);
            parser.setAllowLeadingWildcard(true);
            Query query = parser.parse("*" + searchString + "*");

            indexReader = DirectoryReader.open(indexDirectory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            TopScoreDocCollector collector = TopScoreDocCollector.create(MAX_SEARCH);
            indexSearcher.search(query, collector);

            TopDocs hits = collector.topDocs(0, MAX_SEARCH);

            Set<NitriteId> keySet = new LinkedHashSet<>();
            if (hits != null) {
                ScoreDoc[] scoreDocs = hits.scoreDocs;
                if (scoreDocs != null) {
                    for (ScoreDoc scoreDoc : scoreDocs) {
                        Document document = indexSearcher.doc(scoreDoc.doc);
                        String jsonId = document.get(CONTENT_ID);
                        NitriteId nitriteId = keySerializer.readValue(jsonId, NitriteId.class);
                        keySet.add(nitriteId);
                    }
                }
            }

            return keySet;
        } catch (IOException | ParseException e) {
            throw new IndexingException(errorMessage("could not search on full-text index", 0), e);
        } finally {
            try {
                if (indexReader != null)
                    indexReader.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
    }

    private Document getDocument(String jsonId) {
        IndexReader indexReader = null;
        try {
            Term idTerm = new Term(CONTENT_ID, jsonId);

            TermQuery query = new TermQuery(idTerm);

            indexReader = DirectoryReader.open(indexDirectory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);

            TopScoreDocCollector collector = TopScoreDocCollector.create(MAX_SEARCH);
            indexSearcher.search(query, collector);

            TopDocs hits = collector.topDocs(0, MAX_SEARCH);
            Document document = null;
            if (hits != null) {
                ScoreDoc[] scoreDocs = hits.scoreDocs;
                if (scoreDocs != null) {
                    for (ScoreDoc scoreDoc : scoreDocs) {
                        document = indexSearcher.doc(scoreDoc.doc);
                    }
                }
            }

            return document;
        } catch (IOException e) {
            throw new IndexingException(errorMessage("could not search on full-text index", 0), e);
        } finally {
            try {
                if (indexReader != null)
                    indexReader.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
    }

    private void handleVirtualMachineError(VirtualMachineError vme) {
        if (indexWriter != null) {
            try {
                indexWriter.close();
            } catch (IOException ioe) {
                // ignore it
            }
        }
        throw vme;
    }

    private synchronized void commit() throws IOException {
        indexWriter.commit();
    }
}
