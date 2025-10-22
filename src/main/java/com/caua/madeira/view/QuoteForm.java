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

// OpenPDF
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.Desktop;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
    private Label totalM3Label;
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
        
        // Botão de histórico do cliente
        Button historyButton = new Button("Histórico");
        historyButton.setOnAction(e -> {
            Cliente clienteSel = clientComboBox.getSelectionModel().getSelectedItem();
            if (clienteSel == null && quoteAtual != null && quoteAtual.getClientId() > 0) {
                try {
                    clienteSel = clienteDAO.buscarPorId(quoteAtual.getClientId());
                } catch (SQLException ex) {
                    showAlert("Erro", "Não foi possível carregar o cliente: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
            if (clienteSel == null) {
                showAlert("Atenção", "Selecione um cliente para ver o histórico.", Alert.AlertType.INFORMATION);
                return;
            }
            PurchaseHistoryDialog dialog = new PurchaseHistoryDialog(clienteSel);
            dialog.showAndWait();
        });

        HBox clientBox = new HBox(10, clientComboBox, historyButton);

        // Add fields to grid
        infoGrid.add(nameLabel, 0, 0);
        infoGrid.add(quoteNameField, 0, 1);
        infoGrid.add(shippingLabel, 1, 0);
        infoGrid.add(shippingValueField, 1, 1);
        infoGrid.add(discountLabel, 2, 0);
        infoGrid.add(discountField, 2, 1);
        infoGrid.add(clientLabel, 0, 2);
        infoGrid.add(clientBox, 0, 3, 3, 1);
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
        Label totalM3TextLabel = new Label("TOTAL M3:");
        totalM3TextLabel.setStyle("-fx-font-weight: bold;");
        totalM3Label = new Label("0,000");
        totalM3Label.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        Label freteTextLabel = new Label("FRETE:");
        freteTextLabel.setStyle("-fx-font-weight: bold;");
        
        Label totalGeralTextLabel = new Label("TOTAL GERAL:");
        totalGeralTextLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        totalGeralLabel = new Label("R$ 0,00");
        totalGeralLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32; -fx-font-size: 14px;");
        
        // Adiciona os elementos ao grid de totais
        totaisGrid.add(totalItensTextLabel, 0, 0);
        totaisGrid.add(totalItensLabel, 1, 0);
        totaisGrid.add(totalM3TextLabel, 0, 1);
        totaisGrid.add(totalM3Label, 1, 1);
        
        // Remove o shippingValueField do grid de totais
        // pois ele já está no formulário principal
        
        totaisGrid.add(totalGeralTextLabel, 0, 2);
        totaisGrid.add(totalGeralLabel, 1, 2);
        
        // Ajusta o alinhamento à direita para os valores
        GridPane.setHalignment(totalItensLabel, HPos.RIGHT);
        GridPane.setHalignment(totalM3Label, HPos.RIGHT);
        GridPane.setHalignment(totalGeralLabel, HPos.RIGHT);
        
        // Adiciona um separador visual
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
        GridPane.setColumnSpan(separator, 2);
        totaisGrid.add(separator, 0, 3, 2, 1);
        
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
        TableColumn<QuoteItem, Integer> quantityCol = new TableColumn<>("QUANT");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityCol.setOnEditCommit(event -> {
            QuoteItem item = event.getRowValue();
            item.setQuantity(event.getNewValue());
            item.calculateTotal();
            atualizarTotais();
            itemsTable.refresh();
        });
        quantityCol.setPrefWidth(60);
        
        // Width column (editável)
        TableColumn<QuoteItem, Double> widthCol = new TableColumn<>("LARGURA(cm)");
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
            itemsTable.refresh();
        });
        widthCol.setPrefWidth(80);
        
        // Height column (editável)
        TableColumn<QuoteItem, Double> heightCol = new TableColumn<>("ESPESSURA(cm)");
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
            itemsTable.refresh();
        });
        heightCol.setPrefWidth(80);
        
        // Length column (editável)
        TableColumn<QuoteItem, Double> lengthCol = new TableColumn<>("COMP(m)");
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
            itemsTable.refresh();
        });
        lengthCol.setPrefWidth(120);
        
        // M3 column (não editável)
        TableColumn<QuoteItem, String> m3Col = new TableColumn<>("M3");
        m3Col.setCellValueFactory(cellData -> {
            double m3 = cellData.getValue().getCubicMeters();
            return new SimpleStringProperty(String.format("%.3f", m3).replace('.', ','));
        });
        m3Col.setPrefWidth(90);
        
        // Unit value column (editável)
        TableColumn<QuoteItem, Double> unitValueCol = new TableColumn<>("VALOR UNIT(R$/m³)");
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
            itemsTable.refresh();
        });
        unitValueCol.setPrefWidth(150);
        
        // Total column (não editável)
        TableColumn<QuoteItem, String> totalCol = new TableColumn<>("TOTAL(R$)");
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
        itemsTable.getColumns().add(m3Col);
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
        double totalM3 = itemsData.stream()
                .mapToDouble(QuoteItem::getCubicMeters)
                .sum();
        
        totalItensLabel.setText(String.format("Total dos Itens: R$ %.2f", totalItens));
        if (totalM3Label != null) {
            totalM3Label.setText(String.format("%.3f", totalM3).replace('.', ','));
        }
        
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
            // Busca o cliente completo do orçamento
            Cliente cliente = null;
            if (quoteAtual.getClientId() > 0) {
                try {
                    cliente = clienteDAO.buscarPorId(quoteAtual.getClientId());
                } catch (SQLException ignored) { }
            }

            Path pdfFile = gerarPdfOrcamento(quoteAtual, cliente);

            // Tenta abrir o PDF automaticamente
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile.toFile());
                } else {
                    showAlert("Impressão", "PDF gerado em: " + pdfFile.toAbsolutePath(), Alert.AlertType.INFORMATION);
                }
            } catch (IOException e) {
                showAlert("Impressão", "PDF gerado em: " + pdfFile.toAbsolutePath(), Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Erro", "Erro ao gerar impressão: " + e.getMessage(), 
                    Alert.AlertType.ERROR);
        }
    }

    private Path gerarPdfOrcamento(Quote quote, Cliente cliente) throws Exception {
        // Arquivo temporário
        Path temp = Files.createTempFile("orcamento-" + quote.getId() + "-", ".pdf");

        // Reduz a margem superior para aproveitar melhor o espaço
        Document document = new Document(PageSize.A4, 12, 12, 6, 12);
        PdfWriter.getInstance(document, Files.newOutputStream(temp, StandardOpenOption.TRUNCATE_EXISTING));
        document.open();

        // Fontes
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 7);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 7);
        // Fontes de destaque para cabeçalho
        Font headerLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font headerValueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        // Fontes do bloco à direita do cabeçalho (mais destaque)
        Font headerRightTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        headerRightTitleFont.setColor(new Color(46, 125, 50)); // verde da identidade
        Font headerRightSubFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

        // Duas cópias do orçamento na mesma página
        for (int copy = 0; copy < 2; copy++) {
            // Cabeçalho: logo (esq), informações (centro) e identificação (dir)
            // Aumenta ligeiramente a coluna da logo e cria mais "respiro" para o bloco central
            PdfPTable headerTable = new PdfPTable(new float[]{1.4f, 2.8f, 1.3f});
            headerTable.setWidthPercentage(100);
            headerTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // Coluna da logo (esquerda)
            Image logoImg = carregarLogoOpcional();
            PdfPCell logoCell;
            if (logoImg != null) {
                logoImg.scaleToFit(140, 140);
                logoCell = new PdfPCell(logoImg, false);
            } else {
                logoCell = new PdfPCell(new Phrase("", normalFont));
            }
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            logoCell.setVerticalAlignment(Element.ALIGN_TOP); // alinha o topo com "Orçamento"
            logoCell.setPadding(0f);
            logoCell.setPaddingRight(18f); // mais separação entre a logo e o bloco central
            headerTable.addCell(logoCell);

            // Coluna das informações (centro) mais organizada e destacada
            PdfPTable infoTable = new PdfPTable(new float[]{1.5f, 3.1f});
            infoTable.setWidthPercentage(100);
            infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            PdfPCell l1 = new PdfPCell(new Phrase("E-mail:", headerLabelFont));
            l1.setBorder(Rectangle.NO_BORDER);
            PdfPCell v1 = new PdfPCell(new Phrase("madpaiefilhos@hotmail.com", headerValueFont));
            v1.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(l1); infoTable.addCell(v1);

            PdfPCell l2 = new PdfPCell(new Phrase("Telefone:", headerLabelFont));
            l2.setBorder(Rectangle.NO_BORDER);
            PdfPCell v2 = new PdfPCell(new Phrase("(48) 991252582", headerValueFont));
            v2.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(l2); infoTable.addCell(v2);

            PdfPCell l3 = new PdfPCell(new Phrase("Cidade:", headerLabelFont));
            l3.setBorder(Rectangle.NO_BORDER);
            PdfPCell v3 = new PdfPCell(new Phrase("Alfredo Wagner - 88450-000", headerValueFont));
            v3.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(l3); infoTable.addCell(v3);

            PdfPCell l4 = new PdfPCell(new Phrase("Endereço:", headerLabelFont));
            l4.setBorder(Rectangle.NO_BORDER);
            PdfPCell v4 = new PdfPCell(new Phrase("BR 282 - km 106 - Águas Frias", headerValueFont));
            v4.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(l4); infoTable.addCell(v4);

            PdfPCell infoCell = new PdfPCell(infoTable);
            // Remove a borda e o fundo para ficar mais natural
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            infoCell.setVerticalAlignment(Element.ALIGN_TOP); // alinha o topo com "Orçamento"
            infoCell.setPadding(0f);
            infoCell.setPaddingTop(2f);
            infoCell.setPaddingLeft(18f); // mais espaço entre logo e informações
            headerTable.addCell(infoCell);

            // Coluna direita: "Orçamento" e "Data: ___/___/_____"
            Paragraph rightBlock = new Paragraph();
            rightBlock.setAlignment(Element.ALIGN_RIGHT);
            rightBlock.setLeading(16f); // aumenta o espaçamento entre linhas
            rightBlock.add(new Phrase("Orçamento\n\n", headerRightTitleFont)); // linha extra para dar mais distância
            rightBlock.add(new Phrase("Data: ___/___/_____", headerRightSubFont));

            PdfPCell rightCell = new PdfPCell(rightBlock);
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell.setVerticalAlignment(Element.ALIGN_TOP);
            rightCell.setPaddingRight(2f);
            rightCell.setPaddingTop(2f); // pequeno respiro superior
            headerTable.addCell(rightCell);

            headerTable.setSpacingBefore(0f); // sem espaço extra acima do cabeçalho
            headerTable.setSpacingAfter(10f);
            document.add(headerTable);

            // Bloco cliente (mais informações por linha)
            PdfPTable clienteTable = new PdfPTable(new float[]{1.0f, 3.0f, 1.0f, 2.5f});
            clienteTable.setWidthPercentage(100);
            clienteTable.getDefaultCell().setPadding(1f);
            clienteTable.setSpacingAfter(6f);

            // Cabeçalho da seção (ocupa todas as colunas)
            PdfPCell secHeader = new PdfPCell(new Phrase("Identificação do Solicitante", boldFont));
            secHeader.setBackgroundColor(Color.LIGHT_GRAY);
            secHeader.setColspan(4);
            clienteTable.addCell(secHeader);

            // Linha 1: Cliente | valor   CPF/CNPJ | valor
            clienteTable.addCell(new PdfPCell(new Phrase("Cliente:", boldFont)));
            clienteTable.addCell(new PdfPCell(new Phrase(valorOuVazio(cliente != null ? cliente.getNome() : quote.getClientName()), normalFont)));
            clienteTable.addCell(new PdfPCell(new Phrase("CPF/CNPJ:", boldFont)));
            clienteTable.addCell(new PdfPCell(new Phrase(valorOuVazio(cliente != null ? cliente.getDocumento() : ""), normalFont)));

            // Linha 2: Endereço | valor   Telefone | valor
            clienteTable.addCell(new PdfPCell(new Phrase("Endereço:", boldFont)));
            clienteTable.addCell(new PdfPCell(new Phrase(valorOuVazio(cliente != null ? cliente.getEndereco() : ""), normalFont)));
            clienteTable.addCell(new PdfPCell(new Phrase("Telefone:", boldFont)));
            clienteTable.addCell(new PdfPCell(new Phrase(valorOuVazio(cliente != null ? cliente.getTelefone() : ""), normalFont)));

            // Linha 3: E-mail | valor (valor ocupa as 3 colunas seguintes)
            clienteTable.addCell(new PdfPCell(new Phrase("E-mail:", boldFont)));
            PdfPCell emailValue = new PdfPCell(new Phrase(valorOuVazio(cliente != null ? cliente.getEmail() : ""), normalFont));
            emailValue.setColspan(3);
            clienteTable.addCell(emailValue);

            document.add(clienteTable);

            // Tabela de itens
            PdfPTable itensTable = new PdfPTable(new float[]{0.9f, 1.2f, 1.2f, 1.5f, 1.0f, 1.5f, 1.3f});
            itensTable.setWidthPercentage(100);
            itensTable.getDefaultCell().setPadding(1f);
            itensTable.setSpacingAfter(2f);
            // Centraliza todo o conteúdo das células por padrão
            itensTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            addHeaderCell(itensTable, "QUANT.", boldFont);
            addHeaderCell(itensTable, "LARGURA (cm)", boldFont);
            addHeaderCell(itensTable, "ALTURA (cm)", boldFont);
            addHeaderCell(itensTable, "COMPRIMENTO (m)", boldFont);
            addHeaderCell(itensTable, "M3", boldFont);
            addHeaderCell(itensTable, "VALOR UND. (R$/m³)", boldFont);
            addHeaderCell(itensTable, "TOTAL (R$)", boldFont);

            double subtotal = 0.0;
            double totalM3Pdf = 0.0;
            int renderedRows = 0;
            if (quote.getItems() != null) {
                for (QuoteItem item : quote.getItems()) {
                    itensTable.addCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                    itensTable.addCell(new Phrase(QuoteItem.formatDoubleBr(item.getWidth()), normalFont));
                    itensTable.addCell(new Phrase(QuoteItem.formatDoubleBr(item.getHeight()), normalFont));
                    itensTable.addCell(new Phrase(QuoteItem.formatDoubleBr(item.getLength()), normalFont));
                    itensTable.addCell(new Phrase(String.format("%.3f", item.getCubicMeters()).replace('.', ','), normalFont));
                    itensTable.addCell(new Phrase(String.format("%.2f", item.getUnitValue()).replace('.', ','), normalFont));
                    itensTable.addCell(new Phrase(String.format("R$ %.2f", item.getTotal()).replace('.', ','), normalFont));
                    subtotal += item.getTotal();
                    totalM3Pdf += item.getCubicMeters();
                    renderedRows++;
                }
            }

            // Preenche com linhas marcadas com "---" até atingir 17 linhas
            int minRows = 17;
            for (int i = renderedRows; i < minRows; i++) {
                itensTable.addCell(new Phrase("---", normalFont)); // QUANT
                itensTable.addCell(new Phrase("---", normalFont)); // LARGURA
                itensTable.addCell(new Phrase("---", normalFont)); // ALTURA
                itensTable.addCell(new Phrase("---", normalFont)); // COMPRIMENTO
                itensTable.addCell(new Phrase("---", normalFont)); // M3
                itensTable.addCell(new Phrase("---", normalFont)); // VALOR UND
                itensTable.addCell(new Phrase("---", normalFont)); // TOTAL
            }

            // Rodapé com total de M3 e Subtotal sob as colunas correspondentes
            Font footerBold = boldFont;
            Color footerBg = Color.LIGHT_GRAY;
            // QUANT, LARGURA, ALTURA, COMPRIMENTO -> células vazias
            for (int i = 0; i < 4; i++) {
                PdfPCell empty = new PdfPCell(new Phrase("", footerBold));
                empty.setBackgroundColor(footerBg);
                itensTable.addCell(empty);
            }
            // M3 total
            PdfPCell m3TotalCell = new PdfPCell(new Phrase(String.format("%.3f", totalM3Pdf).replace('.', ','), footerBold));
            m3TotalCell.setBackgroundColor(footerBg);
            m3TotalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itensTable.addCell(m3TotalCell);
            // UNIT vazio
            PdfPCell unitEmpty = new PdfPCell(new Phrase("", footerBold));
            unitEmpty.setBackgroundColor(footerBg);
            itensTable.addCell(unitEmpty);
            // TOTAL subtotal
            PdfPCell subtotalCell = new PdfPCell(new Phrase(String.format("R$ %.2f", subtotal).replace('.', ','), footerBold));
            subtotalCell.setBackgroundColor(footerBg);
            subtotalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itensTable.addCell(subtotalCell);
            document.add(itensTable);

            // Totais
            double desconto = quote.getDiscount(); // percentual
            double valorDesconto = desconto > 0 ? (subtotal * (desconto / 100.0)) : 0.0; // em R$
            double subtotalComDesconto = subtotal - valorDesconto;
            double totalGeral = subtotalComDesconto + quote.getShippingValue();

            // Tabela de totais alinhada aos 2 últimos campos (UNIT e TOTAL) da tabela de itens
            float availableWidth = (float) (document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
            float[] itemCols = new float[]{0.9f, 1.2f, 1.2f, 1.5f, 1.0f, 1.5f, 1.3f};
            float sum = 0f; for (float v : itemCols) sum += v;
            float unitColWidth = availableWidth * (1.5f / sum);   // largura da coluna VALOR UND. (R$/m³)
            float totalColWidth = availableWidth * (1.3f / sum);  // largura da coluna TOTAL (R$)

            PdfPTable totaisTable = new PdfPTable(2);
            totaisTable.setTotalWidth(new float[]{unitColWidth, totalColWidth});
            totaisTable.setLockedWidth(true);
            totaisTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totaisTable.getDefaultCell().setPadding(0.5f);
            totaisTable.setSpacingAfter(0.5f);

            addInfoRow(totaisTable, "Subtotal:", formatCurrency(subtotal), "Desconto (R$):", formatCurrency(valorDesconto), boldFont, normalFont);

            // Linha "Frete" (normal)
            PdfPCell freteLabel = new PdfPCell(new Phrase("Frete:", boldFont));
            PdfPCell freteValue = new PdfPCell(new Phrase(formatCurrency(quote.getShippingValue()), normalFont));
            totaisTable.addCell(freteLabel);
            totaisTable.addCell(freteValue);

            // Linha "Total Geral" destacada
            Font totalLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            totalLabelFont.setColor(Color.WHITE);
            Font totalValueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            totalValueFont.setColor(Color.WHITE);
            Color highlightBg = new Color(46, 125, 50); // #2e7d32

            PdfPCell totalLabelCell = new PdfPCell(new Phrase("Total Geral:", totalLabelFont));
            totalLabelCell.setBackgroundColor(highlightBg);
            totalLabelCell.setBorderWidthTop(1.5f);
            totalLabelCell.setPadding(2f);

            PdfPCell totalValueCell = new PdfPCell(new Phrase(formatCurrency(totalGeral), totalValueFont));
            totalValueCell.setBackgroundColor(highlightBg);
            totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValueCell.setBorderWidthTop(1.5f);
            totalValueCell.setPadding(2f);

            totaisTable.addCell(totalLabelCell);
            totaisTable.addCell(totalValueCell);
            document.add(totaisTable);

            if (quote.getComplemento() != null && !quote.getComplemento().isBlank()) {
                Paragraph comp = new Paragraph("Observações: " + quote.getComplemento(), smallFont);
                document.add(comp);
            }

            // Separador entre as cópias (apenas após a primeira)
            if (copy == 0) {
                document.add(new com.lowagie.text.pdf.draw.LineSeparator(0.5f, 100, Color.GRAY, Element.ALIGN_CENTER, -2));
            }
        }

        document.close();
        return temp;
    }

    private Image carregarLogoOpcional() {
        // Ordem de preferência de nomes (inclui o informado pelo usuário)
        String[] names = new String[]{
                "img_logo.jpg", // preferencial informado pelo usuário (raiz ou resources)
                "logo.jpg",
                "logo.png"
        };

        // 1) Classpath (ex.: src/main/resources)
        for (String n : names) {
            try (InputStream is = getClass().getResourceAsStream("/" + n)) {
                if (is != null) {
                    return Image.getInstance(is.readAllBytes());
                }
            } catch (Exception ignored) { }
        }

        // 2) Raiz do projeto (execução em dev)
        for (String n : names) {
            try {
                Path p = Path.of(n);
                if (Files.exists(p)) {
                    return Image.getInstance(Files.readAllBytes(p));
                }
            } catch (Exception ignored) { }
        }

        // 3) Caminho de fontes para IDE
        for (String n : names) {
            try {
                Path p = Path.of("src", "main", "resources", n);
                if (Files.exists(p)) {
                    return Image.getInstance(Files.readAllBytes(p));
                }
            } catch (Exception ignored) { }
        }

        return null;
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addInfoRow(PdfPTable table, String l1, String v1, String l2, String v2, Font bold, Font normal) {
        PdfPCell c1 = new PdfPCell();
        c1.setPhrase(new Phrase(l1, bold));
        table.addCell(c1);
        PdfPCell c2 = new PdfPCell();
        c2.setPhrase(new Phrase(v1, normal));
        table.addCell(c2);
        PdfPCell c3 = new PdfPCell();
        c3.setPhrase(new Phrase(l2, bold));
        table.addCell(c3);
        PdfPCell c4 = new PdfPCell();
        c4.setPhrase(new Phrase(v2, normal));
        table.addCell(c4);
    }

    private void addInfoRowFull(PdfPTable table, String label, String value, Font bold, Font normal, Color bg) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, bold));
        if (bg != null) c1.setBackgroundColor(bg);
        table.addCell(c1);
        PdfPCell c2 = new PdfPCell(new Phrase(value, normal));
        if (bg != null) c2.setBackgroundColor(bg);
        table.addCell(c2);
    }

    private String valorOuVazio(String v) { return v == null ? "" : v; }

    private String formatCurrency(double v) {
        return ("R$ " + String.format("%.2f", v)).replace('.', ',');
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
                
                // Atualiza o cliente selecionado (id e nome)
                Cliente clienteSelecionado = clientComboBox.getSelectionModel().getSelectedItem();
                if (clienteSelecionado != null) {
                    quoteAtual.setClientId(clienteSelecionado.getId());
                    quoteAtual.setClientName(clienteSelecionado.getNome());
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
