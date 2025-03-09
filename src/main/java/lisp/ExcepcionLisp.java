package lisp;

/**
 * Clase base para excepciones en el intérprete LISP.
 */
public class ExcepcionLisp extends Exception {
    public ExcepcionLisp(String mensaje) {
        super(mensaje);
    }
}
