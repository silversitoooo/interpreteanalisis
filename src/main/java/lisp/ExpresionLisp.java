package lisp;

import java.io.PrintStream;

/**
 * Clase base para todas las expresiones S en LISP.
 * Una expresión S puede ser un átomo (símbolo, número) o una lista.
 */
public abstract class ExpresionLisp {
    
    /**
     * Devuelve el primer elemento de esta expresión S.
     * Debe lanzar una excepción si se llama en un átomo.
     */
    public abstract ExpresionLisp primero() throws ExcepcionLisp;
    
    /**
     * Devuelve el resto de la lista de esta expresión S.
     * Debe lanzar una excepción si se llama en un átomo.
     */
    public abstract ExpresionLisp resto() throws ExcepcionLisp;
    
    /**
     * Verifica si esta expresión S es un átomo.
     */
    public boolean esAtomo() {
        return false;
    }
    
    /**
     * Verifica si esta expresión S es un símbolo.
     */
    public boolean esSimbolo() {
        return false;
    }
    
    /**
     * Verifica si esta expresión S es un número.
     */
    public boolean esNumero() {
        return false;
    }
    
    /**
     * Imprime esta expresión S en el PrintStream dado.
     */
    public abstract void imprimir(PrintStream salida);
    
    /**
     * Devuelve una representación de cadena de esta expresión S.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        PrintStream ps = new PrintStream(System.out) {
            @Override
            public void print(String s) {
                sb.append(s);
            }
            
            @Override
            public void print(long l) {
                sb.append(l);
            }
        };
        imprimir(ps);
        return sb.toString();
    }
}
