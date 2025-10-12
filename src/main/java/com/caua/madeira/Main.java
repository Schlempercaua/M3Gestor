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

/*
mvn javafx:run
*/
public class Main extends Application {
    
    private BorderPane root;
    private StackPane contentArea;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("M3Gestor - Madereira pai e filhos");
        
        // Create root layout
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f0f0;");
        
        // Create header
        createHeader();
        
        // Create content area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        root.setCenter(contentArea);
        
        // Show home screen by default
        showHomeScreen();
        
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }
    
    private void createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #2e7d32; -fx-padding: 10;");
        
        // Logo and title
        HBox logoBox = new HBox(10);
        ImageView logo = new ImageView();
        logo.setFitHeight(40);
        logo.setFitWidth(40);
        
        Label title = new Label("Madereira pai e filhos");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        logoBox.getChildren().addAll(logo, title);
        
        // Navigation
        HBox navBox = new HBox(20);
        
        Button homeBtn = createNavButton("Início");
        homeBtn.setOnAction(e -> showHomeScreen());
        
        Button clientsBtn = createNavButton("Clientes");
        clientsBtn.setOnAction(e -> showClientsScreen());
        
        Button quotesBtn = createNavButton("Orçamento");
        quotesBtn.setOnAction(e -> showQuotesScreen());
        
        navBox.getChildren().addAll(homeBtn, clientsBtn, quotesBtn);
        
        // User menu
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
        
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-underline: true;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-underline: true;", "")));
        
        return button;
    }
    
    private void showHomeScreen() {
        VBox homeScreen = new VBox(20);
        homeScreen.setAlignment(Pos.CENTER);
        homeScreen.setPadding(new Insets(40));
        
        // Logo and title
        VBox logoBox = new VBox(10);
        logoBox.setAlignment(Pos.CENTER);
        
        HBox logoTitle = new HBox(10);
        logoTitle.setAlignment(Pos.CENTER);
        
        ImageView logo1 = new ImageView();
        logo1.setFitHeight(60);
        logo1.setFitWidth(60);
        
        Label title = new Label("Madereira pai e filhos");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        ImageView logo2 = new ImageView();
        logo2.setFitHeight(60);
        logo2.setFitWidth(60);
        
        logoTitle.getChildren().addAll(logo1, title, logo2);
        
        // Quick actions
        HBox quickActions = new HBox(40);
        quickActions.setAlignment(Pos.CENTER);
        
        VBox clientsAction = createQuickAction("Clientes", "👥");
        clientsAction.setOnMouseClicked(e -> showClientsScreen());
        
        VBox quotesAction = createQuickAction("Orçamento", "📋");
        quotesAction.setOnMouseClicked(e -> showQuotesScreen());
        
        quickActions.getChildren().addAll(clientsAction, quotesAction);
        
        // Footer
        Label footer = new Label("Sistema para calcular o metro cúbico");
        footer.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
        
        homeScreen.getChildren().addAll(logoBox, logoTitle, new Region(), quickActions, new Region(), footer);
        VBox.setVgrow(homeScreen.getChildren().get(2), Priority.ALWAYS);
        VBox.setVgrow(homeScreen.getChildren().get(4), Priority.ALWAYS);
        
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
        
        action.setOnMouseEntered(e -> action.setStyle(action.getStyle() + "-fx-background-color: #f5f5f5;"));
        action.setOnMouseExited(e -> action.setStyle(action.getStyle().replace("-fx-background-color: #f5f5f5;", "")));
        
        return action;
    }
    
    private void showClientsScreen() {
        SplitPane clientSplitPane = new SplitPane();
        
        // Criamos uma referência final para o ClientForm
        final ClientForm[] clientFormRef = new ClientForm[1];
        
        // Client list on the left with a callback for when a client is selected
        ClientList clientList = new ClientList(selectedClient -> {
            // When a client is selected in the list, load it in the form
            Cliente cliente = new Cliente();
            cliente.setId(selectedClient.getId()); // Definindo o ID do cliente
            cliente.setNome(selectedClient.getName());
            cliente.setEndereco(selectedClient.getAddress());
            cliente.setTelefone(selectedClient.getPhone());
            cliente.setEmail(selectedClient.getEmail());
            cliente.setDocumento(selectedClient.getDocument());
            clientFormRef[0].carregarCliente(cliente);
            
            // Habilita o botão de excluir quando um cliente é selecionado
            clientFormRef[0].habilitarBotaoExcluir(true);
        });
        
        // Client form on the right with a callback to refresh the list after save
        ClientForm clientForm = new ClientForm(() -> {
            // This callback will be executed after saving a client
            // Atualiza a lista de clientes após salvar
            clientList.refreshClientList();
        });
        
        // Armazena a referência ao ClientForm
        clientFormRef[0] = clientForm;
        
        // Set up split pane
        clientSplitPane.getItems().addAll(clientList, clientForm);
        clientSplitPane.setDividerPositions(0.3);
        
        contentArea.getChildren().setAll(clientSplitPane);
    }
    
    private void showQuotesScreen() {
        SplitPane quoteSplitPane = new SplitPane();
        
        // Quote form on the right with save callback to refresh the list
        QuoteForm quoteForm = new QuoteForm(() -> {
            // This will be called after saving a quote
            showQuotesScreen(); // Refresh the screen
        });
        
        // Quote list on the left with callback to load selected quote in the form
        QuoteList quoteList = new QuoteList(quote -> {
            if (quote != null) {
                quoteForm.carregarOrcamento(quote); // Load the selected quote in the form
            }
        });
        
        // Set up split pane
        quoteSplitPane.getItems().addAll(quoteList, quoteForm);
        quoteSplitPane.setDividerPositions(0.3);
        
        contentArea.getChildren().setAll(quoteSplitPane);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}