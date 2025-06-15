package Backend.Servicos;

import Backend.Utilitarios.SupabaseConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PecaService {
    private static final String API_URL = SupabaseConfig.SUPABASE_URL + "/rest/v1";
    private static final String API_KEY = SupabaseConfig.SUPABASE_ANON_KEY;
    private static final Gson gson = new Gson();

    public static class Peca {
        private String id;
        private String titulo;
        private String sinopse;
        private String urlImagem;

        public String getId() { return id; }
        public String getTitulo() { return titulo; }
        public String getSinopse() { return sinopse; }
        public String getUrlImagem() { return urlImagem; }
    }

    public static class Sessao {
        private String id;
        private String pecaId;
        private LocalDateTime dataHoraInicio;
        private String status;

        public String getId() { return id; }
        public String getPecaId() { return pecaId; }
        public LocalDateTime getDataHoraInicio() { return dataHoraInicio; }
        public String getStatus() { return status; }
    }

    public static List<Peca> listarPecas() {
        try {
            System.out.println("[DEBUG PecaService] Iniciando listagem de peças");
            System.out.println("[DEBUG PecaService] URL da API: " + API_URL + "/pecas");
            
            URL url = new URL(API_URL + "/pecas");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

            int responseCode = conn.getResponseCode();
            System.out.println("[DEBUG PecaService] Código de resposta: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                String response = responseBuilder.toString();
                System.out.println("[DEBUG PecaService] Resposta do servidor: " + response);
                
                JsonArray jsonArray = gson.fromJson(response, JsonArray.class);
                List<Peca> pecas = new ArrayList<>();
                
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject json = jsonArray.get(i).getAsJsonObject();
                    Peca peca = new Peca();
                    peca.id = json.get("id").getAsString();
                    peca.titulo = json.get("titulo").getAsString();
                    peca.sinopse = json.get("sinopse").getAsString();
                    
                    // Tratamento da URL da imagem
                    String urlImagem = json.get("url_imagem").getAsString();
                    if (urlImagem != null && !urlImagem.trim().isEmpty()) {
                        // Se a URL não começa com http, adiciona o domínio do Supabase
                        if (!urlImagem.startsWith("http")) {
                            urlImagem = SupabaseConfig.SUPABASE_URL + "/storage/v1/object/public/" + urlImagem;
                        }
                        System.out.println("[DEBUG PecaService] URL da imagem processada: " + urlImagem);
                    }
                    peca.urlImagem = urlImagem;
                    
                    System.out.println("[DEBUG PecaService] Peça carregada: " + peca.titulo);
                    pecas.add(peca);
                }
                
                return pecas;
            } else {
                System.err.println("[ERRO PecaService] Erro ao listar peças. Código: " + responseCode);
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String errorResponse = errorReader.readLine();
                System.err.println("[ERRO PecaService] Resposta de erro: " + errorResponse);
            }
        } catch (Exception e) {
            System.err.println("[ERRO PecaService] Exceção ao listar peças: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static List<Sessao> listarSessoesPorPeca(String pecaId) {
        try {
            System.out.println("[DEBUG PecaService] Iniciando listagem de sessões para peça: " + pecaId);
            URL url = new URL(API_URL + "/sessoes?peca_id=eq." + pecaId + "&status=eq.Agendada");
            System.out.println("[DEBUG PecaService] URL da API para sessões: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

            int responseCode = conn.getResponseCode();
            System.out.println("[DEBUG PecaService] Código de resposta para sessões: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                String response = responseBuilder.toString();
                System.out.println("[DEBUG PecaService] Resposta do servidor para sessões: " + response);
                
                JsonArray jsonArray = gson.fromJson(response, JsonArray.class);
                List<Sessao> sessoes = new ArrayList<>();
                
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
                
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject json = jsonArray.get(i).getAsJsonObject();
                    Sessao sessao = new Sessao();
                    sessao.id = json.get("id").getAsString();
                    sessao.pecaId = json.get("peca_id").getAsString();
                    sessao.dataHoraInicio = LocalDateTime.parse(json.get("data_hora_inicio").getAsString(), formatter);
                    sessao.status = json.get("status").getAsString();
                    sessoes.add(sessao);
                    System.out.println("[DEBUG PecaService] Sessão carregada: " + sessao.getDataHoraInicio() + " para peça " + sessao.getPecaId());
                }
                
                return sessoes;
            } else {
                System.err.println("[ERRO PecaService] Erro ao listar sessões. Código: " + responseCode);
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String errorResponse = errorReader.readLine();
                System.err.println("[ERRO PecaService] Resposta de erro para sessões: " + errorResponse);
            }
        } catch (Exception e) {
            System.err.println("[ERRO PecaService] Exceção ao listar sessões: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static Sessao buscarSessaoPorId(String sessaoId) {
        try {
            System.out.println("[DEBUG PecaService] Iniciando busca por sessão ID: " + sessaoId);
            URL url = new URL(API_URL + "/sessoes?id=eq." + sessaoId);
            System.out.println("[DEBUG PecaService] URL da API para sessão por ID: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

            int responseCode = conn.getResponseCode();
            System.out.println("[DEBUG PecaService] Código de resposta para sessão por ID: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                String response = responseBuilder.toString();
                System.out.println("[DEBUG PecaService] Resposta do servidor para sessão por ID: " + response);
                
                JsonArray jsonArray = gson.fromJson(response, JsonArray.class);
                if (jsonArray.size() > 0) {
                    JsonObject json = jsonArray.get(0).getAsJsonObject();
                    Sessao sessao = new Sessao();
                    sessao.id = json.get("id").getAsString();
                    sessao.pecaId = json.get("peca_id").getAsString();
                    sessao.dataHoraInicio = LocalDateTime.parse(json.get("data_hora_inicio").getAsString(), DateTimeFormatter.ISO_DATE_TIME);
                    sessao.status = json.get("status").getAsString();
                    System.out.println("[DEBUG PecaService] Sessão encontrada: " + sessao.getDataHoraInicio() + " para peça " + sessao.getPecaId());
                    return sessao;
                } else {
                    System.out.println("[DEBUG PecaService] Nenhuma sessão encontrada com ID: " + sessaoId);
                }
            } else {
                System.err.println("[ERRO PecaService] Erro ao buscar sessão por ID. Código: " + responseCode);
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String errorResponse = errorReader.readLine();
                System.err.println("[ERRO PecaService] Resposta de erro ao buscar sessão por ID: " + errorResponse);
            }
        } catch (Exception e) {
            System.err.println("[ERRO PecaService] Exceção ao buscar sessão por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
} 