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

package org.dizitart.no2.ui;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteBuilder;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.collection.Filter;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.store.NitriteMap;
import org.dizitart.no2.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.dizitart.no2.ui.ErrorDialog.showErrorDialog;
import static org.dizitart.no2.ui.NitriteHelper.getIcon;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteOverviewController {
    @FXML
    public TreeView<String> nitriteStore;
    @FXML
    public Pagination pagination;
    @FXML
    public TableView<NitriteKey> collectionView;
    @FXML
    public TextArea keyView;
    @FXML
    public TextArea valueView;
    @FXML
    public TextField searchKey;
    @FXML
    public TextField searchTerm;
    @FXML
    public SplitPane viewer;
    @FXML
    public GridPane searchBox;
    @FXML
    public ComboBox<String> searchType;

    private String dbName;
    private Nitrite db;
    private NitriteHelper nitriteHelper;
    private NitriteMap selectedMap;
    private boolean isCollection;
    private ObjectMapper objectMapper;

    private static final int ROW_PER_PAGE = 30;
    private int pageCount = 5;

    void init(NitriteManager.DbDetails dbDetails) {
        this.dbName = new File(dbDetails.getFileName()).getName();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        this.objectMapper.disable(MapperFeature.AUTO_DETECT_GETTERS);
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        NitriteBuilder nitriteBuilder = Nitrite.builder();
        nitriteBuilder.filePath(dbDetails.getFileName());
        nitriteBuilder.readOnly();
        if (StringUtils.isNullOrEmpty(dbDetails.getUserName())) {
            db = nitriteBuilder.openOrCreate();
        } else {
            db = nitriteBuilder.openOrCreate(dbDetails.getUserName(), dbDetails.getPassword());
        }

        nitriteHelper = new NitriteHelper(db);
        createTable();
        showDbDetails();

        searchType.getItems().addAll(
                "Equals",
                "Greater Than",
                "Greater Equal",
                "Lesser Than",
                "Lesser Equal",
                "Text");
        searchType.setValue("Equals");
    }

    void close() {
        if (db != null) {
            db.close();
        }
    }

    @SuppressWarnings("unchecked")
    private void createTable() {
        TableColumn<NitriteKey, String> keyColumn = new TableColumn<>("Id");
        keyColumn.prefWidthProperty().bind(collectionView.widthProperty().multiply(0.99));
        keyColumn.setCellValueFactory(param -> param.getValue().getKeyString());
        collectionView.getSelectionModel()
                .selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            try {
                if (newValue != null) {
                    Object key = newValue.getKey();
                    StringWriter writer = new StringWriter();
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, key);
                    keyView.setText(writer.toString());

                    Object value = selectedMap.get(key);
                    writer = new StringWriter();
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, value);
                    valueView.setText(writer.toString());
                }
            } catch (IOException e) {
                showErrorDialog(e);
            }

        });
        collectionView.getColumns().add(keyColumn);
    }

    private void showDbDetails() {
        TreeItem<String> rootItem = new TreeItem<>(dbName, getIcon("database.png"));
        rootItem.getChildren().addAll(nitriteHelper.getCollections());
        nitriteStore.setRoot(rootItem);

        nitriteStore.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> showMapDetails(newValue.getValue()));
    }

    private void showMapDetails(String mapName) {
        if (StringUtils.isNullOrEmpty(mapName)) {
            viewer.setVisible(false);
            return;
        }
        this.selectedMap = nitriteHelper.getMap(mapName);
        if (selectedMap != null && !selectedMap.isEmpty()) {
            viewer.setVisible(true);
            pageCount = getPageCount(selectedMap.size(), ROW_PER_PAGE);
            pagination.setPageCount(pageCount);
            pagination.setPageFactory(this::createPage);

            this.isCollection = nitriteHelper.isCollection(mapName);
            searchBox.setDisable(!isCollection);
        } else {
            viewer.setVisible(false);
            this.isCollection = false;
            searchBox.setDisable(true);
        }
    }

    private Node createPage(int pageIndex) {
        if (db != null && selectedMap != null) {
            List keyList = selectedMap.keyList();
            int totalSize = keyList.size();

            int fromIndex = pageIndex * ROW_PER_PAGE;
            int toIndex = Math.min(fromIndex + ROW_PER_PAGE, totalSize);
            List<NitriteKey> nitriteKeys = new ArrayList<>();
            List subList = keyList.subList(fromIndex, toIndex);
            for (Object id : subList) {
                NitriteKey nitriteKey = new NitriteKey(id, id.toString());
                nitriteKeys.add(nitriteKey);
            }
            collectionView.setItems(FXCollections.observableArrayList(nitriteKeys));
        }

        return new BorderPane(collectionView);
    }

    private int getPageCount(int totalCount, int itemsPerPage) {
        float floatCount = (float) totalCount / (float) itemsPerPage;
        int intCount = totalCount / itemsPerPage;
        return ((floatCount > intCount) ? ++intCount : intCount);
    }

    public void search(ActionEvent actionEvent) {
        if (isCollection) {
            try {
                String collectionName = selectedMap.getName();
                NitriteCollection collection = db.getCollection(collectionName);
                String key = searchKey.getText();
                String term = searchTerm.getText();
                String type = searchType.getSelectionModel().getSelectedItem();

                Filter filter = nitriteHelper.getFilter(key, term, type);
                final List<NitriteId> nitriteIds = nitriteHelper.getResultList(collection, filter);

                viewer.setVisible(true);
                keyView.setText("");
                valueView.setText("");

                pageCount = getPageCount(nitriteIds.size(), ROW_PER_PAGE);
                pagination.setPageCount(pageCount);
                pagination.setPageFactory(pageIndex -> {
                    int totalSize = nitriteIds.size();

                    int fromIndex = pageIndex * ROW_PER_PAGE;
                    int toIndex = Math.min(fromIndex + ROW_PER_PAGE, totalSize);
                    List<NitriteKey> nitriteKeys = new ArrayList<>();
                    List subList = nitriteIds.subList(fromIndex, toIndex);
                    for (Object id : subList) {
                        NitriteKey nitriteKey = new NitriteKey(id, id.toString());
                        nitriteKeys.add(nitriteKey);
                    }
                    collectionView.setItems(FXCollections.observableArrayList(nitriteKeys));
                    return new BorderPane(collectionView);
                });
                searchBox.setDisable(!isCollection);
            } catch (Exception e) {
                showErrorDialog(e);
            }
        }
    }
}
