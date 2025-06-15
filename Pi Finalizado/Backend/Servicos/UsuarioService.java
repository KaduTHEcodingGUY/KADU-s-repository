package Backend.Servicos;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioService {
    private static final String URL = "jdbc:postgresql://localhost:5432/teatroabc";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public static class Usuario {
        private String id;
        private String nomeCompleto;
        private String cpf;
        private String telefone;
        private String tipoUsuario;
        private boolean membroFidelidade;

        public String getId() { return id; }
        public String getNomeCompleto() { return nomeCompleto; }
        public String getCpf() { return cpf; }
        public String getTelefone() { return telefone; }
        public String getTipoUsuario() { return tipoUsuario; }
        public boolean isMembroFidelidade() { return membroFidelidade; }

        @Override
        public String toString() {
            return "Usuario{" +
                   "id='" + id + '\'' +
                   ", nomeCompleto='" + nomeCompleto + '\'' +
                   ", cpf='" + cpf + '\'' +
                   ", telefone='" + telefone + '\'' +
                   ", tipoUsuario='" + tipoUsuario + '\'' +
                   ", membroFidelidade=" + membroFidelidade +
                   '}';
        }
    }

    public static Usuario buscarUsuarioPorId(String userId) {
        System.out.println("[DEBUG UsuarioService] Iniciando busca de usuário por ID: " + userId);
        
        if (userId == null) {
            System.err.println("[ERRO UsuarioService] ID do usuário é nulo");
            return null;
        }

        String sql = "SELECT * FROM public.\"Usuarios\" WHERE id = ?::uuid";
        System.out.println("[DEBUG UsuarioService] SQL a ser executada: " + sql);
        
        try (Connection conn = DatabaseService.getConnection()) {
            System.out.println("[DEBUG UsuarioService] Conexão com banco estabelecida com sucesso");
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                System.out.println("[DEBUG UsuarioService] Parâmetro ID definido: " + userId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("[DEBUG UsuarioService] Query executada");
                    
                    if (rs.next()) {
                        Usuario usuario = new Usuario();
                        usuario.id = rs.getString("id");
                        usuario.nomeCompleto = rs.getString("nome_completo");
                        usuario.cpf = rs.getString("cpf");
                        usuario.telefone = rs.getString("telefone");
                        usuario.tipoUsuario = rs.getString("tipo_usuario");
                        usuario.membroFidelidade = rs.getBoolean("membro_fidelidade");
                        
                        System.out.println("[DEBUG UsuarioService] Usuário encontrado: " + usuario);
                        return usuario;
                    } else {
                        System.out.println("[DEBUG UsuarioService] Nenhum usuário encontrado para o ID: " + userId);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO UsuarioService] Erro ao buscar usuário: " + e.getMessage());
            System.err.println("[ERRO UsuarioService] Stack trace:");
            e.printStackTrace();
        }
        
        return null;
    }
} 