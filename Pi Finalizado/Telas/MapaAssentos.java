package Telas;

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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import Backend.Servicos.AssentoService;
import Backend.Servicos.AssentoService.AreaAssento;
import Backend.Servicos.AssentoService.Assento;
import Backend.Servicos.AssentoService.AssentoReservado;

public class MapaAssentos implements ProvedorView {

    private record Setor(String id, String nome, double preco) {}

    private BorderPane rootLayout;
    private Button btnComprar;
    private List<String> assentosSelecionadosIds = new ArrayList<>();
    
    private VBox setorSelecionadoBox = null;
    private Runnable onBackAction;
    private String sessaoId;
    private java.util.function.BiConsumer<Node, Boolean> viewSwitcher;

    private String selectedSubCategory;
    private String currentAreaId;
    private String currentAreaName;

    private final String STYLE_ASSENTO_DISPONIVEL = "-fx-background-color: #555555; -fx-background-radius: 3px;";
    private final String STYLE_ASSENTO_SELECIONADO = "-fx-background-color: #DC3545; -fx-background-radius: 3px;";
    private final String STYLE_SETOR_NORMAL = "-fx-background-color: transparent; -fx-padding: 8px; -fx-border-color: transparent;";
    private final String STYLE_SETOR_HOVER = "-fx-background-color: #404040; -fx-padding: 8px; -fx-background-radius: 5px;";
    private final String STYLE_SETOR_SELECIONADO = "-fx-background-color: #005A9E; -fx-padding: 8px; -fx-background-radius: 5px;";
    private final String STYLE_ASSENTO_RESERVADO = "-fx-background-color: #8B0000; -fx-background-radius: 3px;";

    public MapaAssentos(Runnable onBackAction, String sessaoId, java.util.function.BiConsumer<Node, Boolean> viewSwitcher) {
        this.onBackAction = onBackAction;
        this.sessaoId = sessaoId;
        this.viewSwitcher = viewSwitcher;
    }

    @Override
    public Node getView() {
        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: black;");

        VBox sidebar = createSidebar(this);
        rootLayout.setLeft(sidebar);

        btnComprar = new Button("Efetuar Compra");
        btnComprar.setCursor(Cursor.HAND);
        btnComprar.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25; -fx-padding: 10px 25px;");
        btnComprar.setOnAction(e -> {
            if (!assentosSelecionadosIds.isEmpty() && viewSwitcher != null) {
                TelaResumoCompra resumoView = new TelaResumoCompra(sessaoId, assentosSelecionadosIds, viewSwitcher);
                viewSwitcher.accept(resumoView.getView(), false);
            }
        });
        
        updateMap(null, null, null);

        return rootLayout;
    }

    private VBox createSidebar(MapaAssentos viewManager) {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #2D2D2D;");
        sidebar.setAlignment(Pos.TOP_LEFT);
        
        Button btnVoltar = new Button("← Voltar para seleção");
        btnVoltar.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnVoltar.setCursor(Cursor.HAND);
        btnVoltar.setOnAction(e -> {
            if (viewSwitcher != null) {
                TelaCompraIngresso compraView = new TelaCompraIngresso(viewSwitcher);
                viewSwitcher.accept(compraView.getView(), true);
            }
        });
        
        VBox mapaTeatroBox = createSetorOption(null, "Mapa Teatro", "Visão geral", viewManager);

        List<Setor> setores = AssentoService.listarAreasAssentos().stream()
            .map(area -> new Setor(area.getId(), area.getNomeArea(), area.getPreco()))
            .sorted(Comparator.comparingDouble(Setor::preco))
            .collect(Collectors.toList());

        sidebar.getChildren().add(btnVoltar);
        sidebar.getChildren().add(mapaTeatroBox);

        for (Setor setor : setores) {
            sidebar.getChildren().add(createSetorOption(setor.id, setor.nome, String.format("R$ %.2f", setor.preco), viewManager));
        }

        return sidebar;
    }

    private VBox createSetorOption(String id, String nome, String preco, MapaAssentos viewManager) {
        VBox container = new VBox(-2);
        container.setCursor(Cursor.HAND);
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
            if (container != setorSelecionadoBox) container.setStyle(STYLE_SETOR_HOVER);
        });
        container.setOnMouseExited(e -> {
            if (container != setorSelecionadoBox) container.setStyle(STYLE_SETOR_NORMAL);
        });
        container.setOnMouseClicked(e -> {
            updateMap((String) container.getUserData(), null, nome);
        });

        return container;
    }

    public void updateMap(String newAreaId, String newSubCategory, String areaName) {
        if (newAreaId == null) {
            assentosSelecionadosIds.clear();
            this.selectedSubCategory = null;
        }

        this.currentAreaId = newAreaId;
        this.currentAreaName = areaName;
        this.selectedSubCategory = newSubCategory;

        if (("Camarote".equalsIgnoreCase(areaName) || "Frisa".equalsIgnoreCase(areaName)) && selectedSubCategory == null) {
            selectedSubCategory = "Camarote".equalsIgnoreCase(areaName) ? "C1" : "F1";
        }
        
        // --- LAYOUT CENTRAL REORGANIZADO ---
        VBox mapAreaContainer = new VBox(20);
        mapAreaContainer.setAlignment(Pos.CENTER);
        mapAreaContainer.setPadding(new Insets(20));

        // 1. Imagem do Palco sempre no topo e centralizada
        ImageView palcoView = new ImageView(new Image(getClass().getResourceAsStream("/Imagens/palco.png")));
        palcoView.setFitWidth(600);
        palcoView.setPreserveRatio(true);
        mapAreaContainer.getChildren().add(palcoView);

        if (currentAreaId == null) {
            btnComprar.setVisible(false);
        } else {
            // 2. Adiciona os seletores de subcategoria (C1, F1, etc.), se necessário
            boolean shouldShowChips = "Camarote".equalsIgnoreCase(currentAreaName) || "Frisa".equalsIgnoreCase(currentAreaName);
            if (shouldShowChips) {
                HBox chipContainer = createSubCategoryChips(currentAreaName);
                mapAreaContainer.getChildren().add(chipContainer);
            }

            // 3. Adiciona a grade de assentos interativos
            List<Assento> assentosDaArea = AssentoService.listarAssentosPorArea(currentAreaId);
            List<AssentoReservado> assentosReservadosSessao = (sessaoId != null) ? AssentoService.listarAssentosReservados(sessaoId) : new ArrayList<>();
            GridPane seatGrid = createSeatGrid(assentosDaArea, assentosReservadosSessao);
            mapAreaContainer.getChildren().add(seatGrid);

            // 4. Adiciona o botão de compra
            btnComprar.setVisible(true);
            mapAreaContainer.getChildren().add(btnComprar);
        }

        rootLayout.setCenter(mapAreaContainer);
    }
    
    private HBox createSubCategoryChips(String areaType) {
        HBox chipsBox = new HBox(10);
        chipsBox.setAlignment(Pos.CENTER);
        chipsBox.setPadding(new Insets(10));

        List<String> subCategories = new ArrayList<>();
        if ("Camarote".equalsIgnoreCase(areaType)) {
            for (int i = 1; i <= 5; i++) subCategories.add("C" + i);
        } else if ("Frisa".equalsIgnoreCase(areaType)) {
            for (int i = 1; i <= 6; i++) subCategories.add("F" + i);
        }

        for (String sub : subCategories) {
            ToggleButton chip = new ToggleButton(sub);
            chip.setUserData(sub);
            chip.setStyle("-fx-background-color: #444444; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");

            if (sub.equals(selectedSubCategory)) {
                chip.setSelected(true);
                chip.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
            }

            chip.setOnAction(event -> {
                if (chip.isSelected()) {
                    updateMap(currentAreaId, sub, currentAreaName);
                } else {
                    chip.setSelected(true); // Impede a desseleção
                }
            });
            chipsBox.getChildren().add(chip);
        }
        return chipsBox;
    }

    private GridPane createSeatGrid(List<Assento> assentosDaArea, List<AssentoReservado> assentosReservadosSessao) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);

        // A lógica do "palco" ou "tela" foi movida para o updateMap
        
        List<Assento> assentosFiltrados;
        boolean shouldFilter = selectedSubCategory != null && ("Camarote".equalsIgnoreCase(currentAreaName) || "Frisa".equalsIgnoreCase(currentAreaName));
        
        if (shouldFilter) {
            assentosFiltrados = assentosDaArea.stream()
                .filter(a -> selectedSubCategory.equalsIgnoreCase(a.getFileira()))
                .collect(Collectors.toList());
        } else {
            assentosFiltrados = assentosDaArea;
        }

        List<String> fileirasOrdenadas = assentosFiltrados.stream()
            .map(Assento::getFileira)
            .distinct().sorted()
            .collect(Collectors.toList());

        for (Assento assento : assentosFiltrados) {
            int col = assento.getNumero() - 1;
            int row = fileirasOrdenadas.indexOf(assento.getFileira());

            ToggleButton seat = createSeatButton();
            seat.setUserData(assento.getId());
            
            boolean isAlreadySelected = assentosSelecionadosIds.contains(assento.getId());
            boolean isReserved = assentosReservadosSessao.stream().anyMatch(r -> r.getAssentoId().equals(assento.getId()));
            
            if (isReserved) {
                seat.setStyle(STYLE_ASSENTO_RESERVADO);
                seat.setDisable(true);
            } else {
                seat.setSelected(isAlreadySelected);
                seat.setStyle(isAlreadySelected ? STYLE_ASSENTO_SELECIONADO : STYLE_ASSENTO_DISPONIVEL);
            }
            grid.add(seat, col, row);
        }
        return grid;
    }

    private ToggleButton createSeatButton() {
        ToggleButton seat = new ToggleButton();
        seat.setPrefSize(25, 25);
        seat.setCursor(Cursor.HAND);
        
        seat.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            String assentoId = (String) seat.getUserData();
            if (isSelected) {
                seat.setStyle(STYLE_ASSENTO_SELECIONADO);
                if (!assentosSelecionadosIds.contains(assentoId)) {
                    assentosSelecionadosIds.add(assentoId);
                }
            } else {
                seat.setStyle(STYLE_ASSENTO_DISPONIVEL);
                assentosSelecionadosIds.remove(assentoId);
            }
        });

        return seat;
    }
}