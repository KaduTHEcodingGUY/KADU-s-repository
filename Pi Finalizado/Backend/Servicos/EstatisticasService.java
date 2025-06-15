package Backend.Servicos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EstatisticasService {

    public static Map<String, String> buscarEstatisticasGerais() {
        Map<String, String> estatisticas = new HashMap<>();
        String query = "SELECT criterio, resultado FROM estatisticas_gerais";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String criterio = rs.getString("criterio");
                String resultado = rs.getString("resultado");
                estatisticas.put(criterio, resultado);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar estatísticas gerais: " + e.getMessage());
            e.printStackTrace();
        }
        return estatisticas;
    }
} 