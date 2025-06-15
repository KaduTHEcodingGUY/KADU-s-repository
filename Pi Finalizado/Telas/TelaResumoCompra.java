package Telas;

import Interfaces.ProvedorView;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.Cursor;
import java.util.List;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import Backend.Servicos.PecaService;
import Backend.Servicos.PecaService.Peca;
import Backend.Servicos.PecaService.Sessao;
import Backend.Servicos.AssentoService;
import Backend.Servicos.AssentoService.Assento;
import Backend.Servicos.AssentoService.AreaAssento;

public class TelaResumoCompra implements ProvedorView {

    private String sessaoId;
    private List<String> assentoIds;
    private BiConsumer<Node, Boolean> viewSwitcher;

    private Peca pecaSelecionada;
    private Sessao sessaoSelecionada;
    private List<Assento> assentosDetalhes = new ArrayList<>();
    private double precoTotal = 0.0;

    public TelaResumoCompra(String sessaoId, List<String> assentoIds, BiConsumer<Node, Boolean> viewSwitcher) {
        System.out.println("[DEBUG TelaResumoCompra] Construtor TelaResumoCompra chamado com sessaoId: " + sessaoId + ", assentoIds: " + assentoIds.size());
        this.sessaoId = sessaoId;
        this.assentoIds = assentoIds;
        this.viewSwitcher = viewSwitcher;
        carregarDadosCompra();
    }

    private void carregarDadosCompra() {
        System.out.println("[DEBUG TelaResumoCompra] Carregando dados da compra...");
        sessaoSelecionada = PecaService.buscarSessaoPorId(sessaoId);
        
        if (sessaoSelecionada != null) {
            List<Peca> todasPecas = PecaService.listarPecas();
            for (Peca p : todasPecas) {
                if (p.getId().equals(sessaoSelecionada.getPecaId())) {
                    pecaSelecionada = p;
                    break;
                }
            }
            System.out.println("[DEBUG TelaResumoCompra] Peça selecionada: " + (pecaSelecionada != null ? pecaSelecionada.getTitulo() : "NULA"));
            System.out.println("[DEBUG TelaResumoCompra] Sessão selecionada: " + (sessaoSelecionada != null ? sessaoSelecionada.getDataHoraInicio() : "NULA"));
        } else {
            System.err.println("[ERRO TelaResumoCompra] Sessão com ID " + sessaoId + " não encontrada.");
        }

        precoTotal = 0.0;
        List<AreaAssento> todasAreas = AssentoService.listarAreasAssentos();
        for (String assentoId : assentoIds) {
            Assento assentoDetalhe = null;
            AreaAssento areaDoAssento = null;
            
            for(AreaAssento area : todasAreas) {
                List<Assento> assentosDaArea = AssentoService.listarAssentosPorArea(area.getId());
                for(Assento ass : assentosDaArea) {
                    if (ass.getId().equals(assentoId)) {
                        assentoDetalhe = ass;
                        areaDoAssento = area;
                        break;
                    }
                }
                if (assentoDetalhe != null) break;
            }

            if (assentoDetalhe != null && areaDoAssento != null) {
                assentosDetalhes.add(assentoDetalhe);
                precoTotal += areaDoAssento.getPreco();
                System.out.println("[DEBUG TelaResumoCompra] Assento " + assentoDetalhe.getFileira() + assentoDetalhe.getNumero() + " (Area: " + areaDoAssento.getNomeArea() + ") adicionado. Preço: R$ " + areaDoAssento.getPreco());
            } else {
                System.err.println("[ERRO TelaResumoCompra] Detalhes para o assento ID " + assentoId + " não encontrados.");
            }
        }
        System.out.println("[DEBUG TelaResumoCompra] Preço total calculado: R$ " + precoTotal);
    }

    @Override
    public Node getView() {
        System.out.println("[DEBUG TelaResumoCompra] getView() chamado para renderizar.");
        BorderPane rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: black;");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));

        Label titleLabel = new Label("Resumo da Compra");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        titleLabel.setTextFill(Color.WHITE);

        Label pecaInfo = new Label("Peça: " + (pecaSelecionada != null ? pecaSelecionada.getTitulo() : "N/A"));
        pecaInfo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        pecaInfo.setTextFill(Color.LIGHTGRAY);

        Label sessaoInfo = new Label("Sessão: " + (sessaoSelecionada != null ? sessaoSelecionada.getDataHoraInicio().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"));
        sessaoInfo.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        sessaoInfo.setTextFill(Color.LIGHTGRAY);

        VBox assentosBox = new VBox(5);
        assentosBox.setAlignment(Pos.CENTER);
        Label assentosTitle = new Label("Assentos Selecionados:");
        assentosTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        assentosTitle.setTextFill(Color.WHITE);
        assentosBox.getChildren().add(assentosTitle);

        List<AreaAssento> todasAreas = AssentoService.listarAreasAssentos();
        for (Assento assento : assentosDetalhes) {
            // Encontrar a área do assento
            AreaAssento areaDoAssento = null;
            for(AreaAssento area : todasAreas) {
                List<Assento> assentosDaArea = AssentoService.listarAssentosPorArea(area.getId());
                for(Assento ass : assentosDaArea) {
                    if (ass.getId().equals(assento.getId())) {
                        areaDoAssento = area;
                        break;
                    }
                }
                if (areaDoAssento != null) break;
            }

            String areaNome = areaDoAssento != null ? areaDoAssento.getNomeArea() : "N/A";
            Label assentoLabel = new Label(String.format("Área: %s, Fileira: %s, Número: %s", 
                areaNome, assento.getFileira(), assento.getNumero()));
            assentoLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 16));
            assentoLabel.setTextFill(Color.LIGHTGRAY);
            assentosBox.getChildren().add(assentoLabel);
        }

        Label totalLabel = new Label(String.format("Total: R$ %.2f", precoTotal));
        totalLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        totalLabel.setTextFill(Color.web("#DC3545"));

        Button btnContinuarCompra = new Button("Continuar Compra");
        btnContinuarCompra.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25; -fx-padding: 10px 25px;");
        btnContinuarCompra.setCursor(Cursor.HAND);
        btnContinuarCompra.setOnAction(e -> {
            System.out.println("[DEBUG TelaResumoCompra] Botão Continuar Compra clicado.");
            if (viewSwitcher != null) {
                TelaPagamentoCartao pagamentoView = new TelaPagamentoCartao(sessaoId, assentoIds, precoTotal, viewSwitcher);
                viewSwitcher.accept(pagamentoView.getView(), false);
            } else {
                System.err.println("[ERRO TelaResumoCompra] viewSwitcher é nulo. Não é possível navegar para o pagamento.");
            }
        });

        Button btnVoltar = new Button("← Voltar");
        btnVoltar.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnVoltar.setCursor(Cursor.HAND);
        btnVoltar.setOnAction(e -> {
            System.out.println("[DEBUG TelaResumoCompra] Botão Voltar clicado.");
            if (viewSwitcher != null) {
                viewSwitcher.accept(new MapaAssentos(null, sessaoId, viewSwitcher).getView(), false);
            } else {
                System.err.println("[ERRO TelaResumoCompra] viewSwitcher é nulo. Não é possível voltar.");
            }
        });

        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(btnContinuarCompra, btnVoltar);

        content.getChildren().addAll(titleLabel, pecaInfo, sessaoInfo, assentosBox, totalLabel, buttonBox);
        rootLayout.setCenter(content);

        return rootLayout;
    }
}