package com.caua.madeira;

import com.caua.madeira.model.Cliente;
import com.caua.madeira.view.ClientForm;
import com.caua.madeira.view.ClientList;
import com.caua.madeira.view.QuoteForm;
import com.caua.madeira.view.QuoteList;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class Main extends Application {

    private BorderPane root;
    private StackPane contentArea;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("M3Gestor - Madereira Pai e Filhos");

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f0f0;");

        createHeader();

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        root.setCenter(contentArea);

        showHomeScreen();

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    // ===============================
    // HEADER
    // ===============================
    private void createHeader() {
        HBox header = new HBox(30);
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #2e7d32;");

        // Logo + título
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        // Título
        Label title = new Label("Madereira Pai e Filhos");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        logoBox.getChildren().addAll(title);

        // Navegação
        HBox navBox = new HBox(20);
        navBox.setAlignment(Pos.CENTER);

        Button homeBtn = createNavButton("Início");
        homeBtn.setOnAction(e -> showHomeScreen());

        Button clientsBtn = createNavButton("Clientes");
        clientsBtn.setOnAction(e -> showClientsScreen());

        Button quotesBtn = createNavButton("Orçamento");
        quotesBtn.setOnAction(e -> showQuotesScreen());

        navBox.getChildren().addAll(homeBtn, clientsBtn, quotesBtn);

        // Usuário
        HBox userBox = new HBox(10);
        userBox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(userBox, Priority.ALWAYS);

        Label userName = new Label("Usuário");
        userName.setTextFill(Color.WHITE);

        Circle userIcon = new Circle(15, Color.WHITE);

        userBox.getChildren().addAll(userName, userIcon);

        header.getChildren().addAll(logoBox, navBox, userBox);
        root.setTop(header);
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle() + "-fx-underline: true;")
        );

        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle().replace("-fx-underline: true;", ""))
        );

        return button;
    }

    // ===============================
    // TELA INICIAL
    // ===============================
    private void showHomeScreen() {
        VBox homeScreen = new VBox(30);
        homeScreen.setAlignment(Pos.CENTER);
        homeScreen.setPadding(new Insets(40));

        HBox logoTitle = new HBox(20);
        logoTitle.setAlignment(Pos.CENTER);

        // Título
        Label title = new Label("Madereira Pai e Filhos");
        title.setStyle(
                "-fx-font-size: 34px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #2e7d32;"
        );

        logoTitle.getChildren().addAll(title);

        HBox quickActions = new HBox(50);
        quickActions.setAlignment(Pos.CENTER);

        VBox clientsAction = createQuickAction("Clientes", "👥");
        clientsAction.setOnMouseClicked(e -> showClientsScreen());

        VBox quotesAction = createQuickAction("Orçamento", "📋");
        quotesAction.setOnMouseClicked(e -> showQuotesScreen());

        quickActions.getChildren().addAll(clientsAction, quotesAction);

        Label footer = new Label("Sistema para calcular o metro cúbico");
        footer.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");

        Region spacerTop = new Region();
        Region spacerBottom = new Region();
        VBox.setVgrow(spacerTop, Priority.ALWAYS);
        VBox.setVgrow(spacerBottom, Priority.ALWAYS);

        homeScreen.getChildren().addAll(
                spacerTop,
                logoTitle,
                quickActions,
                spacerBottom,
                footer
        );

        contentArea.getChildren().setAll(homeScreen);
    }

    private VBox createQuickAction(String title, String emoji) {
        VBox action = new VBox(10);
        action.setAlignment(Pos.CENTER);
        action.setPadding(new Insets(30, 50, 30, 50));
        action.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                "-fx-cursor: hand;"
        );

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 36px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        action.getChildren().addAll(emojiLabel, titleLabel);

        action.setOnMouseEntered(e ->
                action.setStyle(action.getStyle() + "-fx-background-color: #f5f5f5;")
        );

        action.setOnMouseExited(e ->
                action.setStyle(action.getStyle().replace("-fx-background-color: #f5f5f5;", ""))
        );

        return action;
    }

    // ===============================
    // CLIENTES
    // ===============================
    private void showClientsScreen() {
        SplitPane clientSplitPane = new SplitPane();

        final ClientForm[] clientFormRef = new ClientForm[1];

        ClientList clientList = new ClientList(selectedClient -> {
            Cliente cliente = new Cliente();
            cliente.setId(selectedClient.getId());
            cliente.setNome(selectedClient.getName());
            cliente.setEndereco(selectedClient.getAddress());
            cliente.setTelefone(selectedClient.getPhone());
            cliente.setEmail(selectedClient.getEmail());
            cliente.setDocumento(selectedClient.getDocument());

            clientFormRef[0].carregarCliente(cliente);
            clientFormRef[0].habilitarBotaoExcluir(true);
        });

        ClientForm clientForm = new ClientForm(() -> {
            clientList.refreshClientList();
        });

        clientFormRef[0] = clientForm;

        clientSplitPane.getItems().addAll(clientList, clientForm);
        clientSplitPane.setDividerPositions(0.3);

        contentArea.getChildren().setAll(clientSplitPane);
    }

    // ===============================
    // ORÇAMENTOS
    // ===============================
    private void showQuotesScreen() {
        SplitPane quoteSplitPane = new SplitPane();

        QuoteForm quoteForm = new QuoteForm(() -> showQuotesScreen());

        QuoteList quoteList = new QuoteList(quote -> {
            if (quote != null) {
                quoteForm.carregarOrcamento(quote);
            }
        });

        quoteSplitPane.getItems().addAll(quoteList, quoteForm);
        quoteSplitPane.setDividerPositions(0.3);

        contentArea.getChildren().setAll(quoteSplitPane);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
