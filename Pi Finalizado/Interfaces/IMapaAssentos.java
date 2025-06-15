package Interfaces;
public interface IMapaAssentos {
    void selecionarAssento(String assento);
    void desmarcarAssento(String assento);
    void atualizarPrecoTotal();
    double getTotal();
}