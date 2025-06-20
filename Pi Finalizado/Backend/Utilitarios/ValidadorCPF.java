package Backend.Utilitarios;
import java.util.*;
public class ValidadorCPF {

    public static boolean isCPF(String CPF) {
        if (CPF.contains(".") || CPF.contains("-")) {
            CPF = CPF.replace(".", "").replace("-", "");
        }

        if (CPF.equals("00000000000") || CPF.equals("11111111111") || CPF.equals("22222222222")
                || CPF.equals("33333333333") || CPF.equals("44444444444") || CPF.equals("55555555555")
                || CPF.equals("66666666666") || CPF.equals("77777777777") || CPF.equals("88888888888")
                || CPF.equals("99999999999") || (CPF.length() != 11)) {

            return false;
        }

        char dig10, dig11;
        int sm, r, num, peso;

        try {
            // Cálculo do 1° dígito
            sm = 0;
            peso = 10;
            for (int i = 0; i < 9; i++) {
                num = (int) (CPF.charAt(i) - 48);
                sm += num * peso;
                peso--;
            }
            r = 11 - (sm % 11);
             if(r == 10 || r == 11){
                 dig10 = '0';
            } else {
                 dig10 = (char) (r + 48);
             }

            // Cálculo do 2° dígito
            sm = 0;
            peso = 11;
            for (int i = 0; i < 10; i++) {
                num = (int) (CPF.charAt(i) - 48);
                sm += num * peso;
                peso--;
            }
            r = 11 - (sm % 11);
           if(r == 10 || r == 11){
            dig11 = '0';

            } else {
               dig11 = (char) (r + 48);
                   }

            // Confere os últimos dígitos
            return (dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10));
        } catch (InputMismatchException e) {
            return false;
        }
    }
}