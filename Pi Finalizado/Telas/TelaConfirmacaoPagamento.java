package Telas;

import Interfaces.ProvedorView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.BiConsumer;

public class TelaConfirmacaoPagamento implements ProvedorView {
    private String codigoIngresso;
    private BiConsumer<Node, Boolean> viewSwitcher;

    public TelaConfirmacaoPagamento(String codigoIngresso, BiConsumer<Node, Boolean> viewSwitcher) {
        this.codigoIngresso = codigoIngresso;
        this.viewSwitcher = viewSwitcher;
    }

    @Override
    public Node getView() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));

        Label titulo = new Label("Pagamento Confirmado!");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        titulo.setTextFill(Color.WHITE);

        Label codigoLabel = new Label("Seu código de ingresso é:");
        codigoLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        codigoLabel.setTextFill(Color.WHITE);

        Label codigo = new Label(codigoIngresso);
        codigo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        codigo.setTextFill(Color.GREEN);

        Button btnVoltar = new Button("Voltar ao Início");
        btnVoltar.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 25; -fx-padding: 10px 25px;");
        btnVoltar.setOnAction(e -> {
            if (viewSwitcher != null) {
                viewSwitcher.accept(new TelaCompraIngresso(viewSwitcher).getView(), true);
            }
        });

        layout.getChildren().addAll(titulo, codigoLabel, codigo, btnVoltar);
        return layout;
    }
} 