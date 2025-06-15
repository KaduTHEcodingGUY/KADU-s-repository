package Interfaces;

import javafx.scene.Node;

/**
 * Interface que representa qualquer componente visual que pode ser exibido
 * na área de conteúdo principal da aplicação.
 */
public interface ProvedorView {
    /**
     * @return O nó (Node) JavaFX que contém toda a interface visual desta view.
     */
    Node getView();
}