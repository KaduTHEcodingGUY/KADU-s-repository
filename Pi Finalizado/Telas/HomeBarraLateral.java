package Telas;

import Interfaces.ProvedorView;
import Backend.Servicos.AuthService;
import Backend.Servicos.UsuarioService;
import Backend.Servicos.UsuarioService.Usuario;
// ADICIONADO: Import para a nova tela que será aberta.

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class HomeBarraLateral {

    private BorderPane rootLayout;
    private Button botaoSelecionado;
    private ProvedorView clubeFidelidadeViewProvider;
    private BorderPane sidebarPane;
    private Label userName;

    private static final Background BG_BOTAO_NORMAL = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
    private static final Background BG_BOTAO_HOVER = new Background(new BackgroundFill(Color.web("#404040"), new CornerRadii(5), Insets.EMPTY));
    private static final Background BG_BOTAO_SELECIONADO = new Background(new BackgroundFill(Color.web("#005A9E"), new CornerRadii(5), Insets.EMPTY));

    public Parent getRoot(Stage primaryStage) {
        clubeFidelidadeViewProvider = new ClubeFidelidade();
        rootLayout = new BorderPane();
        rootLayout.setBackground(new Background(new BackgroundFill(Color.web("#1E1E1E"), CornerRadii.EMPTY, Insets.EMPTY)));

        sidebarPane = createSidebar(primaryStage);
        rootLayout.setLeft(sidebarPane);

        // A tela inicial continua sendo o Clube Fidelidade
        // REMOVIDO: setCenterView(clubeFidelidadeViewProvider.getView(), true);
        
        return rootLayout;
    }

    private BorderPane createSidebar(Stage primaryStage) { 
        BorderPane sidebarPane = new BorderPane();
        sidebarPane.setPadding(new Insets(20));
        sidebarPane.setPrefWidth(250);
        sidebarPane.setBackground(new Background(new BackgroundFill(Color.web("#2D2D2D"), CornerRadii.EMPTY, Insets.EMPTY)));

        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/Utilitarios/masks.png")));
        logoView.setFitHeight(60);
        logoView.setFitWidth(60);
        HBox logoContainer = new HBox(logoView);
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.setPadding(new Insets(0, 0, 40, 0));
        sidebarPane.setTop(logoContainer);
        
        VBox navButtons = new VBox(15);
        navButtons.setAlignment(Pos.TOP_CENTER);
        
        // Busca o usuário atual e seu tipo
        String userId = AuthService.getCurrentUserId();
        Usuario usuario = UsuarioService.buscarUsuarioPorId(userId);
        String tipoUsuario = (usuario != null) ? usuario.getTipoUsuario() : "cliente"; // Assume 'cliente' como padrão

        Button btnComprar = createNavButtonWithIcon("Comprar Ingressos", "/Utilitarios/ticket.png", new TelaCompraIngresso(this::setCenterView));
        Button btnMeusIngressos = createNavButtonWithIcon("Meus Ingressos", "/Utilitarios/tickets.png", new MeusIngressos());
        Button btnClube = createNavButtonWithIcon("Clube Fidelidade", "/Utilitarios/ClubedeFidelidadeVermelho.png", new ClubeFidelidade());
        Button btnEstatisticas = createNavButtonWithIcon("Estatisticas", "/Utilitarios/ClubedeFidelidadeVermelho.png", new TelaEstatisticas());

        if ("gestor".equalsIgnoreCase(tipoUsuario)) {
            navButtons.getChildren().addAll(btnEstatisticas);
            // Define o estado inicial para gestor
            botaoSelecionado = btnEstatisticas;
            setCenterView(new TelaEstatisticas().getView(), true);
        } else { // Assume cliente ou outro tipo
            navButtons.getChildren().addAll(btnComprar, btnMeusIngressos, btnClube);
            // Define o estado inicial para cliente
            botaoSelecionado = btnClube;
            setCenterView(clubeFidelidadeViewProvider.getView(), true);
        }

        if (botaoSelecionado != null) {
            botaoSelecionado.setBackground(BG_BOTAO_SELECIONADO);
        }
        
        sidebarPane.setCenter(navButtons);

        VBox profileSection = new VBox(10);
        profileSection.setAlignment(Pos.CENTER);
        HBox userBox = new HBox(10);
        userBox.setAlignment(Pos.CENTER_LEFT);
        Circle avatarPlaceholder = new Circle(20, Color.GRAY);
        
        // Busca o usuário atual e exibe seu nome
        userName = new Label(usuario != null ? usuario.getNomeCompleto() : "Usuário");
        userName.setTextFill(Color.WHITE);
        userName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        userBox.getChildren().addAll(avatarPlaceholder, userName);
        
        Button btnLogout = new Button("Logout");
        btnLogout.setPrefWidth(180);
        btnLogout.setTextFill(Color.WHITE);
        btnLogout.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnLogout.setBackground(new Background(new BackgroundFill(Color.web("#DC3545"), new CornerRadii(5), Insets.EMPTY)));
        
        btnLogout.setOnAction(event -> {
            Telalogin telaLogin = new Telalogin();
            Parent loginRoot = telaLogin.getRoot(primaryStage);
            Scene loginScene = new Scene(loginRoot);
            primaryStage.setScene(loginScene);
            primaryStage.setFullScreen(true);
        });

        profileSection.getChildren().addAll(userBox, btnLogout);
        sidebarPane.setBottom(profileSection);

        return sidebarPane;
    }

    private Button createNavButtonWithIcon(String text, String iconPath, ProvedorView provider) {
        Image iconImage = new Image(getClass().getResourceAsStream(iconPath));
        ImageView iconView = new ImageView(iconImage);
        iconView.setFitHeight(20);
        iconView.setFitWidth(20);

        Button button = new Button(text);
        button.setGraphic(iconView);
        
        button.setBackground(BG_BOTAO_NORMAL);
        button.setTextFill(Color.web("#BDBDBD"));
        button.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPrefWidth(200);
        button.setGraphicTextGap(15);
        button.setPadding(new Insets(8, 12, 8, 12));
        button.setContentDisplay(ContentDisplay.LEFT);

        button.hoverProperty().addListener((observable, oldValue, isHovering) -> {
            if (button != botaoSelecionado) {
                button.setBackground(isHovering ? BG_BOTAO_HOVER : BG_BOTAO_NORMAL);
            }
        });

        button.setOnAction(event -> {
            // Agora, quando o provider não for nulo, esta lógica funcionará.
            if (provider != null) {
                if (botaoSelecionado != null) {
                    botaoSelecionado.setBackground(BG_BOTAO_NORMAL);
                }
                botaoSelecionado = button;
                botaoSelecionado.setBackground(BG_BOTAO_SELECIONADO);
                setCenterView(provider.getView(), true);
            } else {
                System.out.println("Tela para '" + button.getText() + "' ainda não implementada.");
            }
        });

        return button;
    }

    public void setCenterView(Node view, boolean showSidebar) {
        rootLayout.setCenter(view);
        if (showSidebar) {
            rootLayout.setLeft(sidebarPane);
        } else {
            rootLayout.setLeft(null);
        }
    }
}