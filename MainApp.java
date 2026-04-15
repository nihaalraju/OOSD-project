package com.supportticket;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class MainApp extends Application {

    private TicketManager manager = new TicketManager();
    private ObservableList<Ticket> displayList = FXCollections.observableArrayList();
    private TableView<Ticket> tableView;
    private Label lblOpen, lblInProgress, lblResolved, lblTotal;
    private TextField txtSearch;
    private ComboBox<String> cbStatusFilter, cbPriorityFilter;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Customer Support Ticket System");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4f8;");

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Left: Stats panel
        VBox leftPanel = createStatsPanel();
        root.setLeft(leftPanel);

        // Center: Ticket table + search
        VBox centerPane = createCenterPanel();
        root.setCenter(centerPane);

        // Right: Form panel
        VBox rightPanel = createFormPanel(stage);
        root.setRight(rightPanel);

        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css") != null
            ? getClass().getResource("/style.css").toExternalForm() : "");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(650);
        stage.show();
        refreshTable(manager.getAllTickets());
        updateStats();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: #1a237e;");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        Label title = new Label("Customer Support Ticket System");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label subtitle = new Label("Ver 1.0");
        subtitle.setTextFill(Color.LIGHTBLUE);
        subtitle.setFont(Font.font("Arial", 12));

        header.getChildren().addAll(title, spacer, subtitle);
        return header;
    }

    private VBox createStatsPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(170);
        panel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 0 1 0 0;");

        Label header = new Label("Dashboard");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        header.setTextFill(Color.web("#1a237e"));

        lblTotal     = createStatCard("Total", "0", "#1a237e");
        lblOpen      = createStatCard("Open", "0", "#e53935");
        lblInProgress = createStatCard("In Progress", "0", "#f57c00");
        lblResolved  = createStatCard("Resolved", "0", "#2e7d32");

        Separator sep = new Separator();

        Label legend = new Label("PRIORITY GUIDE");
        legend.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        legend.setTextFill(Color.GRAY);

        String[] prioColors = {"Critical","High","Medium","Low"};
        VBox prioBox = new VBox(5);
        for (String p : prioColors) {
            Label l = new Label(p);
            l.setFont(Font.font("Arial", 11));
            prioBox.getChildren().add(l);
        }

        panel.getChildren().addAll(header, lblTotal, lblOpen, lblInProgress, lblResolved, sep, legend, prioBox);
        return panel;
    }

    private Label createStatCard(String title, String value, String color) {
        Label lbl = new Label(title + "\n" + value);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web(color));
        lbl.setPadding(new Insets(8));
        lbl.setStyle("-fx-background-color: #f8f9fa; -fx-border-radius: 6; -fx-background-radius: 6;");
        lbl.setMaxWidth(Double.MAX_VALUE);
        return lbl;
    }

    private VBox createCenterPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));

        // Search & Filter row
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Search by name, ID, category...");
        txtSearch.setPrefWidth(250);
        txtSearch.textProperty().addListener((obs, o, n) -> performSearch(n));

        cbStatusFilter = new ComboBox<>(FXCollections.observableArrayList("All","Open","In Progress","Resolved","Closed"));
        cbStatusFilter.setValue("All");
        cbStatusFilter.setOnAction(e -> applyFilters());

        cbPriorityFilter = new ComboBox<>(FXCollections.observableArrayList("All","Critical","High","Medium","Low"));
        cbPriorityFilter.setValue("All");
        cbPriorityFilter.setOnAction(e -> applyFilters());

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-background-radius: 4;");
        btnRefresh.setOnAction(e -> { refreshTable(manager.getAllTickets()); updateStats(); });

        Label lblStatus = new Label("Status:");
        Label lblPriority = new Label("Priority:");
        searchRow.getChildren().addAll(txtSearch, lblStatus, cbStatusFilter, lblPriority, cbPriorityFilter, btnRefresh);

        // Table
        tableView = new TableView<>();
        tableView.setPlaceholder(new Label("No tickets found."));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<Ticket, String> colId = col("Ticket ID", "ticketId", 100);
        TableColumn<Ticket, String> colName = col("Customer Name", "customerName", 140);
        TableColumn<Ticket, String> colCat = col("Category", "category", 100);
        TableColumn<Ticket, String> colPrio = col("Priority", "priority", 90);
        TableColumn<Ticket, String> colStatus = col("Status", "status", 100);
        TableColumn<Ticket, String> colDate = col("Created", "createdDate", 120);
        TableColumn<Ticket, String> colDesc = col("Description", "description", 200);

        tableView.getColumns().addAll(colId, colName, colCat, colPrio, colStatus, colDate, colDesc);
        tableView.setItems(displayList);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Action row for selected ticket
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        Button btnResolve = new Button("Mark Resolved");
        btnResolve.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-background-radius: 4;");

        Button btnInProgress = new Button("In Progress");
        btnInProgress.setStyle("-fx-background-color: #f57c00; -fx-text-fill: white; -fx-background-radius: 4;");

        Button btnClose = new Button("Close Ticket");
        btnClose.setStyle("-fx-background-color: #546e7a; -fx-text-fill: white; -fx-background-radius: 4;");

        Button btnDelete = new Button("Delete");
        btnDelete.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-background-radius: 4;");

        btnResolve.setOnAction(e -> updateSelectedStatus("Resolved"));
        btnInProgress.setOnAction(e -> updateSelectedStatus("In Progress"));
        btnClose.setOnAction(e -> updateSelectedStatus("Closed"));
        btnDelete.setOnAction(e -> deleteSelected());

        actionRow.getChildren().addAll(new Label("Selected Ticket: "), btnResolve, btnInProgress, btnClose, btnDelete);

        panel.getChildren().addAll(searchRow, tableView, actionRow);
        return panel;
    }

    private <T> TableColumn<Ticket, T> col(String header, String property, double width) {
        TableColumn<Ticket, T> col = new TableColumn<>(header);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(width);
        return col;
    }

    private VBox createFormPanel(Stage stage) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(280);
        panel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 0 0 0 1;");

        Label title = new Label("New Ticket");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        title.setTextFill(Color.web("#1a237e"));

        TextField txtName  = field("Customer Name *");
        TextField txtEmail = field("Email Address *");
        ComboBox<String> cbCat = new ComboBox<>(FXCollections.observableArrayList("Technical","Billing","General","Shipping","Account"));
        cbCat.setPromptText("Select Category *");
        cbCat.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> cbPrio = new ComboBox<>(FXCollections.observableArrayList("Critical","High","Medium","Low"));
        cbPrio.setPromptText("Select Priority *");
        cbPrio.setMaxWidth(Double.MAX_VALUE);

        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Describe the issue...");
        txtDesc.setPrefRowCount(4);
        txtDesc.setWrapText(true);

        Label lblErr = new Label("");
        lblErr.setTextFill(Color.RED);
        lblErr.setWrapText(true);

        Button btnSubmit = new Button("Submit Ticket");
        btnSubmit.setMaxWidth(Double.MAX_VALUE);
        btnSubmit.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-font-size:13px; -fx-background-radius: 6; -fx-padding: 8;");

        btnSubmit.setOnAction(e -> {
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String cat = cbCat.getValue();
            String prio = cbPrio.getValue();
            String desc = txtDesc.getText().trim();

            if (name.isEmpty() || email.isEmpty() || cat == null || prio == null || desc.isEmpty()) {
                lblErr.setText("All fields are required!");
                return;
            }
            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                lblErr.setText("Invalid email format!");
                return;
            }
            lblErr.setText("");
            Ticket t = new Ticket(name, email, cat, prio, desc);
            manager.addTicket(t);
            refreshTable(manager.getAllTickets());
            updateStats();
            txtName.clear(); txtEmail.clear(); cbCat.setValue(null); cbPrio.setValue(null); txtDesc.clear();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ticket Submitted");
            alert.setHeaderText(null);
            alert.setContentText("Ticket " + t.getTicketId() + " created successfully!");
            alert.showAndWait();
        });

        Button btnClear = new Button("Clear Form");
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnClear.setStyle("-fx-background-color: #90a4ae; -fx-text-fill: white; -fx-background-radius: 6;");
        btnClear.setOnAction(e -> { txtName.clear(); txtEmail.clear(); cbCat.setValue(null); cbPrio.setValue(null); txtDesc.clear(); lblErr.setText(""); });

        Separator sep = new Separator();
        Label editTitle = new Label("Edit Selected Ticket");
        editTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        editTitle.setTextFill(Color.web("#37474f"));

        Button btnLoadEdit = new Button("Load Selected for Edit");
        btnLoadEdit.setMaxWidth(Double.MAX_VALUE);
        btnLoadEdit.setStyle("-fx-background-color: #0277bd; -fx-text-fill: white; -fx-background-radius: 6;");
        btnLoadEdit.setOnAction(e -> {
            Ticket sel = tableView.getSelectionModel().getSelectedItem();
            if (sel == null) { lblErr.setText("Select a ticket first!"); return; }
            txtName.setText(sel.getCustomerName());
            txtEmail.setText(sel.getEmail());
            cbCat.setValue(sel.getCategory());
            cbPrio.setValue(sel.getPriority());
            txtDesc.setText(sel.getDescription());
            btnSubmit.setText("Update Ticket");
            btnSubmit.setOnAction(ev -> {
                sel.setCustomerName(txtName.getText().trim());
                sel.setEmail(txtEmail.getText().trim());
                sel.setCategory(cbCat.getValue());
                sel.setPriority(cbPrio.getValue());
                sel.setDescription(txtDesc.getText().trim());
                refreshTable(manager.getAllTickets());
                updateStats();
                btnSubmit.setText("Submit Ticket");
                txtName.clear(); txtEmail.clear(); cbCat.setValue(null); cbPrio.setValue(null); txtDesc.clear();
            });
        });

        panel.getChildren().addAll(title, new Label("Name:"), txtName, new Label("Email:"), txtEmail,
            new Label("Category:"), cbCat, new Label("Priority:"), cbPrio,
            new Label("Description:"), txtDesc, lblErr, btnSubmit, btnClear, sep, editTitle, btnLoadEdit);
        return panel;
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        return tf;
    }

    private void refreshTable(java.util.List<Ticket> tickets) {
        displayList.setAll(tickets);
        tableView.refresh();
    }

    private void updateStats() {
        int total = manager.getAllTickets().size();
        long open = manager.getCountByStatus("Open");
        long inProg = manager.getCountByStatus("In Progress");
        long resolved = manager.getCountByStatus("Resolved");
        lblTotal.setText("Total\n" + total);
        lblOpen.setText("Open\n" + open);
        lblInProgress.setText("In Progress\n" + inProg);
        lblResolved.setText("Resolved\n" + resolved);
    }

    private void updateSelectedStatus(String status) {
        Ticket sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("No ticket selected.", "Please select a ticket first."); return; }
        sel.setStatus(status);
        refreshTable(manager.getAllTickets());
        updateStats();
    }

    private void deleteSelected() {
        Ticket sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("No ticket selected.", "Please select a ticket to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete ticket " + sel.getTicketId() + "?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                manager.deleteTicket(sel.getTicketId());
                refreshTable(manager.getAllTickets());
                updateStats();
            }
        });
    }

    private void performSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            refreshTable(manager.getAllTickets());
        } else {
            refreshTable(manager.searchTickets(keyword));
        }
    }

    private void applyFilters() {
        String status = cbStatusFilter.getValue();
        String priority = cbPriorityFilter.getValue();
        java.util.List<Ticket> filtered = manager.filterByStatus(status);
        if (!priority.equals("All")) {
            filtered = filtered.stream()
                .filter(t -> t.getPriority().equals(priority))
                .collect(java.util.stream.Collectors.toList());
        }
        refreshTable(filtered);
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
