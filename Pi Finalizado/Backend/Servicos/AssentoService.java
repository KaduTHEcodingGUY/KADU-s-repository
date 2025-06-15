package Backend.Servicos;

import Backend.Utilitarios.SupabaseConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AssentoService {

    private static final String API_URL = SupabaseConfig.SUPABASE_URL + "/rest/v1";
    private static final String API_KEY = SupabaseConfig.SUPABASE_ANON_KEY;
    private static final Gson gson = new Gson();

    public static class AreaAssento {
        private String id;
        private String nome_area;
        private double preco;

        public String getId() { return id; }
        public String getNomeArea() { return nome_area; }
        public double getPreco() { return preco; }
    }

    public static class Assento {
        private String id;
        private String area_id;
        private String fileira;
        private int numero;

        public String getId() { return id; }
        public String getAreaId() { return area_id; }
        public String getFileira() { return fileira; }
        public int getNumero() { return numero; }
    }

    public static class AssentoReservado {
        private String reserva_id;
        private String assento_id;
        private String sessao_id;

        public String getReservaId() { return reserva_id; }
        public String getAssentoId() { return assento_id; }
        public String getSessaoId() { return sessao_id; }
    }

    public static List<AreaAssento> listarAreasAssentos() {
        List<AreaAssento> areas = new ArrayList<>();
        try {
            URL url = new URL(API_URL + "/areas_assentos?select=*");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                JsonArray jsonArray = gson.fromJson(responseBuilder.toString(), JsonArray.class);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject json = jsonArray.get(i).getAsJsonObject();
                    AreaAssento area = new AreaAssento();
                    area.id = json.get("id").getAsString();
                    area.nome_area = json.get("nome_area").getAsString();
                    area.preco = json.get("preco").getAsDouble();
                    areas.add(area);
                }
            } else {
                System.err.println("Erro ao listar áreas de assentos. Código: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Exceção ao listar áreas de assentos: " + e.getMessage());
            e.printStackTrace();
        }
        return areas;
    }

    public static List<Assento> listarAssentosPorArea(String areaId) {
        List<Assento> assentos = new ArrayList<>();
        try {
            URL url = new URL(API_URL + "/assentos?area_id=eq." + areaId + "&select=*");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                JsonArray jsonArray = gson.fromJson(responseBuilder.toString(), JsonArray.class);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject json = jsonArray.get(i).getAsJsonObject();
                    Assento assento = new Assento();
                    assento.id = json.get("id").getAsString();
                    assento.area_id = json.get("area_id").getAsString();
                    assento.fileira = json.get("fileira").getAsString();
                    assento.numero = json.get("numero").getAsInt();
                    assentos.add(assento);
                }
            } else {
                System.err.println("Erro ao listar assentos por área. Código: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Exceção ao listar assentos por área: " + e.getMessage());
            e.printStackTrace();
        }
        return assentos;
    }

    public static List<AssentoReservado> listarAssentosReservados(String sessaoId) {
        List<AssentoReservado> assentosReservados = new ArrayList<>();
        try {
            URL url = new URL(API_URL + "/assentos_reservados?sessao_id=eq." + sessaoId + "&select=*");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", API_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
                JsonArray jsonArray = gson.fromJson(responseBuilder.toString(), JsonArray.class);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject json = jsonArray.get(i).getAsJsonObject();
                    AssentoReservado assentoReservado = new AssentoReservado();
                    assentoReservado.reserva_id = json.get("reserva_id").getAsString();
                    assentoReservado.assento_id = json.get("assento_id").getAsString();
                    assentoReservado.sessao_id = json.get("sessao_id").getAsString();
                    assentosReservados.add(assentoReservado);
                }
            } else {
                System.err.println("Erro ao listar assentos reservados. Código: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("Exceção ao listar assentos reservados: " + e.getMessage());
            e.printStackTrace();
        }
        return assentosReservados;
    }
} 