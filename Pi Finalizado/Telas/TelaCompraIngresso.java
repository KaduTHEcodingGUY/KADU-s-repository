package Telas;

import Backend.Servicos.PecaService;
import Backend.Servicos.PecaService.Peca;
import Backend.Servicos.PecaService.Sessao;
import Interfaces.ProvedorView;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TelaCompraIngresso implements ProvedorView {

    private ImageView bannerSelecionado = null;
    private String pecaIdSelecionada = null;
    private String sessaoIdSelecionada = null;

    private final double TAMANHO_NORMAL = 1.0;
    private final double TAMANHO_EXPANDIDO = 1.1;

    private StackPane mainLayout;
    private Pane overlayBackground;
    private VBox horarioSelectionContainer;
    private Button btnContinuar;
    private HBox bannersContainer;

    // Novo campo para o callback de troca de view
    private java.util.function.BiConsumer<Node, Boolean> viewSwitcher;

    // Construtor padrão (mantenha se ainda for usado, mas priorize o novo)
    public TelaCompraIngresso() {
        this(null); // Chama o novo construtor com um Consumer nulo
    }

    // Novo construtor para receber a função de troca de view
    public TelaCompraIngresso(java.util.function.BiConsumer<Node, Boolean> viewSwitcher) {
        this.viewSwitcher = viewSwitcher;
    }

    @Override
    public Node getView() {
        mainLayout = new StackPane();
        BorderPane viewLayout = createMainContent();
        
        criarOverlaySelecaoHorario();

        mainLayout.getChildren().addAll(viewLayout, overlayBackground, horarioSelectionContainer);
        return mainLayout;
    }

    private BorderPane createMainContent() {
        BorderPane viewLayout = new BorderPane();
        Image imagemFundo = new Image(getClass().getResourceAsStream("/Utilitarios/Banner1.png"));
        BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, false, true);
        BackgroundImage backgroundImage = new BackgroundImage(imagemFundo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
        viewLayout.setBackground(new Background(backgroundImage));

        bannersContainer = new HBox(30);
        bannersContainer.setAlignment(Pos.CENTER);
        
        // Carrega as peças do banco de dados
        List<Peca> pecas = PecaService.listarPecas();
        System.out.println("Número de peças carregadas: " + pecas.size());
        for (Peca peca : pecas) {
            System.out.println("Carregando peça: " + peca.getTitulo());
            System.out.println("URL da imagem: " + peca.getUrlImagem());
            ImageView banner = createBanner(peca.getUrlImagem(), peca.getTitulo());
            banner.setUserData(peca.getId()); // Armazena o ID da peça
            bannersContainer.getChildren().add(banner);
        }
        
        viewLayout.setCenter(bannersContainer);

        Button btnComprar = new Button("Comprar Ingresso");
        btnComprar.setDisable(true);
        btnComprar.setCursor(Cursor.HAND);
        btnComprar.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25; -fx-padding: 10px 25px;");
        
        btnComprar.setOnAction(e -> {
            if (pecaIdSelecionada != null) {
                mostrarSelecaoHorario();
            }
        });

        HBox buttonContainer = new HBox(btnComprar);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(0, 0, 40, 0));
        viewLayout.setBottom(buttonContainer);

        // Adiciona os listeners para todos os banners
        for (Node node : bannersContainer.getChildren()) {
            if (node instanceof ImageView) {
                addSelectionListener((ImageView) node, btnComprar);
            }
        }

        return viewLayout;
    }
    
    private void criarOverlaySelecaoHorario() {
        overlayBackground = new Pane();
        overlayBackground.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlayBackground.setOnMouseClicked(event -> ocultarSelecaoHorario());

        horarioSelectionContainer = new VBox(25);
        horarioSelectionContainer.setAlignment(Pos.CENTER);
        horarioSelectionContainer.setStyle("-fx-background-color: #2D2D2D; -fx-padding: 40px; -fx-background-radius: 10px;");
        horarioSelectionContainer.setMaxSize(400, 300);

        Label titulo = new Label("Selecione um Horário");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titulo.setTextFill(Color.WHITE);
        VBox.setMargin(titulo, new Insets(0, 0, 10, 0));

        HBox choiceChips = new HBox(15);
        choiceChips.setAlignment(Pos.CENTER);
        
        btnContinuar = new Button("Continuar");
        btnContinuar.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25; -fx-padding: 10px 25px;");
        btnContinuar.setDisable(true);
        
        btnContinuar.setOnAction(e -> {
            if (sessaoIdSelecionada != null) {
                System.out.println("Navegando para o Mapa de Assentos...");
                System.out.println("Peça ID: " + pecaIdSelecionada + ", Sessão ID: " + sessaoIdSelecionada);

                MapaAssentos mapaView = new MapaAssentos(() -> {
                    if (viewSwitcher != null) {
                        viewSwitcher.accept(this.getView(), true);
                    }
                }, sessaoIdSelecionada, viewSwitcher);
                
                if (viewSwitcher != null) {
                    viewSwitcher.accept(mapaView.getView(), false);
                }
            }
        });
        
        horarioSelectionContainer.getChildren().addAll(titulo, choiceChips, btnContinuar);
        
        ocultarSelecaoHorario();
    }

    private Button criarChoiceChip(String horario) {
        Button chip = new Button(horario);
        String styleNormal = "-fx-background-color: #555555; -fx-text-fill: #E0E0E0; -fx-background-radius: 20px; -fx-padding: 8px 20px; -fx-font-weight: bold; -fx-font-size: 14px;";
        String styleSelecionado = "-fx-background-color:rgb(192, 20, 20); -fx-text-fill: white; -fx-background-radius: 20px; -fx-padding: 8px 20px; -fx-font-weight: bold; -fx-font-size: 14px;";
        
        chip.setStyle(styleNormal);
        chip.setCursor(Cursor.HAND);

        chip.setOnAction(e -> {
            HBox parent = (HBox) chip.getParent();
            for (Node node : parent.getChildren()) {
                node.setStyle(styleNormal);
            }
            chip.setStyle(styleSelecionado);
            sessaoIdSelecionada = (String) chip.getUserData();
            btnContinuar.setDisable(false);
        });
        return chip;
    }
    
    private void addSelectionListener(ImageView banner, Button buyButton) {
        banner.setOnMouseClicked(event -> {
            if (bannerSelecionado != null && bannerSelecionado != banner) {
                animateBanner(bannerSelecionado, TAMANHO_NORMAL);
            }
            animateBanner(banner, TAMANHO_EXPANDIDO);
            
            bannerSelecionado = banner;
            pecaIdSelecionada = (String) banner.getUserData();
            
            buyButton.setDisable(false);
        });
    }
    
    private void ocultarSelecaoHorario() {
        overlayBackground.setVisible(false);
        horarioSelectionContainer.setVisible(false);
    }
    
    private ImageView createBanner(String imagePath, String nomeDaPeca) {
        Image bannerImage;
        try {
            System.out.println("[DEBUG TelaCompraIngresso] Tentando carregar imagem: " + imagePath);
            
            // Verifica se a URL é válida
            if (imagePath == null || imagePath.trim().isEmpty()) {
                System.err.println("[ERRO TelaCompraIngresso] URL da imagem é nula ou vazia");
                bannerImage = new Image(getClass().getResourceAsStream("/Utilitarios/Banner1.png"));
            } else {
                // Se a URL começa com http, carrega da web
                if (imagePath.startsWith("http")) {
                    System.out.println("[DEBUG TelaCompraIngresso] Carregando imagem da web: " + imagePath);
                    bannerImage = new Image(imagePath, true); // true para carregamento assíncrono
                } else {
                    // Se não, tenta carregar do recurso local
                    System.out.println("[DEBUG TelaCompraIngresso] Tentando carregar do recurso local: " + imagePath);
                    bannerImage = new Image(getClass().getResourceAsStream(imagePath));
                }
            }
        } catch (Exception e) {
            // Em caso de erro, carrega uma imagem padrão
            System.err.println("[ERRO TelaCompraIngresso] Erro ao carregar imagem: " + imagePath);
            System.err.println("[ERRO TelaCompraIngresso] Erro: " + e.getMessage());
            bannerImage = new Image(getClass().getResourceAsStream("/Utilitarios/Banner1.png"));
        }

        ImageView bannerView = new ImageView(bannerImage);
        bannerView.setFitWidth(300);
        bannerView.setFitHeight(400);
        bannerView.setPreserveRatio(true);
        
        // Adiciona um listener para quando a imagem terminar de carregar
        bannerView.imageProperty().addListener((obs, oldImage, newImage) -> {
            if (newImage != null && !newImage.isError()) {
                System.out.println("[DEBUG TelaCompraIngresso] Imagem carregada com sucesso: " + imagePath);
            } else if (newImage != null && newImage.isError()) {
                System.err.println("[ERRO TelaCompraIngresso] Erro ao carregar imagem: " + imagePath);
                // Tenta carregar a imagem padrão em caso de erro
                bannerView.setImage(new Image(getClass().getResourceAsStream("/Utilitarios/Banner1.png")));
            }
        });
        
        Rectangle clip = new Rectangle(300, 400);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        bannerView.setClip(clip);
        
        DropShadow dropShadow = new DropShadow(10, Color.BLACK);
        bannerView.setEffect(dropShadow);
        
        bannerView.setUserData(nomeDaPeca);
        return bannerView;
    }
    
    private void animateBanner(ImageView banner, double targetScale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), banner);
        st.setToX(targetScale);
        st.setToY(targetScale);
        st.play();
    }
    
    private void mostrarSelecaoHorario() {
        overlayBackground.setVisible(true);
        horarioSelectionContainer.setVisible(true);
        
        // Recarrega as sessões cada vez que a sobreposição é mostrada
        if (pecaIdSelecionada != null) {
            HBox choiceChips = (HBox) horarioSelectionContainer.getChildren().get(1); // Pega a HBox dos chips
            choiceChips.getChildren().clear(); // Limpa os chips existentes
            
            List<Sessao> sessoes = PecaService.listarSessoesPorPeca(pecaIdSelecionada);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            
            if (sessoes.isEmpty()) {
                Label noSessionsLabel = new Label("Nenhum horário disponível para esta peça.");
                noSessionsLabel.setTextFill(Color.LIGHTGRAY);
                choiceChips.getChildren().add(noSessionsLabel);
            } else {
                for (Sessao sessao : sessoes) {
                    Button chip = criarChoiceChip(sessao.getDataHoraInicio().format(formatter));
                    chip.setUserData(sessao.getId()); // Armazena o ID da sessão
                    choiceChips.getChildren().add(chip);
                }
            }
            btnContinuar.setDisable(true); // Desabilita o botão até uma nova sessão ser selecionada
            sessaoIdSelecionada = null; // Limpa a sessão selecionada anteriormente
        }
    }
}