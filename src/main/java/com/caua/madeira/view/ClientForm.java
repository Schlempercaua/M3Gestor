package com.caua.madeira.view;

import com.caua.madeira.dao.ClienteDAO;
import com.caua.madeira.model.Cliente;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;

import java.sql.SQLException;

public class ClientForm extends VBox {
    
    private final ClienteDAO clienteDAO;
    private Cliente clienteAtual;
    private final Runnable onSaveCallback;
    
    private final TextField nameField;
    private final TextField addressField;
    private final TextField phoneField;
    private final TextField emailField;
    private final TextField documentField;
    private Button deleteButton;
    
    public ClientForm(Runnable onSaveCallback) {
        // Inicialização dos campos
        this.nameField = new TextField();
        this.addressField = new TextField();
        this.phoneField = new TextField();
        this.emailField = new TextField();
        this.documentField = new TextField();
        
        this.clienteDAO = new ClienteDAO();
        this.onSaveCallback = onSaveCallback;
        this.initializeUI();
        this.novoCliente();
    }
    
    private void initializeUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 5;");
        
        // Title
        Label titleLabel = new Label("CADASTRO DE CLIENTE");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        // Form fields
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        
        // Name
        Label nameLabel = new Label("NOME DO CLIENTE *");
        nameField.setPromptText("Digite o nome do cliente");
        nameField.setPrefWidth(400);
        
        // Address
        Label addressLabel = new Label("ENDEREÇO *");
        addressField.setPromptText("Digite o endereço");
        
        // Phone
        Label phoneLabel = new Label("TELEFONE *");
        phoneField.setPromptText("(00) 00000-0000");
        
        // Email
        Label emailLabel = new Label("EMAIL");
        emailField.setPromptText("email@exemplo.com");
        
        // Document (CPF/CNPJ)
        Label documentLabel = new Label("CPF/CNPJ");
        documentField.setPromptText("000.000.000-00 ou 00.000.000/0000-00");
        
        // Add fields to grid
        formGrid.add(nameLabel, 0, 0);
        formGrid.add(nameField, 0, 1, 2, 1);
        
        formGrid.add(addressLabel, 0, 2);
        formGrid.add(addressField, 0, 3, 2, 1);
        
        formGrid.add(phoneLabel, 0, 4);
        formGrid.add(phoneField, 0, 5);
        
        formGrid.add(emailLabel, 1, 4);
        formGrid.add(emailField, 1, 5);
        
        formGrid.add(documentLabel, 0, 6);
        formGrid.add(documentField, 0, 7);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        
        Button saveButton = new Button("Salvar");
        saveButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-pref-width: 100;");
        saveButton.setOnAction(e -> salvarCliente());
        
        Button cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #f5f5f5; -fx-pref-width: 100;");
        cancelButton.setOnAction(e -> limparFormulario());
        
        Button newButton = new Button("Novo");
        newButton.setStyle("-fx-background-color: #f5f5f5; -fx-pref-width: 100;");
        newButton.setOnAction(e -> novoCliente());
        
        this.deleteButton = new Button("Excluir");
        this.deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-pref-width: 100;");
        this.deleteButton.setDisable(true);
        this.deleteButton.setOnAction(e -> excluirCliente());
        
        buttonBox.getChildren().addAll(saveButton, cancelButton, newButton, deleteButton);
        
        // Add help button
        Button helpButton = new Button("?");
        helpButton.setStyle("-fx-background-color: #f5f5f5; -fx-font-weight: bold;");
        helpButton.setPrefSize(30, 30);
        
        // Add all components to the main layout
        HBox titleBox = new HBox(10);
        titleBox.getChildren().addAll(titleLabel, helpButton);
        
        getChildren().addAll(titleBox, formGrid, buttonBox);
    }
    
    private void salvarCliente() {
        try {
            if (validarCampos()) {
                if (clienteAtual == null) {
                    clienteAtual = new Cliente();
                }
                
                clienteAtual.setNome(nameField.getText());
                clienteAtual.setEndereco(addressField.getText());
                clienteAtual.setTelefone(phoneField.getText());
                clienteAtual.setEmail(emailField.getText());
                clienteAtual.setDocumento(documentField.getText());
                
                clienteDAO.salvar(clienteAtual);
                
                showAlert("Sucesso", "Cliente salvo com sucesso!", AlertType.INFORMATION);
                limparFormulario();
                
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erro", "Erro ao salvar cliente: " + e.getMessage(), AlertType.ERROR);
        }
    }
    
    private void excluirCliente() {
        if (clienteAtual != null && clienteAtual.getId() > 0) {
            try {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Confirmar Exclusão");
                alert.setHeaderText("Excluir Cliente");
                alert.setContentText("Tem certeza que deseja excluir este cliente?");
                
                if (alert.showAndWait().get() == ButtonType.OK) {
                    clienteDAO.excluir(clienteAtual.getId());
                    showAlert("Sucesso", "Cliente excluído com sucesso!", AlertType.INFORMATION);
                    limparFormulario();
                    
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erro", "Erro ao excluir cliente: " + e.getMessage(), AlertType.ERROR);
            }
        }
    }
    
    private boolean validarCampos() {
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Validação", "O nome do cliente é obrigatório.", AlertType.WARNING);
            nameField.requestFocus();
            return false;
        }
        
        if (addressField.getText().trim().isEmpty()) {
            showAlert("Validação", "O endereço é obrigatório.", AlertType.WARNING);
            addressField.requestFocus();
            return false;
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            showAlert("Validação", "O telefone é obrigatório.", AlertType.WARNING);
            phoneField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    public void carregarCliente(Cliente cliente) {
        if (cliente != null) {
            this.clienteAtual = cliente;
            nameField.setText(cliente.getNome());
            addressField.setText(cliente.getEndereco());
            phoneField.setText(cliente.getTelefone());
            emailField.setText(cliente.getEmail());
            documentField.setText(cliente.getDocumento());
        }
    }
    
    public void novoCliente() {
        this.clienteAtual = new Cliente();
        limparFormulario();
    }
    
    /**
     * Habilita ou desabilita o botão de exclusão
     * @param habilitar true para habilitar, false para desabilitar
     */
    public void habilitarBotaoExcluir(boolean habilitar) {
        if (deleteButton != null) {
            deleteButton.setDisable(!habilitar);
        }
    }
    
    private void limparFormulario() {
        nameField.clear();
        addressField.clear();
        phoneField.clear();
        emailField.clear();
        documentField.clear();
        
        if (clienteAtual != null) {
            clienteAtual = null;
        }
        
        // Desabilita o botão de excluir ao limpar o formulário
        habilitarBotaoExcluir(false);
    }
    
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
