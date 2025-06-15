
package Backend.Servicos;

import Backend.Utilitarios.SupabaseConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

public class ReservaService {

    private static final String API_URL = SupabaseConfig.SUPABASE_URL + "/rest/v1";
    private static final String API_KEY = SupabaseConfig.SUPABASE_ANON_KEY;
    private static final Gson gson = new Gson();

    // Classe para representar o resultado da criação de uma reserva
    public static class ReservaCriada {
        private boolean sucesso;
        private String codigoIngresso;
        private String reservaId;

        public ReservaCriada(boolean sucesso, String codigoIngresso, String reservaId) {
            this.sucesso = sucesso;
            this.codigoIngresso = codigoIngresso;
            this.reservaId = reservaId;
        }

        public boolean isSucesso() { return sucesso; }
        public String getCodigoIngresso() { return codigoIngresso; }
        public String getReservaId() { return reservaId; }
    }

    // Classe para detalhe de assento na reserva (para enviar ao backend)
    public static class AssentoReservaDetalhe {
        private String assentoId;
        private String nomeArea; // Pode ser útil para logging ou depuração
        private String fileira;
        private int numero;
        private double preco;

        public AssentoReservaDetalhe(String assentoId, String nomeArea, String fileira, int numero, double preco) {
            this.assentoId = assentoId;
            this.nomeArea = nomeArea;
            this.fileira = fileira;
            this.numero = numero;
            this.preco = preco;
        }

        public String getAssentoId() { return assentoId; }
        public String getNomeArea() { return nomeArea; }
        public String getFileira() { return fileira; }
        public int getNumero() { return numero; }
        public double getPreco() { return preco; }
    }

    public static ReservaCriada criarReserva(String usuarioId, String sessaoId, List<AssentoReservaDetalhe> assentos) {
        System.out.println("[DEBUG ReservaService] Iniciando criação de reserva para usuário: " + usuarioId + ", sessão: " + sessaoId + ", com " + assentos.size() + " assentos.");
        String reservaId = null;
        String codigoIngresso = null;

        try {
            // 1. Criar a reserva na tabela 'reservas'
            URL urlReserva = new URL(API_URL + "/reservas");
            HttpURLConnection connReserva = (HttpURLConnection) urlReserva.openConnection();
            connReserva.setRequestMethod("POST");
            connReserva.setRequestProperty("Content-Type", "application/json");
            connReserva.setRequestProperty("apikey", API_KEY);
            connReserva.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connReserva.setRequestProperty("Prefer", "return=representation"); // Retorna o objeto criado
            connReserva.setDoOutput(true);

            // Calcular o valor total da reserva
            double valorTotal = 0.0;
            for (AssentoReservaDetalhe assento : assentos) {
                valorTotal += assento.getPreco();
            }

            // Verificar se o usuário é membro fidelidade
            JsonObject userDetails = AuthService.getUserDetails(usuarioId);
            boolean isMembroFidelidade = false;
            if (userDetails != null && userDetails.has("membro_fidelidade")) {
                isMembroFidelidade = userDetails.get("membro_fidelidade").getAsBoolean();
                if (isMembroFidelidade) {
                    double desconto = valorTotal * 0.10; // 10% de desconto
                    valorTotal -= desconto;
                    System.out.println("[DEBUG ReservaService] Desconto de fidelidade aplicado: R$ " + desconto);
                }
            }

            JsonObject payloadReserva = new JsonObject();
            payloadReserva.addProperty("usuario_id", usuarioId);
            payloadReserva.addProperty("status_pagamento", "Confirmado"); // Definir como Confirmado na criação se o pagamento for imediato
            payloadReserva.addProperty("pagamento", valorTotal); // Adicionar o valor total pago com desconto se aplicável

            System.out.println("[DEBUG ReservaService] Payload da Reserva: " + payloadReserva.toString());

            try (OutputStream os = connReserva.getOutputStream()) {
                byte[] input = gson.toJson(payloadReserva).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCodeReserva = connReserva.getResponseCode();
            String responseReserva = new BufferedReader(new InputStreamReader(connReserva.getInputStream(), StandardCharsets.UTF_8)).readLine();

            System.out.println("[DEBUG ReservaService] Resposta da Reserva (código: " + responseCodeReserva + "): " + responseReserva);

            if (responseCodeReserva == HttpURLConnection.HTTP_CREATED) { // 201 Created
                JsonArray jsonArrayReserva = gson.fromJson(responseReserva, JsonArray.class);
                if (jsonArrayReserva != null && jsonArrayReserva.size() > 0) {
                    JsonObject jsonReserva = jsonArrayReserva.get(0).getAsJsonObject();
                    reservaId = jsonReserva.get("id").getAsString();
                    codigoIngresso = jsonReserva.get("codigo_ingresso").getAsString();
                    System.out.println("[DEBUG ReservaService] Reserva criada com ID: " + reservaId + ", Código: " + codigoIngresso);
                } else {
                    System.err.println("[ERRO ReservaService] Resposta vazia ao criar reserva.");
                    return new ReservaCriada(false, null, null);
                }
            } else {
                System.err.println("[ERRO ReservaService] Erro ao criar reserva. Código: " + responseCodeReserva);
                String errorReserva = new BufferedReader(new InputStreamReader(connReserva.getErrorStream(), StandardCharsets.UTF_8)).readLine();
                System.err.println("[ERRO ReservaService] Detalhes do erro na reserva: " + errorReserva);
                return new ReservaCriada(false, null, null);
            }

            // 2. Inserir os assentos na tabela 'assentos_reservados' se a reserva foi criada com sucesso
            if (reservaId != null) {
                for (AssentoReservaDetalhe assento : assentos) {
                    URL urlAssentoReservado = new URL(API_URL + "/assentos_reservados");
                    HttpURLConnection connAssentoReservado = (HttpURLConnection) urlAssentoReservado.openConnection();
                    connAssentoReservado.setRequestMethod("POST");
                    connAssentoReservado.setRequestProperty("Content-Type", "application/json");
                    connAssentoReservado.setRequestProperty("apikey", API_KEY);
                    connAssentoReservado.setRequestProperty("Authorization", "Bearer " + API_KEY);
                    connAssentoReservado.setDoOutput(true);

                    JsonObject payloadAssento = new JsonObject();
                    payloadAssento.addProperty("reserva_id", reservaId);
                    payloadAssento.addProperty("assento_id", assento.getAssentoId());
                    payloadAssento.addProperty("sessao_id", sessaoId);

                    System.out.println("[DEBUG ReservaService] Payload do Assento Reservado: " + payloadAssento.toString());

                    try (OutputStream os = connAssentoReservado.getOutputStream()) {
                        byte[] input = gson.toJson(payloadAssento).getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    int responseCodeAssento = connAssentoReservado.getResponseCode();
                    if (responseCodeAssento != HttpURLConnection.HTTP_CREATED) {
                        System.err.println("[ERRO ReservaService] Erro ao reservar assento " + assento.getAssentoId() + ". Código: " + responseCodeAssento);
                        String errorAssento = new BufferedReader(new InputStreamReader(connAssentoReservado.getErrorStream(), StandardCharsets.UTF_8)).readLine();
                        System.err.println("[ERRO ReservaService] Detalhes do erro no assento: " + errorAssento);
                        // Em caso de falha em um assento, você pode decidir reverter a reserva ou apenas logar o erro.
                        // Por simplicidade, continuaremos e retornaremos sucesso/falha geral no final.
                    } else {
                        System.out.println("[DEBUG ReservaService] Assento " + assento.getAssentoId() + " reservado com sucesso.");
                    }
                }
                return new ReservaCriada(true, codigoIngresso, reservaId);
            } else {
                System.err.println("[ERRO ReservaService] ReservaId é nulo, não é possível reservar assentos.");
                return new ReservaCriada(false, null, null);
            }

        } catch (Exception e) {
            System.err.println("[ERRO ReservaService] Exceção ao criar reserva: " + e.getMessage());
            e.printStackTrace();
            return new ReservaCriada(false, null, null);
        }
    }
}