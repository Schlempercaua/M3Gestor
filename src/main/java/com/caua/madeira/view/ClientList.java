package com.caua.madeira.view;

import com.caua.madeira.dao.ClienteDAO;
import com.caua.madeira.model.Cliente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Alert;

public class ClientList extends VBox {
    
    private TableView<Client> clientTable;
    private ObservableList<Client> clientData;
    private final java.util.function.Consumer<Client> onClientSelected;
    private final ClienteDAO clienteDAO;
    
    public ClientList(java.util.function.Consumer<Client> onClientSelected) {
        this.onClientSelected = onClientSelected;
        this.clienteDAO = new ClienteDAO();
        initializeUI();
        refreshClientList();
    }
    
    private void initializeUI() {
        setSpacing(10);
        setPadding(new Insets(10));
        
        // Title
        Label titleLabel = new Label("CLIENTES CADASTRADOS");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        // Create table
        clientTable = new TableView<>();
        clientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Create columns
        TableColumn<Client, String> nameCol = new TableColumn<>("Nome");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Client, String> phoneCol = new TableColumn<>("Telefone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        TableColumn<Client, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn<Client, String> documentCol = new TableColumn<>("CPF/CNPJ");
        documentCol.setCellValueFactory(new PropertyValueFactory<>("document"));
        
        // Adiciona as colunas à tabela de forma segura
        @SuppressWarnings("unchecked")
        TableColumn<Client, String>[] columns = new TableColumn[] {
            nameCol, phoneCol, emailCol, documentCol
        };
        clientTable.getColumns().addAll(columns);
        
        // Configura a fábrica de linhas para manipular seleção
        clientTable.setRowFactory(tv -> {
            TableRow<Client> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    // Notifica o listener quando um cliente é selecionado
                    Client selectedClient = row.getItem();
                    if (onClientSelected != null) {
                        onClientSelected.accept(selectedClient);
                    }
                }
            });
            return row;
        });
        
        // Add search functionality
        TextField searchField = new TextField();
        searchField.setPromptText("Buscar cliente...");
        searchField.setPrefWidth(300);
        
        // Add components to layout
        HBox searchBox = new HBox(10);
        searchBox.getChildren().addAll(searchField);
        
        getChildren().addAll(titleLabel, searchBox, clientTable);
        
        // Set table to fill available space
        VBox.setVgrow(clientTable, Priority.ALWAYS);
    }
    
    public void refreshClientList() {
        try {
            // Busca todos os clientes do banco de dados
            List<Cliente> clientes = clienteDAO.listarTodos();
            
            // Converte os Cliente para Client (usado na tabela)
            List<Client> clientList = new ArrayList<>();
            for (Cliente c : clientes) {
                clientList.add(new Client(
                    c.getId(),
                    c.getNome(), 
                    c.getEndereco(), 
                    c.getTelefone(), 
                    c.getEmail(), 
                    c.getDocumento()
                ));
            }
            
            clientData = FXCollections.observableArrayList(clientList);
            clientTable.setItems(clientData);
        } catch (SQLException e) {
            e.printStackTrace();
            // Mostra mensagem de erro
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Erro ao carregar clientes");
            alert.setContentText("Não foi possível carregar a lista de clientes: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    // Classe interna para representar um cliente na tabela
    public static class Client {
        private final int id;
        private final String name;
        private final String address;
        private final String phone;
        private final String email;
        private final String document;
        
        public Client(int id, String name, String address, String phone, String email, String document) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.phone = phone;
            this.email = email;
            this.document = document;
        }
        
        public int getId() { return id; }
        
        // Getters
        // Getters
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public String getDocument() { return document; }
    }
}
