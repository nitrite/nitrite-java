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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Data;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.objects.ObjectRepository;
import org.dizitart.no2.ui.data.Company;
import org.dizitart.no2.ui.data.DataGenerator;
import org.dizitart.no2.ui.data.Employee;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.dizitart.no2.ui.ErrorDialog.showErrorDialog;
import static org.dizitart.no2.ui.NitriteHelper.getIcon;

/**
 * @author Anindya Chatterjee.
 */
public class NitriteManager extends Application {
    private Stage primaryStage;
    private BorderPane rootLayout;
    private RootController rootController;
    private DbDetails dbDetails;
    private TextField dbFile;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        System.setProperty("prism.lcdtext", "false");
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Nitrite Explorer");
        this.primaryStage.getIcons().addAll(getIcon("app.png").getImage());
        showDbChooserDialog();
        showNitriteOverview();
    }

    private void showDbChooserDialog() {
        try {
            Dialog<DbDetails> dialog = new Dialog<>();
            dialog.setTitle("Db Chooser");
            dialog.setHeaderText("Choose Nitrite Database File");
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(getIcon("app.png").getImage());

            // Set the icon (must be included in the project).
            dialog.setGraphic(getIcon("adddb.png"));

            // Create the username and password labels and fields.
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            dbFile = new TextField();
            dbFile.setPromptText("Nitrite Database File");

            TextField username = new TextField();
            username.setPromptText("Username");

            PasswordField password = new PasswordField();
            password.setPromptText("Password");

            grid.add(new Label("Nitrite File:"), 0, 0);
            grid.add(dbFile, 1, 0);
            grid.add(new Label("Username:"), 0, 1);
            grid.add(username, 1, 1);
            grid.add(new Label("Password:"), 0, 2);
            grid.add(password, 1, 2);

            // Set the button types.
            ButtonType openButtonType = new ButtonType("Open", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(openButtonType, ButtonType.CANCEL);

            // Enable/Disable login button depending on whether a username was entered.
            Node openButton = dialog.getDialogPane().lookupButton(openButtonType);
            openButton.setDisable(true);

            Button browseButton = new Button("Browse");
            browseButton.setOnAction(actionEvent -> {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter =
                        new FileChooser.ExtensionFilter("Nitrite Db files (*.db)", "*.db");
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file.exists()) {
                    dbFile.setText(file.getAbsolutePath());
                }
            });
            grid.add(browseButton, 3, 0);

            Button demoDbButton = new Button("Sample Database");
            demoDbButton.setOnAction(actionEvent -> createSampleDb());
            grid.add(demoDbButton, 3, 1);

            // Do some validation (using the Java 8 lambda syntax).
            dbFile.textProperty().addListener((observable, oldValue, newValue)
                    -> openButton.setDisable(!new File(newValue).exists()));

            dialog.getDialogPane().setContent(grid);

            // Request focus on the username value by default.
            Platform.runLater(dbFile::requestFocus);

            // Convert the result to a username-password-pair when the login button is clicked.
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == openButtonType) {
                    DbDetails dbDetails = new DbDetails();
                    dbDetails.setFileName(dbFile.getText());
                    dbDetails.setUserName(username.getText());
                    dbDetails.setPassword(password.getText());
                    return dbDetails;
                }
                return null;
            });

            Optional<DbDetails> result = dialog.showAndWait();
            result.ifPresent(this::showNitriteManager);
        } catch (Throwable t) {
            showErrorDialog(t);
            Platform.exit();
        }
    }

    private void showNitriteManager(DbDetails dbDetails) {
        try {
            this.dbDetails = dbDetails;
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("RootLayout.fxml"));
            rootLayout = loader.load();
            rootController = loader.getController();
            rootController.setRootLayout(rootLayout);

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
//            setUserAgentStylesheet(NitriteHelper.class.getClassLoader().getResource("theme.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Throwable t) {
            showErrorDialog(t);
            Platform.exit();
        }
    }

    private void showNitriteOverview() {
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("NitriteOverview.fxml"));
            AnchorPane personOverview = loader.load();

            final NitriteOverviewController overViewController = loader.getController();
            if (rootController != null && overViewController != null) {
                rootController.closeHandler(overViewController::close);
                overViewController.init(dbDetails);
                rootLayout.setCenter(personOverview);
            }
        } catch (Throwable t) {
            showErrorDialog(t);
            Platform.exit();
        }
    }

    private void createSampleDb() {
        String dbFilePath = Paths.get("").toAbsolutePath().toString() + "/sample.db";
        if (!Files.exists(Paths.get(dbFilePath))) {
            Nitrite db = Nitrite.builder()
                    .filePath(dbFilePath)
                    .openOrCreate();

            ObjectRepository<Company> companyRepository = db.getRepository(Company.class);
            ObjectRepository<Employee> employeeRepository = db.getRepository(Employee.class);

            Employee[] dummy = new Employee[0];
            for (int i = 0; i < 10; i++) {
                Company company = DataGenerator.generateCompanyRecord();
                companyRepository.insert(company);

                for (List<Employee> employeeList : company.getEmployeeRecord().values()) {
                    if (employeeList != null && !employeeList.isEmpty()) {
                        employeeRepository.insert(employeeList.toArray(dummy));
                    }
                }
            }

            db.commit();
            db.close();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sample Database");
            alert.setHeaderText("Sample Database Already Exists");
            alert.getDialogPane().setContent(new Label(dbFilePath));

            alert.showAndWait();
        }
        dbFile.setText(dbFilePath);
    }

    @Data
    static class DbDetails {
        private String fileName;
        private String userName;
        private String password;
    }
}
