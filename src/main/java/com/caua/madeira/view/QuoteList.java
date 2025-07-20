package com.caua.madeira.view;

import com.caua.madeira.dao.QuoteDAO;
import com.caua.madeira.model.Quote;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class QuoteList extends VBox {
    
    private final TableView<Quote> quoteTable;
    private ObservableList<Quote> quoteData;
    private final QuoteDAO quoteDAO;
    private final Consumer<Quote> onQuoteSelected;
    
    public QuoteList(Consumer<Quote> onQuoteSelected) {
        this.onQuoteSelected = onQuoteSelected;
        this.quoteDAO = new QuoteDAO();
        this.quoteTable = new TableView<>();
        initializeUI();
        refreshQuoteList();
    }
    
    private void initializeUI() {
        setSpacing(10);
        setPadding(new Insets(10));
        
        // Title
        Label titleLabel = new Label("ORÇAMENTOS SALVOS");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        // Configure table
        quoteTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Create columns
        TableColumn<Quote, String> nameCol = new TableColumn<>("Nome do Orçamento");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Quote, String> clientCol = new TableColumn<>("Cliente");
        clientCol.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        
        TableColumn<Quote, String> dateCol = new TableColumn<>("Data");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        TableColumn<Quote, Double> totalCol = new TableColumn<>("Valor Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalValue"));
        
        // Adiciona as colunas à tabela de forma segura
        @SuppressWarnings("unchecked")
        TableColumn<Quote, ?>[] columns = new TableColumn[] {
            nameCol, clientCol, dateCol, totalCol
        };
        quoteTable.getColumns().addAll(columns);
        
        // Configura a fábrica de linhas para manipular seleção
        quoteTable.setRowFactory(tv -> {
            TableRow<Quote> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    // Carrega os dados do orçamento no formulário quando a linha for clicada duas vezes
                    Quote selectedQuote = row.getItem();
                    if (onQuoteSelected != null) {
                        try {
                            // Busca o orçamento completo do banco de dados
                            Quote quoteCompleto = quoteDAO.buscarPorId(selectedQuote.getId());
                            onQuoteSelected.accept(quoteCompleto);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            showAlert("Erro", "Não foi possível carregar os detalhes do orçamento: " + e.getMessage(), 
                                    Alert.AlertType.ERROR);
                        }
                    }
                }
            });
            return row;
        });
        
        // Add search functionality
        TextField searchField = new TextField();
        searchField.setPromptText("Buscar orçamento...");
        searchField.setPrefWidth(300);
        
        // Add components to layout
        HBox searchBox = new HBox(10);
        searchBox.getChildren().addAll(searchField);
        
        getChildren().addAll(titleLabel, searchBox, quoteTable);
        
        // Set table to fill available space
        VBox.setVgrow(quoteTable, Priority.ALWAYS);
    }
    
    public void refreshQuoteList() {
        try {
            List<Quote> quotes = quoteDAO.listarTodos();
            quoteData = FXCollections.observableArrayList(quotes);
            quoteTable.setItems(quoteData);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erro", "Não foi possível carregar a lista de orçamentos: " + e.getMessage(), 
                    Alert.AlertType.ERROR);
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
