import Telas.Telalogin;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * A classe Main é o ponto de entrada único para toda a aplicação JavaFX.
 * Sua única responsabilidade é iniciar a janela principal (Stage) e carregar a primeira tela.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Define o título da janela principal
        primaryStage.setTitle("App de Cinema");

        // 1. Cria a instância da sua tela de login
        Telalogin telaLogin = new Telalogin();

        // 2. Pega a interface gráfica (o nó Parent) da tela de login.
        //    Passamos o 'primaryStage' para que os botões dentro do login possam navegar.
        Parent loginRoot = telaLogin.getRoot(primaryStage);

        // 3. Cria a cena inicial com a interface de login
        Scene scene = new Scene(loginRoot);

        // 4. Coloca a cena na janela e a exibe
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        // O único comando necessário no main é este.
        // Ele inicia o framework JavaFX e chama o método start() acima.
        launch(args);
    }
}