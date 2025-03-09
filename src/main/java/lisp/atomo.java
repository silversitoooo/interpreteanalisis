package lisp;

/**
 * Clase base para expresiones S atómicas (aquellas que no son pares/listas).
 */
public abstract class atomo extends ExpresionLisp {
    
    @Override
    public boolean esAtomo() {
        return true;
    }
    
    @Override
    public ExpresionLisp primero() throws ExcepcionLisp {
        throw new ExcepcionLisp("No se puede obtener el primer elemento de un átomo: " + this);
    }
    
    @Override
    public ExpresionLisp resto() throws ExcepcionLisp {
        throw new ExcepcionLisp("No se puede obtener el resto de un átomo: " + this);
    }
}
