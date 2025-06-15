package Backend.Servicos;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import Backend.Utilitarios.SupabaseConfig;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

public class AuthService {

    private static final String AUTH_URL = SupabaseConfig.SUPABASE_URL + "/auth/v1";
    private static final String DATABASE_URL = SupabaseConfig.SUPABASE_URL + "/rest/v1/Usuarios";
    private static final String API_KEY = SupabaseConfig.SUPABASE_ANON_KEY;

    private static final Gson gson = new Gson();

    private static String currentUserId; // Armazenar o ID do usuário logado

    public static String getCurrentUserId() {
        return currentUserId;
    }

    public static boolean signUp(String email, String password, String nomeCompleto, String cpf, String telefone) {
        try {
            System.out.println("[DEBUG] Iniciando cadastro para o email: " + email);
            // 1. Cadastrar usuário no Supabase Auth
            URL url = new URL(AUTH_URL + "/signup");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setDoOutput(true);

            JsonObject authPayload = new JsonObject();
            authPayload.addProperty("email", email);
            authPayload.addProperty("password", password);

            System.out.println("[DEBUG] Payload enviado para Auth: " + authPayload.toString());

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = gson.toJson(authPayload).getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int authResponseCode = conn.getResponseCode();
            StringBuilder authResponse = new StringBuilder();
            if (authResponseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                String responseLine = null;
                while ((responseLine = in.readLine()) != null) {
                    authResponse.append(responseLine.trim());
                }
                in.close();
            } else {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                String errorLine = null;
                while ((errorLine = errorReader.readLine()) != null) {
                    authResponse.append(errorLine.trim());
                }
            }
            System.out.println("[DEBUG] Resposta do Supabase Auth: " + authResponse.toString());

            if (authResponseCode == HttpURLConnection.HTTP_OK) {
                JsonObject authJson = gson.fromJson(authResponse.toString(), JsonObject.class);
                JsonObject user = authJson.getAsJsonObject("user");
                String userId = user.get("id").getAsString();

                // 2. Inserir dados adicionais na tabela "Usuarios"
                URL dbUrl = new URL(DATABASE_URL);
                HttpURLConnection dbConn = (HttpURLConnection) dbUrl.openConnection();
                dbConn.setRequestMethod("POST");
                dbConn.setRequestProperty("Content-Type", "application/json");
                dbConn.setRequestProperty("apikey", API_KEY);
                dbConn.setRequestProperty("Authorization", "Bearer " + API_KEY); // Use a chave anon aqui, ou token se preferir
                dbConn.setDoOutput(true);

                JsonObject userPayload = new JsonObject();
                userPayload.addProperty("id", userId);
                userPayload.addProperty("nome_completo", nomeCompleto);
                userPayload.addProperty("cpf", cpf);
                userPayload.addProperty("telefone", telefone);
                userPayload.addProperty("tipo_usuario", "cliente"); // Valor padrão
                userPayload.addProperty("membro_fidelidade", false); // Valor padrão
                System.out.println("[DEBUG] Payload enviado para Usuarios: " + userPayload.toString());

                try (OutputStream os = dbConn.getOutputStream()) {
                    byte[] input = gson.toJson(userPayload).getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int dbResponseCode = dbConn.getResponseCode();
                BufferedReader dbReader = new BufferedReader(new InputStreamReader(
                    dbResponseCode >= 400 ? dbConn.getErrorStream() : dbConn.getInputStream(), "utf-8"));
                StringBuilder dbResponse = new StringBuilder();
                String dbLine = null;
                while ((dbLine = dbReader.readLine()) != null) {
                    dbResponse.append(dbLine.trim());
                }
                System.out.println("[DEBUG] Resposta do Supabase Usuarios: " + dbResponse.toString());
                return dbResponseCode == HttpURLConnection.HTTP_CREATED; // 201 Created para sucesso
            } else {
                System.err.println("[ERRO] Cadastro no Auth falhou, não será feito insert na tabela Usuarios para o email: " + email);
                return false;
            }
        } catch (Exception e) {
            System.err.println("[ERRO] Exceção no cadastro do email: " + email);
            e.printStackTrace();
            return false;
        }
    }

    public static boolean signIn(String email, String password) {
        try {
            URL url = new URL(AUTH_URL + "/token?grant_type=password");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setDoOutput(true);

            JsonObject payload = new JsonObject();
            payload.addProperty("email", email);
            payload.addProperty("password", password);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = gson.toJson(payload).getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Login bem-sucedido
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = in.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                in.close();

                // Armazenar o access_token e o ID do usuário
                JsonObject authResponse = gson.fromJson(response.toString(), JsonObject.class);
                if (authResponse.has("user") && authResponse.getAsJsonObject("user").has("id")) {
                    currentUserId = authResponse.getAsJsonObject("user").get("id").getAsString();
                    System.out.println("[DEBUG] Usuário logado ID: " + currentUserId);
                } else {
                    System.err.println("[ERRO] ID do usuário não encontrado na resposta de login.");
                }

                return true;
            } else {
                // Login falhou
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                StringBuilder errorResponse = new StringBuilder();
                String errorLine = null;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine.trim());
                }
                System.err.println("Erro no login do Supabase: " + errorResponse.toString());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean atualizarMembroFidelidade(String userId, boolean status) {
        try {
            System.out.println("[DEBUG] Tentando atualizar status de membro fidelidade para o usuário: " + userId + ", status: " + status);
            
            // Criar o payload com o status e o ID para garantir que o Supabase não reclame de null no ID.
            JsonObject payload = new JsonObject();
            payload.addProperty("id", userId); // Adicionar o ID ao payload
            payload.addProperty("membro_fidelidade", status);
            System.out.println("[DEBUG] Payload enviado para update: " + payload.toString());
            
            // Construir a URL com o filtro id=eq
            String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/Usuarios?id=eq." + userId;
            
            HttpClient client = HttpClientBuilder.create().build();
            HttpPatch patch = new HttpPatch(url);
            
            patch.setHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
            patch.setHeader("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY);
            patch.setHeader("Content-Type", "application/json");
            patch.setHeader("Prefer", "return=minimal");
            
            StringEntity entity = new StringEntity(gson.toJson(payload), StandardCharsets.UTF_8);
            patch.setEntity(entity);
            
            HttpResponse response = client.execute(patch);
            int responseCode = response.getStatusLine().getStatusCode();
            System.out.println("[DEBUG] Código de resposta do Supabase: " + responseCode);
            
            if (responseCode == 204) {
                System.out.println("[DEBUG] Atualização do membro fidelidade bem-sucedida");
                return true;
            } else {
                System.out.println("[ERRO] Falha na atualização do membro fidelidade. Código de resposta: " + responseCode);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
                    StringBuilder responseString = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        responseString.append(responseLine.trim());
                    }
                    System.out.println("[DEBUG] Resposta do Supabase para atualização: " + responseString.toString());
                }
                return false;
            }
        } catch (Exception e) {
            System.err.println("[ERRO] Exceção ao atualizar membro fidelidade: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Novo método para obter detalhes do usuário
    public static JsonObject getUserDetails(String userId) {
        if (userId == null || userId.isEmpty()) {
            System.err.println("[ERRO] ID do usuário nulo ou vazio para getUserDetails.");
            return null;
        }

        try {
            String url = SupabaseConfig.SUPABASE_URL + "/rest/v1/Usuarios?id=eq." + userId;
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            
            request.setHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
            request.setHeader("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY);
            request.setHeader("Content-Type", "application/json");
            
            HttpResponse response = client.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();

            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
                    StringBuilder responseString = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        responseString.append(responseLine.trim());
                    }
                    // O Supabase retorna um array JSON para GETs filtrados, mesmo que seja um único resultado
                    // Precisamos pegar o primeiro (e único) elemento do array
                    if (responseString.toString().startsWith("[") && responseString.toString().endsWith("]")) {
                        String jsonArrayString = responseString.toString();
                        if (jsonArrayString.equals("[]")) {
                            System.out.println("[DEBUG] Usuário não encontrado no Supabase: " + userId);
                            return null;
                        }
                        // Remover os colchetes do array e pegar o primeiro objeto
                        jsonArrayString = jsonArrayString.substring(1, jsonArrayString.length() - 1);
                        // Se houver múltiplos objetos (improvável com id=eq), pegamos apenas o primeiro
                        if (jsonArrayString.contains("{") && jsonArrayString.indexOf("}") < jsonArrayString.lastIndexOf("}")) {
                            jsonArrayString = jsonArrayString.substring(jsonArrayString.indexOf("{"), jsonArrayString.indexOf("}") + 1);
                        }
                        return gson.fromJson(jsonArrayString, JsonObject.class);
                    } else {
                        return gson.fromJson(responseString.toString(), JsonObject.class);
                    }
                }
            } else {
                System.err.println("[ERRO] Falha ao obter detalhes do usuário. Código de resposta: " + responseCode);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = br.readLine()) != null) {
                        errorResponse.append(errorLine.trim());
                    }
                    System.err.println("[DEBUG] Resposta de erro do Supabase: " + errorResponse.toString());
                }
                return null;
            }
        } catch (Exception e) {
            System.err.println("[ERRO] Exceção ao obter detalhes do usuário: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}