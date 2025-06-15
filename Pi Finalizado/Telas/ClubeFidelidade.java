package Telas;

import Interfaces.ProvedorView;
import Backend.Servicos.AuthService;
import com.google.gson.JsonObject;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ClubeFidelidade implements ProvedorView {

    private java.util.function.Consumer<javafx.scene.Node> onNavigateToPayment;

    public ClubeFidelidade() {
        // Construtor padrão (pode ser usado se não houver necessidade de navegação imediata)
    }

    public ClubeFidelidade(java.util.function.Consumer<javafx.scene.Node> onNavigateToPayment) {
        this.onNavigateToPayment = onNavigateToPayment;
    }

    @Override
    public Node getView() {
        BorderPane viewLayout = new BorderPane();

        try {
            String imagePath = "Pi Finalizado/Utilitarios/Testelogin.jpg";
            java.io.File file = new java.io.File(imagePath);
            System.out.println("[DEBUG ClubeFidelidade] Caminho absoluto da imagem: " + file.getAbsolutePath());
            System.out.println("[DEBUG ClubeFidelidade] Arquivo existe? " + file.exists());
            Image imagemFundo = new Image(file.toURI().toString());
            BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, false, true); // cover
            BackgroundImage backgroundImage = new BackgroundImage(imagemFundo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
            viewLayout.setBackground(new Background(backgroundImage));
        } catch (Exception e) {
            System.err.println("Erro ao carregar a imagem de fundo (ClubeFidelidade). Usando cor de fundo sólida.");
            e.printStackTrace();
            // Em caso de erro, define um fundo de cor sólida para evitar NullPointerException
            viewLayout.setBackground(new Background(new BackgroundFill(Color.web("#1E1E1E"), CornerRadii.EMPTY, Insets.EMPTY)));
        }

        Label titulo = new Label("Clube Fidelidade");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titulo.setTextFill(Color.web("#FFD700"));
        BorderPane.setAlignment(titulo, Pos.CENTER);
        titulo.setPadding(new Insets(50, 0, 0, 0));
        viewLayout.setTop(titulo);
        
        VBox centerContent = new VBox(35);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(0, 20, 20, 20));

        String[] beneficios = {"→ 10% de desconto em todos os ingressos", "→ Acumule pontos e troque por combos de pipoca", "→ Acesso antecipado a pré-estreias exclusivas"};
        for (String texto : beneficios) {
            Label beneficioLabel = new Label(texto);
            beneficioLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 22));
            beneficioLabel.setTextFill(Color.WHITE);
            centerContent.getChildren().add(beneficioLabel);
        }

        // Lógica para verificar o status do membro fidelidade
        String userId = AuthService.getCurrentUserId();
        boolean isMembroFidelidade = false;
        if (userId != null) {
            JsonObject userDetails = AuthService.getUserDetails(userId);
            if (userDetails != null && userDetails.has("membro_fidelidade")) {
                isMembroFidelidade = userDetails.get("membro_fidelidade").getAsBoolean();
            }
        }

        if (isMembroFidelidade) {
            Label membroStatusLabel = new Label("Você já faz parte do Clube de Fidelidade!");
            membroStatusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
            membroStatusLabel.setTextFill(Color.web("#28a745")); // Verde para indicar sucesso
            VBox.setMargin(membroStatusLabel, new Insets(20, 0, 0, 0));
            centerContent.getChildren().add(membroStatusLabel);
        } else {
            Button btnAssinar = new Button("Assinar clube");
            btnAssinar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            btnAssinar.setTextFill(Color.WHITE);
            btnAssinar.setBackground(new Background(new BackgroundFill(Color.web("#28a745"), new CornerRadii(20), Insets.EMPTY)));
            btnAssinar.setPadding(new Insets(10, 25, 10, 25));
            
            DropShadow blueGlow = new DropShadow(20, Color.rgb(0, 191, 255, 0.8));
            blueGlow.setSpread(0.5);
            btnAssinar.setOnMousePressed(event -> btnAssinar.setEffect(blueGlow));
            btnAssinar.setOnMouseReleased(event -> btnAssinar.setEffect(null));
            
            VBox.setMargin(btnAssinar, new Insets(20, 0, 0, 0)); 
            centerContent.getChildren().add(btnAssinar);
            
            btnAssinar.setOnAction(event -> {
                if (onNavigateToPayment != null) {
                    onNavigateToPayment.accept(new TelaPagamentoCartao(onNavigateToPayment).getView());
                }
            });
        }
        
        viewLayout.setCenter(centerContent);

        return viewLayout;
    }
}