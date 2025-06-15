package Telas;

import Interfaces.ProvedorView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import Backend.Servicos.AuthService;
import Backend.Servicos.ReservaService;
import Backend.Servicos.ReservaService.ReservaCriada;
import Backend.Servicos.ReservaService.AssentoReservaDetalhe;
import Backend.Servicos.AssentoService;
import Backend.Servicos.AssentoService.Assento;
import Backend.Servicos.AssentoService.AreaAssento;
import Backend.Servicos.PecaService;
import Backend.Servicos.PecaService.Peca;
import Backend.Servicos.PecaService.Sessao;
import java.util.List;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class TelaPagamentoCartao implements ProvedorView {

    private TextField cardNumberField;
    private TextField expiryDateField;
    private PasswordField cvvField;
    private Label messageLabel;

    private java.util.function.Consumer<javafx.scene.Node> onMembershipConfirmed;

    private String sessaoId;
    private List<String> assentoIds;
    private double precoTotal;
    private BiConsumer<Node, Boolean> viewSwitcher;

    public TelaPagamentoCartao(java.util.function.Consumer<javafx.scene.Node> onMembershipConfirmed) {
        System.out.println("[DEBUG TelaPagamentoCartao] Construtor para fluxo de Fidelidade.");
        this.onMembershipConfirmed = onMembershipConfirmed;
    }

    public TelaPagamentoCartao(String sessaoId, List<String> assentoIds, double precoTotal, BiConsumer<Node, Boolean> viewSwitcher) {
        System.out.println("[DEBUG TelaPagamentoCartao] Construtor para fluxo de Compra de Ingresso. SessaoId: " + sessaoId + ", Assentos: " + assentoIds.size() + ", Total: " + precoTotal);
        this.sessaoId = sessaoId;
        this.assentoIds = assentoIds;
        this.precoTotal = precoTotal;
        this.viewSwitcher = viewSwitcher;
    }

    @Override
    public Node getView() {
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(50));
        layout.setBackground(new Background(new BackgroundFill(Color.web("#1E1E1E"), CornerRadii.EMPTY, Insets.EMPTY)));

        Label title = new Label("Informações de Pagamento");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        title.setTextFill(Color.WHITE);

        cardNumberField = createTextField("Número do Cartão");
        expiryDateField = createTextField("MM/AA");
        cvvField = createPasswordField("CVV");

        Button confirmButton = new Button("Confirmar Compra");
        confirmButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        confirmButton.setTextFill(Color.WHITE);
        confirmButton.setBackground(new Background(new BackgroundFill(Color.web("#28a745"), new CornerRadii(20), Insets.EMPTY)));
        confirmButton.setPadding(new Insets(10, 25, 10, 25));

        messageLabel = new Label();
        messageLabel.setTextFill(Color.RED);
        messageLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));

        confirmButton.setOnAction(event -> {
            if (this.sessaoId != null && this.assentoIds != null && !this.assentoIds.isEmpty()) {
                handleTicketPurchase();
            } else {
                handleMembershipConfirmation();
            }
        });

        layout.getChildren().addAll(title, cardNumberField, expiryDateField, cvvField, confirmButton, messageLabel);

        return layout;
    }

    private TextField createTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setPrefWidth(300);
        textField.setMaxWidth(300);
        textField.setStyle(
                "-fx-background-color: #3D3D3D; " +
                "-fx-text-fill: white; " +
                "-fx-prompt-text-fill: #BDBDBD; " +
                "-fx-font-size: 14px; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; "
        );
        textField.setPadding(new Insets(10));
        return textField;
    }

    private PasswordField createPasswordField(String promptText) {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText(promptText);
        passwordField.setPrefWidth(300);
        passwordField.setMaxWidth(300);
        passwordField.setStyle(
                "-fx-background-color: #3D3D3D; " +
                "-fx-text-fill: white; " +
                "-fx-prompt-text-fill: #BDBDBD; " +
                "-fx-font-size: 14px; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; "
        );
        passwordField.setPadding(new Insets(10));
        return passwordField;
    }

    private void handleMembershipConfirmation() {
        System.out.println("[DEBUG TelaPagamentoCartao] Processando confirmação de membro fidelidade.");
        String cardNumber = cardNumberField.getText();
        String expiryDate = expiryDateField.getText();
        String cvv = cvvField.getText();

        if (cardNumber.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
            messageLabel.setText("Por favor, preencha todos os campos.");
            return;
        }

        if (cardNumber.length() < 16 || expiryDate.length() < 5 || cvv.length() < 3) {
            messageLabel.setText("Dados do cartão inválidos.");
            return;
        }

        String userId = AuthService.getCurrentUserId();
        if (userId == null) {
            messageLabel.setText("Erro: Usuário não logado. Por favor, faça login novamente.");
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível obter o ID do usuário. Faça login novamente.");
            return;
        }

        boolean success = AuthService.atualizarMembroFidelidade(userId, true);

        if (success) {
            messageLabel.setText("Pagamento processado e status de membro fidelidade atualizado com sucesso!");
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Pagamento confirmado! Você agora é um membro fidelidade.");
            if (onMembershipConfirmed != null) {
                onMembershipConfirmed.accept(new ClubeFidelidade().getView());
            }
        } else {
            messageLabel.setText("Erro ao atualizar status de membro fidelidade. Tente novamente.");
            showAlert(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro ao atualizar seu status de membro fidelidade.");
        }
    }

    private void handleTicketPurchase() {
        System.out.println("[DEBUG TelaPagamentoCartao] Processando compra de ingresso.");
        String cardNumber = cardNumberField.getText();
        String expiryDate = expiryDateField.getText();
        String cvv = cvvField.getText();

        if (cardNumber.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
            messageLabel.setText("Por favor, preencha todos os campos.");
            return;
        }

        if (cardNumber.length() < 16 || expiryDate.length() < 5 || cvv.length() < 3) {
            messageLabel.setText("Dados do cartão inválidos.");
            return;
        }

        String userId = AuthService.getCurrentUserId();
        if (userId == null) {
            messageLabel.setText("Erro: Usuário não logado. Por favor, faça login novamente.");
            showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível obter o ID do usuário. Faça login novamente.");
            return;
        }
        
        System.out.println("[DEBUG TelaPagamentoCartao] Tentando criar reserva e assentos reservados...");

        List<AssentoReservaDetalhe> assentosParaReserva = new ArrayList<>();
        
        List<Assento> todosAssentosDisponiveis = new ArrayList<>();
        List<AreaAssento> todasAreasAssentos = new ArrayList<>();

        todasAreasAssentos = AssentoService.listarAreasAssentos();
        for (String assentoId : this.assentoIds) {
            Assento assentoDetalhe = null;
            AreaAssento areaDoAssento = null;
            
            for(AreaAssento area : todasAreasAssentos) {
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
                assentosParaReserva.add(new AssentoReservaDetalhe(assentoDetalhe.getId(), areaDoAssento.getNomeArea(), assentoDetalhe.getFileira(), assentoDetalhe.getNumero(), areaDoAssento.getPreco()));
            } else {
                System.err.println("[ERRO TelaPagamentoCartao] Detalhes do assento " + assentoId + " não encontrados para reserva.");
            }
        }

        ReservaCriada reserva = ReservaService.criarReserva(userId, this.sessaoId, assentosParaReserva);

        if (reserva != null && reserva.isSucesso()) {
            System.out.println("[DEBUG TelaPagamentoCartao] Compra de ingresso efetuada com sucesso! Código da Reserva: " + reserva.getCodigoIngresso());
            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Compra confirmada! Seu código de ingresso é: " + reserva.getCodigoIngresso());
            
            if (viewSwitcher != null) {
                TelaConfirmacaoPagamento confirmacaoView = new TelaConfirmacaoPagamento(reserva.getCodigoIngresso(), viewSwitcher);
                viewSwitcher.accept(confirmacaoView.getView(), false);
            }
        } else {
            messageLabel.setText("Erro ao efetuar a compra do ingresso. Tente novamente.");
            showAlert(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro ao processar sua compra.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}