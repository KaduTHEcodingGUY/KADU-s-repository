package Telas; // Ou seu pacote de views

import Interfaces.ProvedorView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import Backend.Servicos.AuthService;
import Backend.Servicos.IngressoService;
import Backend.Servicos.IngressoService.DetalheIngresso;
import Backend.Servicos.PDFService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MeusIngressos implements ProvedorView {

    public record Ticket(String codigo, String nomePeca, String horarioSessao, String assentos, String imagePath, DetalheIngresso detalheOriginal) {}

    @Override
    public Node getView() {
        BorderPane rootLayout = new BorderPane();

        // --- TOP: A imagem e o título ---
        // MODIFICADO: Não passamos mais o rootLayout, pois a altura será fixa.
        StackPane topSection = createTopSection();
        rootLayout.setTop(topSection);

        // --- CENTER: A ListView com fundo preto ---
        ListView<Ticket> listView = createListView();
        StackPane centerContainer = new StackPane(listView);
        centerContainer.setStyle("-fx-background-color: black;");
        
        // MODIFICADO: Aumentamos a margem lateral para 80px para dar mais respiro.
        StackPane.setMargin(listView, new Insets(20, 80, 20, 80));

        rootLayout.setCenter(centerContainer);

        return rootLayout;
    }

    /**
     * Cria a seção superior com a imagem de fundo e o título.
     */
    private StackPane createTopSection() {
        StackPane topPane = new StackPane();
        // MODIFICADO: Altura preferida do container da imagem diminuída para 200px.
        topPane.setPrefHeight(200);
        topPane.setMaxHeight(200); // Trava a altura máxima também.

        // Imagem de fundo da seção
        ImageView topBanner = new ImageView(new Image(getClass().getResourceAsStream("/Utilitarios/Banner0.png")));
        
        // A lógica de redimensionamento da imagem de fundo é trocada por uma abordagem mais simples e robusta
        // usando um objeto Background, que se comporta melhor com o StackPane.
        BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, false, true); // cover
        BackgroundImage backgroundImage = new BackgroundImage(topBanner.getImage(),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
        topPane.setBackground(new Background(backgroundImage));


        Label titulo = new Label("MEUS INGRESSOS");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titulo.setTextFill(Color.WHITE);
        titulo.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 10, 0.4, 0, 0);"); // Sombra para legibilidade
        
        // MODIFICADO: Removemos a margem para deixar o alinhamento padrão do StackPane centralizar o título.
        StackPane.setAlignment(titulo, Pos.CENTER);

        // Note que não adicionamos mais o topBanner como filho, pois ele agora é o fundo.
        topPane.getChildren().add(titulo);
        
        return topPane;
    }

    /**
     * Cria e configura a ListView, agora carregando dados do banco.
     */
    private ListView<Ticket> createListView() {
        ListView<Ticket> listView = new ListView<>();
        
        // MODIFICADO: Carrega ingressos do banco de dados
        String userId = AuthService.getCurrentUserId(); // Obter o ID do usuário logado
        List<DetalheIngresso> detalhesIngressos = new ArrayList<>();
        if (userId != null) {
            detalhesIngressos = IngressoService.listarIngressosPorUsuario(userId);
            System.out.println("[DEBUG MeusIngressos] Ingressos carregados do DB: " + detalhesIngressos.size());
        } else {
            System.err.println("[ERRO MeusIngressos] Usuário não logado. Não foi possível carregar ingressos.");
        }
        
        ObservableList<Ticket> ingressos = FXCollections.observableArrayList(
            detalhesIngressos.stream()
                .map(detalhe -> new Ticket(
                    detalhe.getCodigoIngresso(),
                    detalhe.getPecaTitulo(),
                    detalhe.getDataHoraSessao().format(DateTimeFormatter.ofPattern("HH:mm 'em' dd/MM/yyyy")), // Formata a data/hora
                    detalhe.getAssentosComprados(),
                    detalhe.getPecaImagemUrl(), // A URL da imagem já vem pronta do serviço
                    detalhe // ADICIONADO: Passa o objeto DetalheIngresso completo
                ))
                .collect(Collectors.toList())
        );
        listView.setItems(ingressos);

        listView.setCellFactory(param -> new TicketCell());
        
        listView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        
        return listView;
    }

    /**
     * Classe interna que define a aparência de cada célula (item) na ListView.
     */
    private static class TicketCell extends ListCell<Ticket> {
        private final HBox container;
        private final ImageView bannerView = new ImageView();
        private final Label codigoLabel = new Label();
        private final Label pecaLabel = new Label();
        private final Label sessaoLabel = new Label();
        private final Label assentosLabel = new Label();
        private DetalheIngresso detalheIngresso;

        public TicketCell() {
            super();
            setBackground(Background.EMPTY); 

            bannerView.setFitHeight(120);
            bannerView.setFitWidth(80);
            Rectangle clip = new Rectangle(80, 120);
            clip.setArcWidth(15);
            clip.setArcHeight(15);
            bannerView.setClip(clip);

            codigoLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 22));
            codigoLabel.setTextFill(Color.WHITE);
            pecaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            pecaLabel.setTextFill(Color.LIGHTGRAY);
            sessaoLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            sessaoLabel.setTextFill(Color.LIGHTGRAY);
            assentosLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            assentosLabel.setTextFill(Color.WHITE);

            VBox infoBox = new VBox(5, codigoLabel, pecaLabel, sessaoLabel);
            infoBox.setAlignment(Pos.CENTER_LEFT);

            BorderPane detailsPane = new BorderPane();
            detailsPane.setLeft(infoBox);
            detailsPane.setBottom(assentosLabel);
            BorderPane.setAlignment(assentosLabel, Pos.CENTER_RIGHT);

            // NOVO: Botão para baixar PDF
            Button btnBaixarPdf = new Button("Baixar PDF");
            btnBaixarPdf.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 5 10;");
            btnBaixarPdf.setOnAction(event -> {
                if (this.detalheIngresso != null) {
                    try {
                        PDFService.gerarIngressoPDF(this.detalheIngresso);
                        
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Download Concluído");
                        alert.setHeaderText("PDF Baixado com Sucesso!");
                        alert.setContentText("O ingresso foi salvo na sua pasta de Downloads.");
                        alert.showAndWait();
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Erro ao Baixar");
                        alert.setHeaderText("Não foi possível baixar o PDF");
                        alert.setContentText("Ocorreu um erro ao gerar o PDF. Por favor, tente novamente.");
                        alert.showAndWait();
                    }
                } else {
                    System.err.println("Erro: Detalhes do ingresso não disponíveis para gerar PDF.");
                    
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Dados do Ingresso Indisponíveis");
                    alert.setContentText("Não foi possível encontrar os dados do ingresso para gerar o PDF.");
                    alert.showAndWait();
                }
            });
            BorderPane.setAlignment(btnBaixarPdf, Pos.BOTTOM_RIGHT);
            detailsPane.setRight(btnBaixarPdf);

            container = new HBox(20, bannerView, detailsPane);
            container.setPadding(new Insets(15));
            container.setAlignment(Pos.CENTER_LEFT);
            container.setStyle("-fx-background-color: #2D2D2D; -fx-background-radius: 10;");
            
            HBox.setHgrow(detailsPane, Priority.ALWAYS);
            setPadding(new Insets(0, 0, 10, 0));
        }

        @Override
        protected void updateItem(Ticket ticket, boolean empty) {
            super.updateItem(ticket, empty);
            if (empty || ticket == null) {
                setGraphic(null);
            } else {
                this.detalheIngresso = ticket.detalheOriginal();

                bannerView.setImage(new Image(ticket.imagePath()));
                codigoLabel.setText(ticket.codigo());
                pecaLabel.setText(ticket.nomePeca());
                sessaoLabel.setText(ticket.horarioSessao());
                assentosLabel.setText("Assentos: " + ticket.assentos());
                setGraphic(container);
            }
        }
    }
}