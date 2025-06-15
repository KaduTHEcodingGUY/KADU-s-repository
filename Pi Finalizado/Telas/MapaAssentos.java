package Telas; // Ou o seu pacote de views

import Interfaces.ProvedorView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import Backend.Servicos.AssentoService;
import Backend.Servicos.AssentoService.AreaAssento;
import Backend.Servicos.AssentoService.Assento;
import Backend.Servicos.AssentoService.AssentoReservado;

// MODIFICADO: O nome da classe foi alterado para MapaAssentos
public class MapaAssentos implements ProvedorView {

    // MODIFICADO: Adicionado campo id ao record Setor
    private record Setor(String id, String nome, double preco) {}

    private BorderPane rootLayout;
    private Button btnComprar;
    private List<ToggleButton> assentosSelecionados = new ArrayList<>();
    
    private VBox setorSelecionadoBox = null;
    private Runnable onBackAction;
    private String sessaoId;
    // NOVO: Adicionado viewSwitcher para navegação.
    private java.util.function.BiConsumer<Node, Boolean> viewSwitcher;

    private final String STYLE_ASSENTO_DISPONIVEL = "-fx-background-color: #555555; -fx-background-radius: 3px;";
    private final String STYLE_ASSENTO_SELECIONADO = "-fx-background-color: #DC3545; -fx-background-radius: 3px;";
    
    private final String STYLE_SETOR_NORMAL = "-fx-background-color: transparent; -fx-padding: 8px; -fx-border-color: transparent;";
    private final String STYLE_SETOR_HOVER = "-fx-background-color: #404040; -fx-padding: 8px; -fx-background-radius: 5px;";
    private final String STYLE_SETOR_SELECIONADO = "-fx-background-color: #005A9E; -fx-padding: 8px; -fx-background-radius: 5px;";
    private final String STYLE_ASSENTO_RESERVADO = "-fx-background-color: #8B0000; -fx-background-radius: 3px;"; // NOVO: Estilo para assentos reservados

    // MODIFICADO: Construtor padrão que chama o novo construtor com sessaoId e viewSwitcher nulos
    public MapaAssentos() {
        this(null, null, null);
    }

    // MODIFICADO: Construtor para receber a ação de retorno, o sessaoId e o viewSwitcher
    public MapaAssentos(Runnable onBackAction, String sessaoId, java.util.function.BiConsumer<Node, Boolean> viewSwitcher) {
        System.out.println("[DEBUG MapaAssentos] Construtor MapaAssentos chamado com sessaoId: " + sessaoId);
        this.onBackAction = onBackAction;
        this.sessaoId = sessaoId; // Inicializa o sessaoId
        this.viewSwitcher = viewSwitcher; // Inicializa o viewSwitcher
    }

    @Override
    public Node getView() {
        System.out.println("[DEBUG MapaAssentos] getView() chamado.");
        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: black;");

        VBox sidebar = createSidebar(this);
        rootLayout.setLeft(sidebar);

        btnComprar = new Button("Efetuar Compra");
        btnComprar.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25; -fx-padding: 10px 25px;");
        btnComprar.setOnAction(e -> {
            System.out.println("[DEBUG MapaAssentos] Botão Efetuar Compra clicado.");
            if (assentosSelecionados.isEmpty()) {
                System.out.println("[DEBUG MapaAssentos] Nenhum assento selecionado.");
                // Adicionar um alerta ou mensagem para o usuário aqui
                return;
            }
            
            List<String> assentoIds = new ArrayList<>();
            for (ToggleButton seat : assentosSelecionados) {
                assentoIds.add((String) seat.getUserData());
            }
            System.out.println("[DEBUG MapaAssentos] Assentos selecionados (IDs): " + assentoIds);

            if (viewSwitcher != null) {
                System.out.println("[DEBUG MapaAssentos] Navegando para TelaResumoCompra...");
                TelaResumoCompra resumoView = new TelaResumoCompra(sessaoId, assentoIds, viewSwitcher);
                viewSwitcher.accept(resumoView.getView(), false); // Ocultar sidebar para o resumo
            } else {
                System.err.println("[ERRO MapaAssentos] viewSwitcher é nulo. Não é possível navegar para o resumo da compra.");
            }
        });
        
        updateMap(null); // MODIFICADO: Passa null para exibir o mapa geral inicialmente

        return rootLayout;
    }

    private VBox createSidebar(MapaAssentos viewManager) {
        System.out.println("[DEBUG MapaAssentos] createSidebar() chamado.");
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #2D2D2D;");
        sidebar.setAlignment(Pos.TOP_LEFT);
        
        Button btnVoltar = new Button("← Voltar para seleção");
        btnVoltar.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnVoltar.setCursor(Cursor.HAND);
        btnVoltar.setOnAction(e -> {
            System.out.println("[DEBUG MapaAssentos] Botão Voltar para seleção clicado.");
            if (onBackAction != null) {
                onBackAction.run();
            } else {
                System.out.println("[DEBUG MapaAssentos] Ação de voltar clicada (sem callback definido).");
            }
        });
        
        VBox mapaTeatroBox = createSetorOption(null, "Mapa Teatro", "Visão geral", viewManager); // MODIFICADO: id nulo para Mapa Teatro

        // MODIFICADO: Busca áreas de assentos do serviço
        List<AssentoService.AreaAssento> areasBanco = AssentoService.listarAreasAssentos();
        System.out.println("[DEBUG MapaAssentos] Áreas de assentos carregadas do banco: " + areasBanco.size());

        // Converte AreaAssento para Setor
        List<Setor> setores = new ArrayList<>();
        for (AreaAssento area : areasBanco) {
            setores.add(new Setor(area.getId(), area.getNomeArea(), area.getPreco()));
        }

        setores.sort(Comparator.comparingDouble(Setor::preco));

        sidebar.getChildren().add(btnVoltar);
        sidebar.getChildren().add(mapaTeatroBox);

        for (Setor setor : setores) {
            // MODIFICADO: Passa o ID do setor para createSetorOption
            VBox setorBox = createSetorOption(setor.id(), setor.nome(), String.format("R$ %.2f", setor.preco()), viewManager);
            sidebar.getChildren().add(setorBox);
        }

        setorSelecionadoBox = mapaTeatroBox;
        setorSelecionadoBox.setStyle(STYLE_SETOR_SELECIONADO);

        return sidebar;
    }

    private VBox createSetorOption(String id, String nome, String preco, MapaAssentos viewManager) {
        System.out.println("[DEBUG MapaAssentos] createSetorOption() chamado para: " + nome);
        VBox container = new VBox(-2);
        container.setCursor(Cursor.HAND);
        
        // Define um user data para o ID da área, para fácil acesso no clique
        container.setUserData(id);

        Label nomeLabel = new Label(nome);
        nomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        nomeLabel.setTextFill(Color.WHITE);

        Label precoLabel = new Label(preco);
        precoLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        precoLabel.setTextFill(Color.LIGHTGRAY);

        container.getChildren().addAll(nomeLabel, precoLabel);
        container.setStyle(STYLE_SETOR_NORMAL);

        container.setOnMouseEntered(e -> {
            if (container != setorSelecionadoBox) {
                container.setStyle(STYLE_SETOR_HOVER);
            }
        });
        container.setOnMouseExited(e -> {
            if (container != setorSelecionadoBox) {
                container.setStyle(STYLE_SETOR_NORMAL);
            }
        });
        container.setOnMouseClicked(e -> {
            System.out.println("[DEBUG MapaAssentos] Setor clicado: " + nome + " (ID: " + id + ").");
            if (setorSelecionadoBox != null) {
                setorSelecionadoBox.setStyle(STYLE_SETOR_NORMAL);
            }
            setorSelecionadoBox = container;
            setorSelecionadoBox.setStyle(STYLE_SETOR_SELECIONADO);
            viewManager.updateMap((String) container.getUserData()); // MODIFICADO: Passa o ID da área do user data
        });

        return container;
    }
    
    // MODIFICADO: Agora aceita areaId e carrega assentos do banco
    public void updateMap(String areaId) {
        System.out.println("[DEBUG MapaAssentos] updateMap() chamado para areaId: " + areaId);
        assentosSelecionados.clear();

        GridPane seatMap;
        List<Assento> assentosDaArea = new ArrayList<>();
        List<AssentoReservado> assentosReservadosSessao = new ArrayList<>();

        if (areaId == null) { // Caso seja "Mapa Teatro"
            System.out.println("[DEBUG MapaAssentos] Exibindo mapa geral do teatro.");
            seatMap = createSeatGrid(areaId, assentosDaArea, assentosReservadosSessao); // Passa listas vazias
            btnComprar.setVisible(false); // Oculta o botão de comprar no mapa geral
        } else {
            System.out.println("[DEBUG MapaAssentos] Carregando assentos para a área: " + areaId);
            assentosDaArea = AssentoService.listarAssentosPorArea(areaId);
            System.out.println("[DEBUG MapaAssentos] Assentos carregados para área " + areaId + ": " + assentosDaArea.size());

            if (sessaoId != null) {
                assentosReservadosSessao = AssentoService.listarAssentosReservados(sessaoId);
                System.out.println("[DEBUG MapaAssentos] Assentos reservados para a sessão " + sessaoId + ": " + assentosReservadosSessao.size());
            } else {
                System.out.println("[DEBUG MapaAssentos] sessaoId é nulo, não buscando assentos reservados.");
            }

            seatMap = createSeatGrid(areaId, assentosDaArea, assentosReservadosSessao);
            btnComprar.setVisible(true); // Exibe o botão de comprar para áreas específicas
        }

        VBox centerContainer = new VBox(20);
        centerContainer.setAlignment(Pos.CENTER);
        centerContainer.getChildren().addAll(seatMap, btnComprar); // Adiciona o botão de comprar aqui
        
        rootLayout.setCenter(centerContainer);
    }
    
    // MODIFICADO: Agora aceita listas de assentos e assentos reservados
    private GridPane createSeatGrid(String areaId, List<Assento> assentosDaArea, List<AssentoReservado> assentosReservadosSessao) {
        System.out.println("[DEBUG MapaAssentos] createSeatGrid() chamado para areaId: " + areaId + ", assentos: " + assentosDaArea.size() + ", reservados: " + assentosReservadosSessao.size());
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);

        if (areaId == null) { // Se for o mapa geral, exibe a imagem
            System.out.println("[DEBUG MapaAssentos] Exibindo imagem do mapa geral.");
            try {
                Image image = new Image("file:C:/Users/norma/Downloads/SappensV2Git/TeatroABC/TeatroABC/TeatroABC-main/TeatroABC-main/Pi Finalizado/Utilitarios/PALCO.png");
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(600); // Ajuste o tamanho conforme necessário
                imageView.setPreserveRatio(true);
                grid.add(imageView, 0, 0, 12, 12); // Adiciona a imagem ao grid
            } catch (Exception e) {
                System.err.println("[ERRO MapaAssentos] Erro ao carregar a imagem do mapa do teatro: " + e.getMessage());
                // Fallback para a tela e label existentes, caso a imagem não carregue
        Arc telaShape = new Arc(0, 0, 200, 50, 0, -180);
        telaShape.setType(ArcType.OPEN);
        telaShape.setStroke(Color.WHITE);
        telaShape.setStrokeWidth(2);
        telaShape.setFill(Color.TRANSPARENT);
        Label telaLabel = new Label("TELA");
        telaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        telaLabel.setTextFill(Color.WHITE);
        StackPane telaContainer = new StackPane(telaShape, telaLabel);
        grid.add(telaContainer, 0, 0, 10, 1);
            }
        } else { // Se for uma área específica, exibe os assentos
            System.out.println("[DEBUG MapaAssentos] Exibindo assentos para a área: " + areaId);
            Arc telaShape = new Arc(0, 0, 200, 50, 0, -180);
            telaShape.setType(ArcType.OPEN);
            telaShape.setStroke(Color.WHITE);
            telaShape.setStrokeWidth(2);
            telaShape.setFill(Color.TRANSPARENT);
            Label telaLabel = new Label("TELA");
            telaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            telaLabel.setTextFill(Color.WHITE);
            StackPane telaContainer = new StackPane(telaShape, telaLabel);
            grid.add(telaContainer, 0, 0, 10, 1); // A "TELA" sempre aparece

            // Mapeamento de fileiras para índices de linha (ajuste conforme necessário)
            // Assumindo que as fileiras são letras e queremos uma ordem alfabética
            List<String> fileirasOrdenadas = assentosDaArea.stream()
                                                    .map(Assento::getFileira)
                                                    .distinct()
                                                    .sorted()
                                                    .collect(java.util.stream.Collectors.toList());
            
            // Adicionar mais debug aqui
            System.out.println("[DEBUG MapaAssentos] Fileiras ordenadas: " + fileirasOrdenadas);

            for (Assento assento : assentosDaArea) {
                int col = assento.getNumero() - 1; // Ajuste para índice 0-base
                int row = fileirasOrdenadas.indexOf(assento.getFileira()) + 2; // +2 para compensar a "TELA"

                if (col >= 0 && row >= 2) { // Validação básica de posição
                ToggleButton seat = createSeatButton();
                    seat.setUserData(assento.getId()); // Armazena o ID do assento
                    
                    boolean isReserved = assentosReservadosSessao.stream()
                                                        .anyMatch(r -> r.getAssentoId().equals(assento.getId()));
                    
                    if (isReserved) {
                        System.out.println("[DEBUG MapaAssentos] Assento " + assento.getFileira() + assento.getNumero() + " (ID: " + assento.getId() + ") está RESERVADO.");
                        seat.setStyle(STYLE_ASSENTO_RESERVADO);
                        seat.setDisable(true); // Desabilita assentos reservados
                    } else {
                        System.out.println("[DEBUG MapaAssentos] Assento " + assento.getFileira() + assento.getNumero() + " (ID: " + assento.getId() + ") está DISPONÍVEL.");
                        seat.setStyle(STYLE_ASSENTO_DISPONIVEL);
                    }
                    grid.add(seat, col, row);
                } else {
                    System.err.println("[ERRO MapaAssentos] Posição de assento inválida: Fileira " + assento.getFileira() + ", Número " + assento.getNumero());
                }
            }
        }
        return grid;
    }

    private ToggleButton createSeatButton() {
        ToggleButton seat = new ToggleButton();
        seat.setPrefSize(25, 25);
        seat.setStyle(STYLE_ASSENTO_DISPONIVEL);
        seat.setCursor(Cursor.HAND);
        seat.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            seat.setStyle(isSelected ? STYLE_ASSENTO_SELECIONADO : STYLE_ASSENTO_DISPONIVEL);
            if (isSelected) {
                assentosSelecionados.add(seat);
            } else {
                assentosSelecionados.remove(seat);
            }
        });
        return seat;
    }
}