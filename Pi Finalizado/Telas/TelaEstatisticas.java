package Telas;

import Interfaces.ProvedorView;
import Backend.Servicos.EstatisticasService;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

// REMOVIDO: A classe não estende mais Application
public class TelaEstatisticas implements ProvedorView {

    private Label pecaMaisVendidaValor = createValueLabel();
    private Label pecaMenosVendidaValor = createValueLabel();
    private Label sessaoMaiorOcupacaoValor = createValueLabel();
    private Label sessaoMenorOcupacaoValor = createValueLabel();
    private Label faturamentoMedioValor = createValueValueLabel();
    private Label assinantesValor = createValueLabel();
    private Label feedbackLabel;
    private Label pecaMaisLucrativaValor = createValueLabel();
    private Label pecaMenosLucrativaValor = createValueLabel();
    private Label faturamentoAssinaturasValor = createValueValueLabel();

    // REMOVIDO: Método main() não é mais necessário aqui.
    // REMOVIDO: Método start() não é mais necessário aqui.

    // MODIFICADO: O método agora aceita o Stage para seguir o padrão da interface.
    @Override
    public Node getView() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #1E1E1E;");
        mainLayout.setPadding(new Insets(30));

        Label title = new Label("Estatísticas do Teatro");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.WHITE);
        BorderPane.setAlignment(title, Pos.CENTER);
        mainLayout.setTop(title);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(30);
        grid.setVgap(30);
        grid.setPadding(new Insets(40, 0, 0, 0));

        // Primeira linha
        grid.add(createMetricCard("Peça com Mais Ingressos Vendidos", pecaMaisVendidaValor), 0, 0);
        grid.add(createMetricCard("Peça com Menos Ingressos Vendidos", pecaMenosVendidaValor), 1, 0);
        grid.add(createMetricCard("Peça Mais Lucrativa", pecaMaisLucrativaValor), 2, 0);

        // Segunda linha
        grid.add(createMetricCard("Sessão com Maior Ocupação", sessaoMaiorOcupacaoValor), 0, 1);
        grid.add(createMetricCard("Sessão com Menor Ocupação", sessaoMenorOcupacaoValor), 1, 1);
        grid.add(createMetricCard("Peça Menos Lucrativa", pecaMenosLucrativaValor), 2, 1);

        // Terceira linha
        grid.add(createMetricCard("Faturamento Médio por Peça", faturamentoMedioValor), 0, 2);
        grid.add(createMetricCard("Faturamento com Assinaturas", faturamentoAssinaturasValor), 1, 2);
        grid.add(createMetricCard("Assinantes do Clube Fidelidade", assinantesValor), 2, 2);

        mainLayout.setCenter(grid);

        // Criando o container para o botão e o feedback
        VBox bottomContainer = new VBox(10);
        bottomContainer.setAlignment(Pos.CENTER);

        Button btnAtualizar = new Button("Atualizar Estatísticas");
        btnAtualizar.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // Criando o label de feedback
        feedbackLabel = new Label("");
        feedbackLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        feedbackLabel.setTextFill(Color.web("#28a745"));
        feedbackLabel.setVisible(false);

        btnAtualizar.setOnAction(e -> {
            carregarMetricasDoSupabase();
            mostrarFeedback("Estatísticas atualizadas com sucesso!");
        });

        bottomContainer.getChildren().addAll(btnAtualizar, feedbackLabel);
        BorderPane.setAlignment(bottomContainer, Pos.CENTER);
        BorderPane.setMargin(bottomContainer, new Insets(20, 0, 0, 0));
        mainLayout.setBottom(bottomContainer);
        
        // Carrega os dados uma vez quando a tela é criada
        carregarMetricasDoSupabase();

        return mainLayout;
    }

    private void mostrarFeedback(String mensagem) {
        feedbackLabel.setText(mensagem);
        feedbackLabel.setVisible(true);

        // Criando uma animação de fade out
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), feedbackLabel);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setOnFinished(event -> feedbackLabel.setVisible(false));
        fadeTransition.play();
    }

    private VBox createMetricCard(String titulo, Label valorLabel) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25));
        card.setPrefSize(450, 150);
        card.setStyle("-fx-background-color: #2D2D2D; -fx-background-radius: 10;");

        Label tituloLabel = new Label(titulo);
        tituloLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        tituloLabel.setTextFill(Color.LIGHTGRAY);
        tituloLabel.setWrapText(true);
        tituloLabel.setTextAlignment(TextAlignment.CENTER);

        valorLabel.setWrapText(true);

        card.getChildren().addAll(tituloLabel, valorLabel);
        return card;
    }

    private Label createValueLabel() {
        Label label = new Label("Carregando...");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        label.setTextFill(Color.WHITE);
        return label;
    }
    
    private Label createValueValueLabel() {
        Label label = new Label("R$ 0,00");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        label.setTextFill(Color.web("#28a745"));
        return label;
    }

    private void carregarMetricasDoSupabase() {
        System.out.println("Buscando métricas no Supabase...");
        
        Map<String, String> estatisticas = EstatisticasService.buscarEstatisticasGerais();

        // ADICIONADO para depuração: Imprime o mapa de estatísticas
        System.out.println("Estatísticas recebidas DO SUPABASE: " + estatisticas);

        // Atualiza os labels com os dados do Supabase, usando "Sem valor" como padrão
        pecaMaisVendidaValor.setText(getFormattedStatistic(estatisticas, "Peça com mais ingressos vendidos"));
        pecaMenosVendidaValor.setText(getFormattedStatistic(estatisticas, "Peça com menos ingressos vendidos"));
        sessaoMaiorOcupacaoValor.setText(getFormattedStatistic(estatisticas, "Sessão com maior ocupação"));
        sessaoMenorOcupacaoValor.setText(getFormattedStatistic(estatisticas, "Sessão com menor ocupação"));
        faturamentoMedioValor.setText(estatisticas.getOrDefault("Lucro médio do teatro por peça", "R$ 0,00"));
        assinantesValor.setText(estatisticas.getOrDefault("Número total de assinantes", "0"));
        pecaMaisLucrativaValor.setText(getFormattedStatistic(estatisticas, "Peça mais lucrativa"));
        pecaMenosLucrativaValor.setText(getFormattedStatistic(estatisticas, "Peça menos lucrativa"));
        faturamentoAssinaturasValor.setText(estatisticas.getOrDefault("Faturamento com assinaturas", "R$ 0,00"));
    }

    private String getFormattedStatistic(Map<String, String> statsMap, String key) {
        String value = statsMap.get(key);
        if (value == null || value.trim().isEmpty()) {
            return "Sem valor";
        } else {
            return value;
        }
    }
}