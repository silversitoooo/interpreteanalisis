package lisp;

import java.io.PrintStream;

/**
 * Representa una celda cons (par) en LISP, el bloque de construcción para listas.
 */
public class par extends ExpresionLisp {
    private final ExpresionLisp primero;
    private final ExpresionLisp resto;
    
    public par(ExpresionLisp primero, ExpresionLisp resto) {
        this.primero = primero;
        this.resto = resto;
    }
    
    @Override
    public ExpresionLisp primero() {
        return primero;
    }
    
    @Override
    public ExpresionLisp resto() {
        return resto;
    }
    
    /**
     * Crea una lista a partir de los elementos dados.
     */
    public static ExpresionLisp crearLista(ExpresionLisp... elementos) {
        ExpresionLisp resultado = simbolo.NULO;
        for (int i = elementos.length - 1; i >= 0; i--) {
            resultado = new par(elementos[i], resultado);
        }
        return resultado;
    }
    
    /**
     * Verifica si esta celda cons representa una lista adecuada
     * (termina con NULO).
     */
    public boolean esLista() {
        ExpresionLisp actual = this;
        while (!(actual.esAtomo())) {
            try {
                actual = actual.resto();
            } catch (ExcepcionLisp e) {
                return false;
            }
        }
        return actual == simbolo.NULO;
    }
    
    /**
     * Retorna la longitud de la lista representada por esta celda cons.
     * Retorna -1 si esta no es una lista adecuada.
     */
    public int longitud() {
        if (!esLista()) return -1;
        
        int longitud = 0;
        ExpresionLisp actual = this;
        while (actual != simbolo.NULO) {
            longitud++;
            try {
                actual = actual.resto();
            } catch (ExcepcionLisp e) {
                return -1;
            }
        }
        return longitud;
    }
    
    @Override
    public void imprimir(PrintStream salida) {
        salida.print("(");
        
        // Imprime el primer elemento
        primero.imprimir(salida);
        
        // Imprime el resto de la lista
        ExpresionLisp restoLista = resto;
        while (!(restoLista.esAtomo()) && restoLista != simbolo.NULO) {
            salida.print(" ");
            try {
                restoLista.primero().imprimir(salida);
                restoLista = restoLista.resto();
            } catch (ExcepcionLisp e) {
                break;
            }
        }
        
        // Si el último resto no es NULO, imprímelo como un par punteado
        if (restoLista != simbolo.NULO) {
            salida.print(" . ");
            restoLista.imprimir(salida);
        }
        
        salida.print(")");
    }
}
