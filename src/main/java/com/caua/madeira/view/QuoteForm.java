package com.caua.madeira.view;

import com.caua.madeira.dao.ClienteDAO;
import com.caua.madeira.dao.QuoteDAO;
import com.caua.madeira.model.Cliente;
import com.caua.madeira.model.Quote;
import com.caua.madeira.model.QuoteItem;
import com.caua.madeira.util.NumberUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.converter.IntegerStringConverter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuoteForm extends VBox {
    
    private TextField quoteNameField;
    private TextField shippingValueField;
    private TextField discountField;
    private TextArea complementoField;
    private ComboBox<Cliente> clientComboBox;
    private Label totalItensLabel;
    private Label totalGeralLabel;
    private TableView<QuoteItem> itemsTable;
    private ObservableList<QuoteItem> itemsData;
    private Button printButton;
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
    
    public void carregarOrcamento(Quote quote) {
        if (quote == null) {
            limparFormulario();
            return;
        }
        
        this.quoteAtual = quote;
        quoteNameField.setText(quote.getName());
        shippingValueField.setText(String.format("%.2f", quote.getShippingValue()).replace(".", ","));
        discountField.setText(String.format("%.2f", quote.getDiscount()));
        complementoField.setText(quote.getComplemento());
        
        // Carrega o cliente selecionado
        if (quote.getClientId() > 0) {
            clientComboBox.getItems().stream()
                .filter(c -> c.getId() == quote.getClientId())
                .findFirst()
                .ifPresent(clientComboBox.getSelectionModel()::select);
        }
        
        // Carrega os itens
        itemsData.setAll(quote.getItems() != null ? quote.getItems() : new ArrayList<>());
        
        // Atualiza os totais
        atualizarTotais();
        
        // Habilita/desabilita botões
        deleteButton.setDisable(false);
        printButton.setDisable(false);
    }
    
    private void carregarClientes() {
        try {
            List<Cliente> clientes = clienteDAO.listarTodos();
            clientComboBox.getItems().setAll(clientes);
        } catch (SQLException e) {
            showAlert("Erro", "Erro ao carregar clientes: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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
        shippingValueField = new TextField("0.00");
        shippingValueField.setPrefWidth(150);
        shippingValueField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([,]\\d{0,2})?")) {
                shippingValueField.setText(oldValue);
            } else {
                atualizarTotais();
            }
        });
        
        // Discount
        Label discountLabel = new Label("DESCONTO (%)");
        discountField = new TextField("0.00");
        discountField.setPrefWidth(100);
        discountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d{0,2}")) {
                discountField.setText(oldValue);
            } else if (!newValue.isEmpty()) {
                try {
                    double discount = Double.parseDouble(newValue);
                    if (discount < 0 || discount > 100) {
                        discountField.setText(oldValue);
                    } else {
                        atualizarTotais();
                    }
                } catch (NumberFormatException e) {
                    discountField.setText(oldValue);
                }
            } else {
                atualizarTotais();
            }
        });
        
        // Client selection
        Label clientLabel = new Label("SELECIONE O CLIENTE *");
        clientComboBox = new ComboBox<>();
        clientComboBox.setPromptText("Selecione um cliente");
        clientComboBox.setPrefWidth(300);
        
        // Complemento
        Label complementoLabel = new Label("COMPLEMENTO");
        complementoField = new TextArea();
        complementoField.setPromptText("Informações adicionais sobre o orçamento");
        complementoField.setPrefRowCount(3);
        complementoField.setWrapText(true);
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
        
        // Add fields to grid
        infoGrid.add(nameLabel, 0, 0);
        infoGrid.add(quoteNameField, 0, 1);
        infoGrid.add(shippingLabel, 1, 0);
        infoGrid.add(shippingValueField, 1, 1);
        infoGrid.add(discountLabel, 2, 0);
        infoGrid.add(discountField, 2, 1);
        infoGrid.add(clientLabel, 0, 2);
        infoGrid.add(clientComboBox, 0, 3, 3, 1);
        infoGrid.add(complementoLabel, 0, 4);
        infoGrid.add(complementoField, 0, 5, 3, 1);
        
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
        
        printButton = new Button("Imprimir");
        printButton.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; -fx-pref-width: 100;");
        printButton.setDisable(true);
        printButton.setOnAction(e -> imprimirOrcamento());
        
        buttonBox.getChildren().addAll(saveButton, cancelButton, newButton, deleteButton, printButton);
        
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
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
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
        TableColumn<QuoteItem, Integer> quantityCol = new TableColumn<>("QUANT.");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityCol.setOnEditCommit(event -> {
            QuoteItem item = event.getRowValue();
            item.setQuantity(event.getNewValue());
            item.calculateTotal();
            atualizarTotais();
        });
        quantityCol.setPrefWidth(60);
        
        // Width column (editável)
        TableColumn<QuoteItem, Double> widthCol = new TableColumn<>("LARGURA (m)");
        widthCol.setCellValueFactory(new PropertyValueFactory<>("width"));
        widthCol.setCellFactory(column -> {
            TextFieldTableCell<QuoteItem, Double> cell = new TextFieldTableCell<QuoteItem, Double>();
            cell.setConverter(NumberUtils.createDoubleStringConverter());
            return cell;
        });
        widthCol.setOnEditCommit(event -> {
            QuoteItem item = event.getRowValue();
            item.setWidth(NumberUtils.parseDouble(event.getNewValue().toString()));
            atualizarTotais();
        });
        widthCol.setPrefWidth(80);
        
        // Height column (editável)
        TableColumn<QuoteItem, Double> heightCol = new TableColumn<>("ALTURA (m)");
        heightCol.setCellValueFactory(new PropertyValueFactory<>("height"));
        heightCol.setCellFactory(column -> {
            TextFieldTableCell<QuoteItem, Double> cell = new TextFieldTableCell<QuoteItem, Double>();
            cell.setConverter(NumberUtils.createDoubleStringConverter());
            return cell;
        });
        heightCol.setOnEditCommit(event -> {
            QuoteItem item = event.getRowValue();
            item.setHeight(NumberUtils.parseDouble(event.getNewValue().toString()));
            atualizarTotais();
        });
        heightCol.setPrefWidth(80);
        
        // Length column (editável)
        TableColumn<QuoteItem, Double> lengthCol = new TableColumn<>("COMPRIMENTO (m)");
        lengthCol.setCellValueFactory(new PropertyValueFactory<>("length"));
        lengthCol.setCellFactory(column -> {
            TextFieldTableCell<QuoteItem, Double> cell = new TextFieldTableCell<QuoteItem, Double>();
            cell.setConverter(NumberUtils.createDoubleStringConverter());
            return cell;
        });
        lengthCol.setOnEditCommit(event -> {
            QuoteItem item = event.getRowValue();
            item.setLength(NumberUtils.parseDouble(event.getNewValue().toString()));
            atualizarTotais();
        });
        lengthCol.setPrefWidth(120);
        
        // Unit value column (editável)
        TableColumn<QuoteItem, Double> unitValueCol = new TableColumn<>("VALOR UND. (R$/m³)");
        unitValueCol.setCellValueFactory(new PropertyValueFactory<>("unitValue"));
        unitValueCol.setCellFactory(column -> {
            TextFieldTableCell<QuoteItem, Double> cell = new TextFieldTableCell<QuoteItem, Double>();
            cell.setConverter(NumberUtils.createCurrencyStringConverter());
            return cell;
        });
        unitValueCol.setOnEditCommit(event -> {
            QuoteItem item = event.getRowValue();
            item.setUnitValue(event.getNewValue());
            item.calculateTotal();
            atualizarTotais();
        });
        unitValueCol.setPrefWidth(150);
        
        // Total column (não editável)
        TableColumn<QuoteItem, String> totalCol = new TableColumn<>("TOTAL (R$)");
        totalCol.setCellValueFactory(cellData -> {
            double total = cellData.getValue().getTotal();
            return new SimpleStringProperty(String.format("R$ %.2f", total));
        });
        totalCol.setPrefWidth(120);
        
        // Configura a tabela
        itemsTable.getColumns().clear();
        itemsTable.getColumns().add(quantityCol);
        itemsTable.getColumns().add(widthCol);
        itemsTable.getColumns().add(heightCol);
        itemsTable.getColumns().add(lengthCol);
        itemsTable.getColumns().add(unitValueCol);
        itemsTable.getColumns().add(totalCol);
        itemsTable.setItems(itemsData);
        itemsTable.setEditable(true);
        
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
        
        // Cria o container para os botões de itens
        HBox itemButtonsBox = new HBox(10, addItemButton, removeItemButton);
        itemButtonsBox.setPadding(new Insets(5, 0, 0, 0));
        
        // Cria o container principal da tabela
        VBox tableBox = new VBox(5, itemsTable, itemButtonsBox);
        VBox.setVgrow(itemsTable, Priority.ALWAYS);
        
        return tableBox;
    }

    private void adicionarNovoItem() {
        QuoteItem novoItem = new QuoteItem();
        itemsData.add(novoItem);
        itemsTable.getSelectionModel().select(novoItem);
        itemsTable.scrollTo(novoItem);
    }

    private void removerItemSelecionado() {
        QuoteItem itemSelecionado = itemsTable.getSelectionModel().getSelectedItem();
        if (itemSelecionado != null) {
            itemsData.remove(itemSelecionado);
            atualizarTotais();
        }
    }

    private void atualizarTotais() {
        double totalItens = itemsData.stream()
                .mapToDouble(QuoteItem::getTotal)
                .sum();
        
        totalItensLabel.setText(String.format("Total dos Itens: R$ %.2f", totalItens));
        
        double frete = 0.0;
        double desconto = 0.0;
        
        try {
            frete = Double.parseDouble(shippingValueField.getText().replace(",", "."));
            desconto = discountField.getText().isEmpty() ? 0.0 : Double.parseDouble(discountField.getText());
            
            // Aplica o desconto se for maior que zero
            if (desconto > 0) {
                totalItens = totalItens - (totalItens * (desconto / 100.0));
            }
        } catch (NumberFormatException e) {
            // Valores inválidos, mantêm 0
        }
        
        double totalGeral = totalItens + frete;
        totalGeralLabel.setText(String.format("Total Geral: R$ %.2f (%.2f%% de desconto aplicado)", 
            totalGeral, desconto));
        
        if (quoteAtual != null) {
            quoteAtual.setShippingValue(frete);
            quoteAtual.setDiscount(desconto);
            quoteAtual.setTotalValue(totalGeral);
        }
    }

    private void imprimirOrcamento() {
        if (quoteAtual == null || quoteAtual.getId() <= 0) {
            showAlert("Atenção", "Nenhum orçamento selecionado para impressão!", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            // Aqui você pode implementar a lógica de impressão
            // Por enquanto, apenas mostramos uma mensagem
            showAlert("Impressão", "Funcionalidade de impressão será implementada aqui.", 
                    Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Erro", "Erro ao gerar impressão: " + e.getMessage(), 
                    Alert.AlertType.ERROR);
        }
    }
    
    private void excluirOrcamento() {
        if (quoteAtual != null && quoteAtual.getId() > 0) {
            Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Tem certeza que deseja excluir este orçamento?",
                ButtonType.YES, 
                ButtonType.NO
            );
            alert.setTitle("Confirmar Exclusão");
            alert.setHeaderText(null);
            
            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.YES) {
                    try {
                        quoteDAO.excluir(quoteAtual.getId());
                        showAlert("Sucesso", "Orçamento excluído com sucesso!", Alert.AlertType.INFORMATION);
                        limparFormulario();
                        if (onSaveCallback != null) {
                            onSaveCallback.run();
                        }
                    } catch (SQLException e) {
                        showAlert("Erro", "Erro ao excluir orçamento: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        }
    }
    
    private void salvarOrcamento() {
        if (validarCampos()) {
            try {
                // Cria um novo orçamento se não existir
                if (quoteAtual == null) {
                    quoteAtual = new Quote();
                }
                
                // Atualiza os dados do orçamento
                quoteAtual.setName(quoteNameField.getText());
                quoteAtual.setShippingValue(Double.parseDouble(shippingValueField.getText().replace(",", ".")));
                quoteAtual.setDiscount(discountField.getText().isEmpty() ? 0.0 : 
                    Double.parseDouble(discountField.getText()));
                quoteAtual.setComplemento(complementoField.getText());
                quoteAtual.setItems(new ArrayList<>(itemsData));
                
                // Atualiza o cliente selecionado
                Cliente clienteSelecionado = clientComboBox.getSelectionModel().getSelectedItem();
                if (clienteSelecionado != null) {
                    quoteAtual.setClientId(clienteSelecionado.getId());
                }
                
                // Salva ou atualiza o orçamento
                if (quoteAtual.getId() > 0) {
                    quoteDAO.atualizar(quoteAtual);
                    showAlert("Sucesso", "Orçamento atualizado com sucesso!", Alert.AlertType.INFORMATION);
                } else {
                    quoteDAO.salvar(quoteAtual);
                    showAlert("Sucesso", "Orçamento salvo com sucesso!", Alert.AlertType.INFORMATION);
                }
                
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erro", "Não foi possível salvar o orçamento: " + e.getMessage(), 
                        Alert.AlertType.ERROR);
            } catch (NumberFormatException e) {
                showAlert("Erro", "Verifique os valores numéricos informados.", 
                        Alert.AlertType.ERROR);
            }
        }
    }
    
    private boolean validarCampos() {
        if (quoteNameField.getText().trim().isEmpty()) {
            showAlert("Atenção", "Informe o nome do orçamento.", Alert.AlertType.WARNING);
            quoteNameField.requestFocus();
            return false;
        }
        
        if (clientComboBox.getSelectionModel().isEmpty()) {
            showAlert("Atenção", "Selecione um cliente.", Alert.AlertType.WARNING);
            clientComboBox.requestFocus();
            return false;
        }
        
        try {
            // Valida valor do frete
            Double.parseDouble(shippingValueField.getText().replace(",", "."));
            
            // Valida desconto, se informado
            if (!discountField.getText().isEmpty()) {
                double desconto = Double.parseDouble(discountField.getText());
                if (desconto < 0 || desconto > 100) {
                    showAlert("Atenção", "O desconto deve estar entre 0 e 100%.", Alert.AlertType.WARNING);
                    discountField.requestFocus();
                    return false;
                }
            }
            
        } catch (NumberFormatException e) {
            showAlert("Atenção", "Verifique os valores numéricos informados.", Alert.AlertType.WARNING);
            return false;
        }
        
        if (itemsData.isEmpty()) {
            showAlert("Atenção", "Adicione pelo menos um item ao orçamento.", Alert.AlertType.WARNING);
            return false;
        }
        
        return true;
    }

    private void showAlert(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void limparFormulario() {
        quoteAtual = new Quote();
        quoteNameField.clear();
        shippingValueField.setText("0.00");
        discountField.setText("0.00");
        complementoField.clear();
        clientComboBox.getSelectionModel().clearSelection();
        itemsData.clear();
        atualizarTotais();
        
        if (deleteButton != null) deleteButton.setDisable(true);
        if (printButton != null) printButton.setDisable(true);
    }
}
