package lisp;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa un contexto para enlaces de variables en LISP.
 * Los contextos pueden anidarse (para ámbito léxico).
 */
public class contexto {
    private final Map<simbolo, ExpresionLisp> enlaces;
    private final contexto padre;
    
    /**
     * Crea un nuevo contexto global.
     */
    public contexto() {
        this(null);
    }
    
    /**
     * Crea un nuevo contexto con el padre dado.
     */
    public contexto(contexto padre) {
        this.enlaces = new HashMap<>();
        this.padre = padre;
    }
    
    /**
     * Obtiene el valor enlazado al símbolo dado en este contexto.
     * Si el símbolo no está enlazado en este contexto, busca en el padre.
     * 
     * @throws ExcepcionLisp si el símbolo no está enlazado en ningún contexto
     */
    public ExpresionLisp obtener(simbolo simbolo) throws ExcepcionLisp {
        if (enlaces.containsKey(simbolo)) {
            return enlaces.get(simbolo);
        } else if (padre != null) {
            return padre.obtener(simbolo);
        } else {
            throw new ExcepcionLisp("Símbolo no definido: " + simbolo.obtenerNombre());
        }
    }
    
    /**
     * Enlaza el símbolo dado al valor dado en este contexto.
     */
    public void establecer(simbolo simbolo, ExpresionLisp valor) {
        enlaces.put(simbolo, valor);
    }
    
    /**
     * Actualiza el enlace del símbolo dado al valor dado.
     * Si el símbolo ya está enlazado en un contexto padre,
     * actualiza el enlace allí.
     * 
     * @throws ExcepcionLisp si el símbolo no está enlazado en ningún contexto
     */
    public void actualizar(simbolo simbolo, ExpresionLisp valor) throws ExcepcionLisp {
        if (enlaces.containsKey(simbolo)) {
            enlaces.put(simbolo, valor);
        } else if (padre != null) {
            padre.actualizar(simbolo, valor);
        } else {
            throw new ExcepcionLisp("Símbolo no definido: " + simbolo.obtenerNombre());
        }
    }
    
    /**
     * Crea un nuevo contexto con este como padre,
     * y enlaza los símbolos dados a los valores dados.
     */
    public contexto extender(ExpresionLisp parametros, ExpresionLisp argumentos) throws ExcepcionLisp {
        contexto nuevoContexto = new contexto(this);
        
        ExpresionLisp parametroActual = parametros;
        ExpresionLisp argumentoActual = argumentos;
        
        while (parametroActual != simbolo.NULO && argumentoActual != simbolo.NULO) {
            if (parametroActual.esAtomo()) {
                // Número variable de argumentos (parámetro rest)
                nuevoContexto.establecer((simbolo) parametroActual, argumentoActual);
                break;
            }
            
            nuevoContexto.establecer((simbolo) parametroActual.primero(), argumentoActual.primero());
            parametroActual = parametroActual.resto();
            argumentoActual = argumentoActual.resto();
        }
        
        return nuevoContexto;
    }
}
