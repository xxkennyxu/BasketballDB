import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.*;

/**
 * Created by Kenny on 5/4/2016.
 */
public class DBView implements Observer {
    private DBController controller;
    private Stage stage;
    private TableView criteriaTable;
    private TableView resultTable;

    // TODO: Where should this go?
    private final ObservableList<String> tableOptions =
            FXCollections.observableArrayList(
                    "COACHES",
                    "PLAYERS",
                    "PLAYER_SEASON",
                    "TEAMS",
                    "TEAM_SEASON"
            );

    public DBView(DBController controller, Stage stage) {
        this.controller = controller;
        this.stage = stage;
        init();
    }

    private void init() {
        BorderPane bp = new BorderPane();

        // Put it all together
        bp.setTop(createTabPane());

        this.stage.setScene(new Scene(bp));
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();

        // Query Tab
        Tab queryTab = new Tab("Query");
        queryTab.setClosable(false);
        BorderPane mainPane = new BorderPane();
        criteriaTable = createCriteriaTable();
        mainPane.setLeft(criteriaTable);
        resultTable = new TableView();
        mainPane.setCenter(resultTable);
        queryTab.setContent(mainPane);

        // Criteria Buttons
        FlowPane bottomPane = new FlowPane();
        Button queryCriteriaButton = new Button("Add Query Criteria");
        Button clearCriteriaButton = new Button("Clear Criteria(s)");
        // Table Selector Dropdown
        ComboBox tableDropdown = new ComboBox(tableOptions);
        tableDropdown.setMinWidth(100);
        // Results Buttons
        Button clearResultsButton = new Button("Clear Result(s)");
        clearResultsButton.setOnAction((event) -> {
            clearQueryResultsTable();
        });
        Button queryButton =  new Button("Query!");
        queryButton.setOnAction((event) -> {
            // TODO: Change method;
            this.controller.selectPlayers();

        });

        // margins
        bottomPane.setMargin(queryCriteriaButton, new Insets(10,5,10,10));
        bottomPane.setMargin(clearCriteriaButton, new Insets(10,5,10,10));
        bottomPane.setMargin(tableDropdown, new Insets(10,5,10,60));
        bottomPane.setMargin(clearResultsButton, new Insets(10, 10, 10, 150));
        bottomPane.setMargin(queryButton, new Insets(10, 10, 10, 5));

        // Add all to the pane
        bottomPane.getChildren().addAll(queryCriteriaButton, clearCriteriaButton, tableDropdown, clearResultsButton, queryButton);
        mainPane.setBottom(bottomPane);

        // ============================ UGLY SECTION START ===============================

        // Event Listener
        queryCriteriaButton.setOnAction((event) -> {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Criteria Dialog");
            dialog.setHeaderText("Please select a search criteria and specify a value");


            // Set the button types.
            ButtonType loginButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

            // Create the username and password labels and fields.
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            ComboBox choices;
            // TODO: ComoboBox decider
            switch( tableDropdown.getValue().toString()) {
                case "PLAYERS":
                    choices = new ComboBox(Player.ColHeaders);
                    break;
                default:
                    choices = new ComboBox();
            }

            TextField value = new TextField();
            value.setPromptText("Value");

            grid.add(new Label("Criteria:"), 0, 0);
            grid.add(choices, 1, 0);
            grid.add(new Label("Value:"), 0, 1);
            grid.add(value, 1, 1);

            // Enable/Disable login button depending on whether a username was entered.
            Node confirmButton = dialog.getDialogPane().lookupButton(loginButtonType);
            confirmButton.setDisable(true);

            // Do some validation (using the Java 8 lambda syntax).
            choices.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if(!value.getText().trim().isEmpty()) {
                    confirmButton.setDisable(newValue == null);
                }
            });

            value.textProperty().addListener((observable, oldValue, newValue) -> {
                if(choices.getValue() != null) {
                    confirmButton.setDisable(newValue.trim().isEmpty());
                }
            });

            dialog.getDialogPane().setContent(grid);

            // Convert the result to a username-password-pair when the confirm button is clicked.
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == loginButtonType) {
                    return new Pair<>(choices.getValue().toString(), value.getText());
                }
                return null;
            });

            Optional<Pair<String, String>> result = dialog.showAndWait();

            result.ifPresent(callback -> {
                this.controller.addCriteria(callback.getKey(), callback.getValue());
            });
        });

        clearCriteriaButton.setOnAction((event) -> {
            this.controller.clearCriterias();
        });

        // ============================ UGLY SECTION END ===============================

        // Add Tabs to TabPane
        tabPane.getTabs().add(queryTab);
        // TODO: Do we want to add plotting? if not get rid of tabs

        return tabPane;
    }

    private TableView<Criteria> createCriteriaTable() {
        TableView<Criteria> table = new TableView();

        TableColumn parameters = new TableColumn("Parameters");
//        parameters.setCellValueFactory(new PropertyValueFactory<Criteria, String>("parameter"));
        // TODO: BUG, columns resize when clicked!?
        parameters.prefWidthProperty().bind(table.widthProperty().multiply(0.6));
        parameters.maxWidthProperty().bind(parameters.prefWidthProperty());
        parameters.setResizable(false);
        TableColumn values = new TableColumn("Values");
        values.setCellValueFactory(new PropertyValueFactory<Criteria, String>("value"));
        values.prefWidthProperty().bind(table.widthProperty().multiply(0.4));
        values.maxWidthProperty().bind(values.prefWidthProperty());
        values.setResizable(false);
        table.getColumns().addAll(parameters, values);

        return table;
    }

    private void populateQueryResultTable(ArrayList<QueryResult> results) {
        resultTable.getColumns().clear();
        if(!results.isEmpty()) {
            QueryResult result = results.get(0);
            if(result instanceof Player) {

            }

            HashMap<String, String> values = result.getValues();
            for(String param : result.getParameters()) {
                System.out.println(param);
                TableColumn newCol = new TableColumn(param);
                // TODO: Need to find out how to abstract this
                newCol.setCellValueFactory(new PropertyValueFactory<QueryResult, String>(param));
                resultTable.getColumns().add(newCol);
            }
        }
        resultTable.getItems().addAll(results);
    }

    private void clearQueryResultsTable() {
        controller.clearQueryResults();
    }

    @Override
    public void update(Observable o, Object arg) {
        ObserverNotification notification = (ObserverNotification) arg;
        if(notification.type == ObserverNotification.Type.SEARCH_CRITERIA) {
            ArrayList<Criteria> criteriaList = (ArrayList<Criteria>) notification.obj;
            criteriaTable.getItems().clear();
            criteriaTable.getItems().addAll(criteriaList);
        } else if (notification.type == ObserverNotification.Type.QUERY_RESULT) {
            ArrayList<QueryResult> queryResults = (ArrayList<QueryResult>) notification.obj;
            populateQueryResultTable(queryResults);
        }
    }
}
