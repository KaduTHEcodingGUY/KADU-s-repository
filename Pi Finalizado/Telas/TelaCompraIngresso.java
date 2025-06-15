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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TelaCompraIngresso implements ProvedorView {

    private Node bannerSelecionado = null;
    private String pecaIdSelecionada = null;
    private String sessaoIdSelecionada = null;

    private final double TAMANHO_NORMAL = 1.0;
    private final double TAMANHO_EXPANDIDO = 1.1;

    private StackPane mainLayout;
    private Pane overlayBackground;
    private VBox horarioSelectionContainer;
    private Button btnContinuar;
    private HBox bannersContainer;

    private java.util.function.BiConsumer<Node, Boolean> viewSwitcher;

    public TelaCompraIngresso() {
        this(null);
    }

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
        Image imagemFundo = new Image(getClass().getResourceAsStream("/Imagens/BG1BR.png"));
        BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, false, true);
        BackgroundImage backgroundImage = new BackgroundImage(imagemFundo, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
        viewLayout.setBackground(new Background(backgroundImage));

        bannersContainer = new HBox(40);
        bannersContainer.setAlignment(Pos.CENTER);
        bannersContainer.setPadding(new Insets(20));
        
        // --- LÓGICA ATUALIZADA PARA BUSCAR AS PRÓXIMAS SESSÕES ---
        List<Peca> pecas = PecaService.listarPecas();
        for (Peca peca : pecas) {
            List<Sessao> sessoes = PecaService.listarSessoesPorPeca(peca.getId());
            
            // Filtra e ordena para pegar todas as sessões futuras
            List<Sessao> sessoesFuturas = sessoes.stream()
                .filter(s -> s.getDataHoraInicio().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Sessao::getDataHoraInicio))
                .collect(Collectors.toList());

            // O método createBanner agora recebe a Peça e a LISTA de sessões futuras
            VBox bannerComCabecalho = createBanner(peca, sessoesFuturas);
            
            bannerComCabecalho.setUserData(peca.getId());
            bannersContainer.getChildren().add(bannerComCabecalho);
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

        for (Node node : bannersContainer.getChildren()) {
            addSelectionListener(node, btnComprar);
        }

        return viewLayout;
    }
    
    // --- MÉTODO CREATEBANNER TOTALMENTE REFEITO ---
    private VBox createBanner(Peca peca, List<Sessao> sessoesFuturas) {
        // 1. Contêiner principal (VBox)
        VBox bannerVBox = new VBox();
        bannerVBox.setCursor(Cursor.HAND);
        // REMOVIDO: setPrefSize para corrigir o espaçamento extra. O tamanho agora é natural.

        // 2. Criação do Cabeçalho (agora um VBox para suportar múltiplas linhas)
        VBox cabecalho = new VBox(5); // 5px de espaçamento entre as linhas de sessão
        cabecalho.setPrefHeight(80);   // Altura fixa para alinhar todos os banners
        cabecalho.setAlignment(Pos.CENTER);
        cabecalho.setPadding(new Insets(5));

        // Define a cor do cabeçalho
        String corFundoCabecalho = "#DC3545"; // Vermelho padrão
        if (peca.getTitulo().toLowerCase().contains("michael jackson")) {
            corFundoCabecalho = "#404040"; // Cinza para o MJ
        }
        cabecalho.setStyle("-fx-background-color: " + corFundoCabecalho + ";");

        // 3. Adiciona os textos das sessões ao cabeçalho
        if (sessoesFuturas.isEmpty()) {
            Label infoSessaoLabel = new Label("Nenhuma sessão futura");
            infoSessaoLabel.setTextFill(Color.WHITE);
            infoSessaoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            cabecalho.getChildren().add(infoSessaoLabel);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
            // Pega até as duas próximas sessões
            sessoesFuturas.stream().limit(2).forEach(sessao -> {
                Label infoSessaoLabel = new Label(sessao.getDataHoraInicio().format(formatter));
                infoSessaoLabel.setTextFill(Color.WHITE);
                infoSessaoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
                cabecalho.getChildren().add(infoSessaoLabel);
            });
        }

        // 4. Criação da Imagem do Pôster
        Image bannerImage;
        try {
            bannerImage = new Image(peca.getUrlImagem(), true);
        } catch (Exception e) {
            bannerImage = new Image(getClass().getResourceAsStream("/Imagens/Banner1.png"));
        }
        
        ImageView bannerView = new ImageView(bannerImage);
        bannerView.setFitWidth(300);
        bannerView.setFitHeight(450); // Altura da imagem ajustada (550 total - 80 do cabeçalho)
        bannerView.setPreserveRatio(false);

        // 5. Adiciona o cabeçalho e a imagem ao VBox principal
        bannerVBox.getChildren().addAll(cabecalho, bannerView);
        
        // 6. Aplica o arredondamento e sombra ao VBox completo
        bannerVBox.setStyle("-fx-background-radius: 30; -fx-background-color: black;");
        DropShadow dropShadow = new DropShadow(1, Color.BLACK);
        bannerVBox.setEffect(dropShadow);
        
        return bannerVBox;
    }

    private void addSelectionListener(Node banner, Button buyButton) {
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
    
    private void animateBanner(Node banner, double targetScale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), banner);
        st.setToX(targetScale);
        st.setToY(targetScale);
        st.play();
    }
    
    // ... (Restante da classe como criarOverlaySelecaoHorario, etc., permanece o mesmo) ...
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
        btnContinuar.setCursor(Cursor.HAND);
        btnContinuar.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25; -fx-padding: 10px 25px;");
        btnContinuar.setDisable(true);
        
        btnContinuar.setOnAction(e -> {
            if (sessaoIdSelecionada != null) {
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
    
    private void ocultarSelecaoHorario() {
        overlayBackground.setVisible(false);
        horarioSelectionContainer.setVisible(false);
    }
    
    private void mostrarSelecaoHorario() {
        overlayBackground.setVisible(true);
        horarioSelectionContainer.setVisible(true);
        
        if (pecaIdSelecionada != null) {
            HBox choiceChips = (HBox) horarioSelectionContainer.getChildren().get(1);
            choiceChips.getChildren().clear();
            
            List<Sessao> sessoes = PecaService.listarSessoesPorPeca(pecaIdSelecionada);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            
            if (sessoes.isEmpty()) {
                Label noSessionsLabel = new Label("Nenhum horário disponível para esta peça.");
                noSessionsLabel.setTextFill(Color.LIGHTGRAY);
                choiceChips.getChildren().add(noSessionsLabel);
            } else {
                for (Sessao sessao : sessoes) {
                    Button chip = criarChoiceChip(sessao.getDataHoraInicio().format(formatter));
                    chip.setUserData(sessao.getId());
                    choiceChips.getChildren().add(chip);
                }
            }
            btnContinuar.setDisable(true);
            sessaoIdSelecionada = null;
        }
    }
}