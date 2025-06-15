package Backend.Servicos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseService {
    // Atualizando para usar o host do Supabase
    private static final String URL = "jdbc:postgresql://db.brekkmkxtyxadxyaqakv.supabase.co:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "fe!eB&2ZrPNV#vXnJpeTP1BBX^#hy!XV^R1Fj#PKDU%nqB5SUBNQj0*7mRxfsScX";

    public static Connection getConnection() throws SQLException {
        System.out.println("[DEBUG DatabaseService] Tentando conectar ao banco de dados...");
        System.out.println("[DEBUG DatabaseService] URL: " + URL);
        System.out.println("[DEBUG DatabaseService] Usuário: " + USER);
        
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("[DEBUG DatabaseService] Driver PostgreSQL carregado com sucesso");
            
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DEBUG DatabaseService] Conexão estabelecida com sucesso!");
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("[ERRO DatabaseService] Driver PostgreSQL não encontrado");
            e.printStackTrace();
            throw new SQLException("Driver PostgreSQL não encontrado", e);
        } catch (SQLException e) {
            System.err.println("[ERRO DatabaseService] Erro ao conectar ao banco de dados");
            System.err.println("[ERRO DatabaseService] Mensagem: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 