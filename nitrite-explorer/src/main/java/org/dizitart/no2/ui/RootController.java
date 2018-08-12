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

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;

import java.io.Closeable;
import java.io.IOException;

import static org.dizitart.no2.ui.ErrorDialog.showErrorDialog;

/**
 * @author Anindya Chatterjee.
 */
public class RootController {
    private Closeable closeHandler;

    @FXML
    private Slider brightness;

    private BorderPane rootLayout;

    public void exit(ActionEvent actionEvent) {
        try {
            closeHandler.close();
        } catch (IOException e) {
            showErrorDialog(e);
        } finally {
            Platform.exit();
        }
    }

    public void about(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Nitrite Explorer");
        alert.setContentText("A readonly viewer for Nitrite Database.");

        alert.showAndWait();
    }

    void closeHandler(Closeable closeHandler) {
        this.closeHandler = closeHandler;
    }

    void setRootLayout(BorderPane rootLayout) {
        this.rootLayout = rootLayout;
        brightness.valueProperty().addListener((observable, oldValue, newValue) -> {
            Double value = (Double) newValue;
            value = value * 2.55;
            StringExpression styleString = Bindings.format("-fx-base:rgb(%1$.0f , %1$.0f, %1$.0f)", value);
            this.rootLayout.styleProperty().bind(styleString);
        });
        brightness.setMin(0);
        brightness.setMax(100);
        brightness.setValue(30.8);
    }
}
