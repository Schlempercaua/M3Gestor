package com.caua.madeira.view;

import com.caua.madeira.dao.ClienteDAO;
import com.caua.madeira.dao.QuoteDAO;
import com.caua.madeira.model.Cliente;
import com.caua.madeira.model.Quote;
import com.caua.madeira.model.QuoteItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.HPos;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.beans.property.SimpleDoubleProperty;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuoteForm extends VBox {
    
    private TextField quoteNameField;
    private TextField shippingValueField;
    private ComboBox<Cliente> clientComboBox;
    private Label totalItensLabel;
    private Label totalGeralLabel;
    private TableView<QuoteItem> itemsTable;
    private ObservableList<QuoteItem> itemsData;
    private final QuoteDAO quoteDAO;
    private final ClienteDAO clienteDAO;
    private Quote quoteAtual;
    private final Runnable onSaveCallback;
    private Button deleteButton;
    
    public QuoteForm(Runnable onSaveCallback) {
        this.quoteDAO = new QuoteDAO();
        this.clienteDAO = new ClienteDAO();
        this.onSaveCallback = onSaveCallback;
        this.itemsData = FXCollections.observableArrayList();
        initializeUI();
        carregarClientes();
    }
    
    private void initializeUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        
        // Title
        Label titleLabel = new Label("ORÇAMENTO");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        // Quote info section
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(10);
        
        // Quote name
        Label nameLabel = new Label("NOME DO ORÇAMENTO *");
        quoteNameField = new TextField();
        quoteNameField.setPromptText("Ex: Orçamento para Casa Nova");
        
        // Shipping value
        Label shippingLabel = new Label("VALOR DO FRETE *");
        shippingValueField = new TextField();
        shippingValueField.setPromptText("0,00");
        
        // Client selection
        Label clientLabel = new Label("SELECIONE O CLIENTE *");
        clientComboBox = new ComboBox<>();
        clientComboBox.setPromptText("Selecione um cliente");
        clientComboBox.setPrefWidth(300);
        clientComboBox.setCellFactory(lv -> new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                setText(empty ? null : cliente.getNome());
            }
        });
        clientComboBox.setButtonCell(new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                setText(empty ? null : (cliente != null ? cliente.getNome() : ""));
            }
        });
        
        // Add to grid
        infoGrid.add(nameLabel, 0, 0);
        infoGrid.add(quoteNameField, 0, 1);
        infoGrid.add(shippingLabel, 1, 0);
        infoGrid.add(shippingValueField, 1, 1);
        infoGrid.add(clientLabel, 0, 2);
        infoGrid.add(clientComboBox, 0, 3, 2, 1);
        
        // Quote items section
        Label itemsLabel = new Label("DETALHES DO ORÇAMENTO");
        itemsLabel.setStyle("-fx-font-weight: bold;");
        
        // Create items table with buttons
        VBox tableWithButtons = createItemsTable();
        
        // Buttons
        HBox buttonBox = new HBox(10);
        
        Button saveButton = new Button("Salvar");
        saveButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-pref-width: 100;");
        saveButton.setOnAction(e -> salvarOrcamento());
        
        Button cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #f5f5f5; -fx-pref-width: 100;");
        cancelButton.setOnAction(e -> limparFormulario());
        
        Button newButton = new Button("Novo");
        newButton.setStyle("-fx-background-color: #f5f5f5; -fx-pref-width: 100;");
        newButton.setOnAction(e -> limparFormulario());
        
        deleteButton = new Button("Excluir");
        deleteButton.setStyle("-fx-background-color: #f5f5f5; -fx-pref-width: 100;");
        deleteButton.setDisable(true);
        deleteButton.setOnAction(e -> excluirOrcamento());
        
        buttonBox.getChildren().addAll(saveButton, cancelButton, newButton, deleteButton);
        
        // Painel de totais
        GridPane totaisGrid = new GridPane();
        totaisGrid.setHgap(15);
        totaisGrid.setVgap(10);
        totaisGrid.setPadding(new Insets(10, 0, 10, 0));
        
        // Rótulos e valores dos totais
        Label totalItensTextLabel = new Label("TOTAL ITENS:");
        totalItensTextLabel.setStyle("-fx-font-weight: bold;");
        totalItensLabel = new Label("R$ 0,00");
        totalItensLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        Label freteTextLabel = new Label("FRETE:");
        freteTextLabel.setStyle("-fx-font-weight: bold;");
        
        Label totalGeralTextLabel = new Label("TOTAL GERAL:");
        totalGeralTextLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        totalGeralLabel = new Label("R$ 0,00");
        totalGeralLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32; -fx-font-size: 14px;");
        
        // Adiciona os elementos ao grid de totais
        totaisGrid.add(totalItensTextLabel, 0, 0);
        totaisGrid.add(totalItensLabel, 1, 0);
        
        // Remove o shippingValueField do grid de totais
        // pois ele já está no formulário principal
        
        totaisGrid.add(totalGeralTextLabel, 0, 1);
        totaisGrid.add(totalGeralLabel, 1, 1);
        
        // Ajusta o alinhamento à direita para os valores
        GridPane.setHalignment(totalItensLabel, HPos.RIGHT);
        GridPane.setHalignment(totalGeralLabel, HPos.RIGHT);
        
        // Adiciona um separador visual
        Separator separator = new Separator();
        GridPane.setColumnSpan(separator, 2);
        totaisGrid.add(separator, 0, 2, 2, 1);
        
        // Configura o campo de frete para atualizar os totais quando alterado
        shippingValueField.textProperty().addListener((obs, oldValue, newValue) -> {
            atualizarTotais();
        });
        
        // Add all components to the main layout
        getChildren().addAll(titleLabel, infoGrid, itemsLabel, tableWithButtons, totaisGrid, buttonBox);
        
        // Set container to fill available space
        VBox.setVgrow(tableWithButtons, Priority.ALWAYS);
    }
    
    private VBox createItemsTable() {
        itemsTable = new TableView<>();
        itemsTable.setEditable(true);
        itemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // ID column (não editável)
        TableColumn<QuoteItem, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        // Quantity column (editável)
        TableColumn<QuoteItem, Integer> qtyCol = new TableColumn<>("QUANT.");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        qtyCol.setOnEditCommit(event -> {
            QuoteItem item = event.getRowValue();
            item.setQuantity(event.getNewValue());
            atualizarTotais();
        });
        qtyCol.setPrefWidth(60);
        
        // Width column (editável)
        TableColumn<QuoteItem, Double> widthCol = new TableColumn<>("LARGURA (mm)");
        widthCol.setCellValueFactory(new PropertyValueFactory<>("width"));
        widthCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        widthCol.setOnEditCommit(event -> {
            QuoteItem item = event.getRowValue();
            item.setWidth(event.getNewValue());
            atualizarTotais();
        });
        widthCol.setPrefWidth(80);
        
        // Height column (editável)
        TableColumn<QuoteItem, Double> heightCol = new TableColumn<>("ALTURA (mm)");
        heightCol.setCellValueFactory(new PropertyValueFactory<>("height"));
        heightCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        heightCol.setOnEditCommit(event -> {
            QuoteItem item = event.getRowValue();
            item.setHeight(event.getNewValue());
            atualizarTotais();
        });
        heightCol.setPrefWidth(80);
        
        // Length column (editável)
        TableColumn<QuoteItem, Double> lengthCol = new TableColumn<>("COMPRIMENTO (mm)");
        lengthCol.setCellValueFactory(new PropertyValueFactory<>("length"));
        lengthCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        lengthCol.setOnEditCommit(event -> {
            QuoteItem item = event.getRowValue();
            item.setLength(event.getNewValue());
            atualizarTotais();
        });
        lengthCol.setPrefWidth(120);
        
        // Unit value column (editável)
        TableColumn<QuoteItem, Double> unitValueCol = new TableColumn<>("VALOR UND. (R$/m³)");
        unitValueCol.setCellValueFactory(new PropertyValueFactory<>("unitValue"));
        unitValueCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        unitValueCol.setOnEditCommit(event -> {
            QuoteItem item = event.getRowValue();
            item.setUnitValue(event.getNewValue());
            atualizarTotais();
        });
        unitValueCol.setPrefWidth(120);
        
        // Total column (não editável)
        TableColumn<QuoteItem, Double> totalCol = new TableColumn<>("TOTAL (R$)");
        totalCol.setCellValueFactory(cellData -> {
            QuoteItem item = cellData.getValue();
            return new SimpleDoubleProperty(item.getTotal()).asObject();
        });
        totalCol.setPrefWidth(100);
        
        itemsTable.getColumns().addAll(idCol, qtyCol, widthCol, heightCol, lengthCol, unitValueCol, totalCol);
        itemsTable.setItems(itemsData);
        
        // Adiciona botão para adicionar itens
        Button addItemButton = new Button("+ Adicionar Item");
        addItemButton.setOnAction(e -> adicionarNovoItem());
        
        // Adiciona botão para remover itens
        Button removeItemButton = new Button("- Remover Item");
        removeItemButton.setDisable(true);
        removeItemButton.setOnAction(e -> removerItemSelecionado());
        
        // Habilita/desabilita o botão de remover com base na seleção
        itemsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            removeItemButton.setDisable(newSelection == null);
        });
        
        // Adiciona os botões ao layout
        HBox buttonBox = new HBox(10, addItemButton, removeItemButton);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));
        
        VBox tableBox = new VBox(5, itemsTable, buttonBox);
        VBox.setVgrow(itemsTable, Priority.ALWAYS);
        
        return tableBox;
    }
    
    // Atualiza os totais e a interface
    private void atualizarTotais() {
        itemsTable.refresh();
        
        // Atualiza o total do orçamento
        double totalItens = itemsData.stream()
            .mapToDouble(QuoteItem::getTotal)
            .sum();
        
        // Atualiza o campo de frete se necessário
        double frete = 0;
        try {
            frete = parseDoubleSafe(shippingValueField.getText());
        } catch (Exception e) {
            // Se não for um número válido, considera 0
            shippingValueField.setText("0.00");
        }
        
        // Atualiza o total geral (itens + frete)
        double totalGeral = totalItens + frete;
        
        // Atualiza a interface com os totais formatados
        totalItensLabel.setText(String.format("R$ %,.2f", totalItens));
        shippingValueField.setText(String.format("%.2f", frete));
        totalGeralLabel.setText(String.format("R$ %,.2f", totalGeral));
    }
    
    // Métodos auxiliares
    private void carregarClientes() {
        try {
            List<Cliente> clientes = clienteDAO.listarTodos();
            clientComboBox.getItems().setAll(clientes);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erro", "Não foi possível carregar a lista de clientes: " + e.getMessage(), 
                    Alert.AlertType.ERROR);
        }
    }
    
    private void salvarOrcamento() {
        if (!validarCampos()) {
            return;
        }
        
        try {
            Quote quote = new Quote();
            quote.setName(quoteNameField.getText().trim());
            quote.setClientId(clientComboBox.getValue().getId());
            quote.setClientName(clientComboBox.getValue().getNome());
            quote.setShippingValue(parseDoubleSafe(shippingValueField.getText()));
            quote.setItems(new ArrayList<>(itemsData));
            
            if (quoteAtual != null) {
                quote.setId(quoteAtual.getId());
                quoteDAO.atualizar(quote);
                showAlert("Sucesso", "Orçamento atualizado com sucesso!", Alert.AlertType.INFORMATION);
            } else {
                quoteDAO.salvar(quote);
                showAlert("Sucesso", "Orçamento salvo com sucesso!", Alert.AlertType.INFORMATION);
            }
            
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            
            limparFormulario();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erro", "Não foi possível salvar o orçamento: " + e.getMessage(), 
                    Alert.AlertType.ERROR);
        }
    }
    
    private void excluirOrcamento() {
        if (quoteAtual == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText(null);
        alert.setContentText("Tem certeza que deseja excluir este orçamento?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    quoteDAO.excluir(quoteAtual.getId());
                    showAlert("Sucesso", "Orçamento excluído com sucesso!", Alert.AlertType.INFORMATION);
                    
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                    
                    limparFormulario();
                    
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erro", "Não foi possível excluir o orçamento: " + e.getMessage(), 
                            Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    private void limparFormulario() {
        quoteAtual = null;
        quoteNameField.clear();
        shippingValueField.clear();
        clientComboBox.getSelectionModel().clearSelection();
        itemsData.clear();
        
        // Habilita/desabilita botões
        deleteButton.setDisable(true);
    }
    
    private boolean validarCampos() {
        if (quoteNameField.getText().trim().isEmpty()) {
            showAlert("Atenção", "O nome do orçamento é obrigatório.", Alert.AlertType.WARNING);
            quoteNameField.requestFocus();
            return false;
        }
        
        if (clientComboBox.getValue() == null) {
            showAlert("Atenção", "Selecione um cliente.", Alert.AlertType.WARNING);
            clientComboBox.requestFocus();
            return false;
        }
        
        if (itemsData.isEmpty()) {
            showAlert("Atenção", "Adicione pelo menos um item ao orçamento.", Alert.AlertType.WARNING);
            return false;
        }
        
        return true;
    }
    
    private double parseDoubleSafe(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(value.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    private void adicionarNovoItem() {
        // TODO: Implementar diálogo para adicionar novo item
        // Por enquanto, adiciona um item de exemplo
        QuoteItem novoItem = new QuoteItem();
        novoItem.setId(String.valueOf(itemsData.size() + 1));
        novoItem.setQuantity(1);
        novoItem.setWidth(100);
        novoItem.setHeight(100);
        novoItem.setLength(1000);
        novoItem.setUnitValue(1500.0);
        novoItem.calculateTotal();
        
        itemsData.add(novoItem);
    }
    
    private void removerItemSelecionado() {
        if (itemsTable != null) {
            QuoteItem itemSelecionado = itemsTable.getSelectionModel().getSelectedItem();
            if (itemSelecionado != null) {
                itemsData.remove(itemSelecionado);
            }
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Getters para os campos do formulário
    public void carregarOrcamento(Quote quote) {
        if (quote == null) {
            limparFormulario();
            return;
        }
        
        this.quoteAtual = quote;
        quoteNameField.setText(quote.getName());
        
        // Seleciona o cliente correspondente
        clientComboBox.getItems().stream()
            .filter(c -> c.getId() == quote.getClientId())
            .findFirst()
            .ifPresent(c -> clientComboBox.getSelectionModel().select(c));
            
        shippingValueField.setText(String.format("%.2f", quote.getShippingValue()));
        
        // Carrega os itens
        itemsData.clear();
        if (quote.getItems() != null) {
            itemsData.addAll(quote.getItems());
        }
        
        // Habilita o botão de exclusão
        deleteButton.setDisable(false);
    }
}
