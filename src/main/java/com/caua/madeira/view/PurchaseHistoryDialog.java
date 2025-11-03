package com.caua.madeira.view;

import com.caua.madeira.dao.QuoteDAO;
import com.caua.madeira.model.Cliente;
import com.caua.madeira.model.Quote;
import com.caua.madeira.model.QuoteItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class PurchaseHistoryDialog extends Stage {

    private final QuoteDAO quoteDAO;
    private final Cliente cliente;
    private final TableView<Quote> quoteTable;
    private final TableView<QuoteItem> itemsTable;
    private ObservableList<Quote> data;
    private final Label totalLabel;

    public PurchaseHistoryDialog(Cliente cliente) {
        this.cliente = cliente;
        this.quoteDAO = new QuoteDAO();
        this.quoteTable = new TableView<>();
        this.itemsTable = new TableView<>();
        this.totalLabel = new Label();
        
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Histórico de Compras - " + (cliente != null ? cliente.getNome() : "Cliente"));
        setScene(buildScene());
        setWidth(1000);
        setHeight(700);
        refresh();
    }

    private Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Title
        Label title = new Label("HISTÓRICO DE COMPRAS DO CLIENTE: " + 
            (cliente != null ? cliente.getNome().toUpperCase() : ""));
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        // Configure quote table (master)
        setupQuoteTable();
        
        // Configure items table (detail)
        setupItemsTable();
        
        // Selection model to update items table when quote is selected
        quoteTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateItemsTable(newSelection);
            }
        });

        // Layout
        VBox topBox = new VBox(10, title);
        topBox.setPadding(new Insets(0, 0, 10, 0));
        
        // Split pane to separate quote list and items
        SplitPane splitPane = new SplitPane();
        
        VBox quoteBox = new VBox(10, new Label("ORÇAMENTOS"), quoteTable);
        VBox itemsBox = new VBox(10, new Label("ITENS DO ORÇAMENTO"), itemsTable, totalLabel);
        
        VBox.setVgrow(quoteTable, Priority.ALWAYS);
        VBox.setVgrow(itemsTable, Priority.ALWAYS);
        
        quoteBox.setPadding(new Insets(10));
        itemsBox.setPadding(new Insets(10));
        
        splitPane.getItems().addAll(quoteBox, itemsBox);
        splitPane.setDividerPositions(0.4);
        
        // Bottom buttons
        Button closeButton = new Button("Fechar");
        closeButton.setOnAction(e -> close());
        
        HBox buttonBox = new HBox(10, closeButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        VBox mainContent = new VBox(10, topBox, splitPane, buttonBox);
        mainContent.setPadding(new Insets(10));
        
        root.setCenter(mainContent);
        
        return new Scene(root);
    }
    
    private void setupQuoteTable() {
        quoteTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Date column
        TableColumn<Quote, String> dateCol = new TableColumn<>("Data");
        dateCol.setCellValueFactory(cell -> {
            Quote q = cell.getValue();
            String s = q.getDate() != null ? q.getFormattedDate() : "";
            return new javafx.beans.property.SimpleStringProperty(s);
        });
        dateCol.setPrefWidth(100);
        
        // Quote name column
        TableColumn<Quote, String> nameCol = new TableColumn<>("Orçamento");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);
        
        // Total column with currency formatting
        TableColumn<Quote, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(cell -> {
            Quote q = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(String.format("R$ %.2f", q.getTotalValue()));
        });
        totalCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        totalCol.setPrefWidth(100);
        
        quoteTable.getColumns().addAll(dateCol, nameCol, totalCol);
    }
    
    private void setupItemsTable() {
        itemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Code column
        TableColumn<QuoteItem, String> codeCol = new TableColumn<>("COD");
        codeCol.setCellValueFactory(cell -> {
            QuoteItem item = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                item.getCode() != null ? item.getCode() : ""
            );
        });
        codeCol.setStyle("-fx-alignment: CENTER;");
        codeCol.setPrefWidth(60);
        
        // Quantity column
        TableColumn<QuoteItem, Integer> qtyCol = new TableColumn<>("QTD");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setStyle("-fx-alignment: CENTER;");
        qtyCol.setPrefWidth(50);
        
        // Width column
        TableColumn<QuoteItem, String> widthCol = new TableColumn<>("LARG.(cm)");
        widthCol.setCellValueFactory(cell -> {
            QuoteItem item = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("%.1f", item.getWidth())
            );
        });
        widthCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        widthCol.setPrefWidth(70);
        
        // Height column
        TableColumn<QuoteItem, String> heightCol = new TableColumn<>("ALT.(cm)");
        heightCol.setCellValueFactory(cell -> {
            QuoteItem item = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("%.1f", item.getHeight())
            );
        });
        heightCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        heightCol.setPrefWidth(70);
        
        // Length column
        TableColumn<QuoteItem, String> lengthCol = new TableColumn<>("COMP.(m)");
        lengthCol.setCellValueFactory(cell -> {
            QuoteItem item = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("%.2f", item.getLength())
            );
        });
        lengthCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        lengthCol.setPrefWidth(70);
        
        // Cubic meters
        TableColumn<QuoteItem, String> m3Col = new TableColumn<>("M³");
        m3Col.setCellValueFactory(cell -> {
            QuoteItem item = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("%.3f", item.getCubicMeters())
            );
        });
        m3Col.setStyle("-fx-alignment: CENTER-RIGHT;");
        m3Col.setPrefWidth(70);
        
        // Unit value
        TableColumn<QuoteItem, String> unitValueCol = new TableColumn<>("VALOR UND. (R$/m³)");
        unitValueCol.setCellValueFactory(cell -> {
            QuoteItem item = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("R$ %.2f", item.getUnitValue())
            );
        });
        unitValueCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        unitValueCol.setPrefWidth(120);
        
        // Total value
        TableColumn<QuoteItem, String> totalCol = new TableColumn<>("TOTAL (R$)");
        totalCol.setCellValueFactory(cell -> {
            QuoteItem item = cell.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("R$ %.2f", item.getTotal())
            );
        });
        totalCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        totalCol.setPrefWidth(100);
        
        itemsTable.getColumns().addAll(codeCol, qtyCol, widthCol, heightCol, lengthCol, m3Col, unitValueCol, totalCol);
    }
    
    private void updateItemsTable(Quote quote) {
        if (quote == null || quote.getItems() == null) {
            itemsTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("");
            return;
        }
        
        itemsTable.setItems(FXCollections.observableArrayList(quote.getItems()));
        
        // Calculate and display totals
        double subtotal = quote.getItems().stream()
                .mapToDouble(QuoteItem::getTotal)
                .sum();
        
        double total = subtotal + quote.getShippingValue();
        
        totalLabel.setText(String.format(
            "Subtotal: R$ %.2f   |   Frete: R$ %.2f   |   Total: R$ %.2f",
            subtotal,
            quote.getShippingValue(),
            total
        ));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 0 0 0;");
    }

    private void refresh() {
        if (cliente == null || cliente.getId() <= 0) {
            quoteTable.setItems(FXCollections.observableArrayList());
            itemsTable.setItems(FXCollections.observableArrayList());
            return;
        }
        
        try {
            List<Quote> quotes = quoteDAO.listarPorCliente(cliente.getId());
            data = FXCollections.observableArrayList(quotes);
            quoteTable.setItems(data);
            
            // Select first quote if available
            if (!quotes.isEmpty()) {
                quoteTable.getSelectionModel().select(0);
                updateItemsTable(quotes.get(0));
            }
        } catch (SQLException e) {
            showError("Erro ao carregar histórico", e);
        }
    }
    
    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}