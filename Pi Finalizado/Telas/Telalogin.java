package Telas;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

// REMOVIDO: extends Application
public class Telalogin {

    // MÉTODO NOVO: Monta e retorna a interface de login.
    // Ele recebe o Stage para poder trocar de tela no futuro.
    public Parent getRoot(Stage primaryStage) {
        StackPane root = new StackPane();
        ImageView backgroundImageView = createBackgroundImage();
        Pane gradientOverlay = createGradientOverlay();
        // MODIFICADO: Passamos o Stage para o formulário, para que o botão de login funcione
        VBox loginForm = createLoginForm(primaryStage);

        loginForm.maxWidthProperty().bind(root.widthProperty().multiply(0.4));
        loginForm.maxHeightProperty().bind(root.heightProperty().multiply(0.6));

        root.getChildren().addAll(backgroundImageView, gradientOverlay, loginForm);
        return root;
    }
    
    private ImageView createBackgroundImage() {
        ImageView imageView = new ImageView();
        try {
            String imagePath = "Pi Finalizado/Utilitarios/TesteLogin.jpg";
            java.io.File file = new java.io.File(imagePath);
            System.out.println("[DEBUG Telalogin] Caminho absoluto da imagem: " + file.getAbsolutePath());
            System.out.println("[DEBUG Telalogin] Arquivo existe? " + file.exists());
            Image backgroundImage = new Image(file.toURI().toString());
            imageView.setImage(backgroundImage);
        } catch (Exception e) {
            System.err.println("Erro ao carregar a imagem de fundo (Telalogin). A tela ficará preta.");
            e.printStackTrace();
        }
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(1920);
        return imageView;
    }

    private Pane createGradientOverlay() {
        Pane gradientPane = new Pane();
        gradientPane.setStyle("-fx-background-color: linear-gradient(from 0% 100% to 0% 0%, rgba(0,0,0,1) 55%, rgba(0,0,0,0) 100%);");
        return gradientPane;
    }
    
    // MODIFICADO: Recebe o Stage como parâmetro
    private VBox createLoginForm(Stage primaryStage) {
        VBox formVBox = new VBox(10);
        formVBox.setAlignment(Pos.CENTER);
        formVBox.setPadding(new Insets(40, 40, 40, 40));
        formVBox.setStyle("-fx-background-color: rgba(25, 25, 25, 0.9); -fx-background-radius: 25;");

        Label titleLabel = new Label("Bem vindo de volta!");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");
        VBox.setMargin(titleLabel, new Insets(0, 0, 20, 0));

        // ... (criação dos outros campos de texto continua a mesma)
        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #A0A0A0;");
        TextField emailField = new TextField();
        emailField.setPromptText("Digite seu email");
        emailField.setStyle("-fx-background-color: #333333; -fx-text-fill: #E0E0E0; -fx-background-radius: 8; -fx-border-width: 0; -fx-font-size: 14px; -fx-pref-height: 40px;");
        VBox emailBox = new VBox(5, emailLabel, emailField);

        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #A0A0A0;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Digite sua senha");
        passwordField.setStyle("-fx-background-color: #333333; -fx-text-fill: #E0E0E0; -fx-background-radius: 8; -fx-border-width: 0; -fx-font-size: 14px; -fx-pref-height: 40px;");
        VBox passwordBox = new VBox(5, passwordLabel, passwordField);
        VBox.setMargin(passwordBox, new Insets(10, 0, 0, 0));

        Hyperlink cadastroHlink = new Hyperlink("Primeira vez? Se cadastre por aqui!");
        cadastroHlink.setStyle("-fx-text-fill: #FFC300; -fx-underline: false; -fx-border-color: transparent;");
        cadastroHlink.setOnAction(e -> {

            // Cria a tela principal

            TelaCadastro telaCadastro = new TelaCadastro();
            Parent cadastroRoot = telaCadastro.getRoot(primaryStage); // Passa o Stage para a tela Home
            Scene cadastroScene = new Scene(cadastroRoot);
            
            // Troca a cena na janela principal
            primaryStage.setScene(cadastroScene);
            primaryStage.setFullScreen(true);
        });
                 
        Button loginButton = new Button("Entrar");
        loginButton.setCursor(Cursor.HAND);
        loginButton.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25; -fx-pref-width: 200px; -fx-pref-height: 45px;");
        VBox.setMargin(loginButton, new Insets(15, 0, 0, 0));
        
        // --- AÇÃO DO BOTÃO DE LOGIN ATUALIZADA ---
        loginButton.setOnAction(e -> {
            // Aqui iria a sua lógica de validação de login
            // System.out.println("Login bem-sucedido para: " + emailField.getText());
            
            String email = emailField.getText();
            String password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Campos Vazios", "Por favor, preencha todos os campos.");
                return;
            }

            if (Backend.Servicos.AuthService.signIn(email, password)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sucesso");
                alert.setHeaderText(null);
                alert.setContentText("Login realizado com sucesso!");

                alert.setOnHidden(evt -> {
                    HomeBarraLateral barraLateral = new HomeBarraLateral();
                    Parent rootWithSidebar = barraLateral.getRoot(primaryStage);
                    Scene scene = new Scene(rootWithSidebar);
                    primaryStage.setScene(scene);
                    primaryStage.setFullScreen(true);
                });
                alert.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro no Login", "Email ou senha incorretos. Tente novamente.");
            }
        });
        
        // ... (resto do código do formulário continua o mesmo)
        DropShadow glowEffect = new DropShadow(20, Color.rgb(70, 130, 255, 0.7));
        loginButton.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                loginButton.setEffect(glowEffect);
            } else {
                loginButton.setEffect(null);
            }
        });

        formVBox.getChildren().addAll(titleLabel, emailBox, passwordBox, cadastroHlink, loginButton);
        return formVBox;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}