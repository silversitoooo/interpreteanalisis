package lisp;

import java.io.PrintStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Representa un valor numérico en LISP.
 */
public class numero extends atomo {
    private static final Map<Long, numero> cacheNumeros = new ConcurrentHashMap<>();
    
    // Números comunes para caché
    public static final numero CERO = obtenerValor(0);
    public static final numero UNO = obtenerValor(1);
    
    private final long valor;
    
    private numero(long valor) {
        this.valor = valor;
    }
    
    /**
     * Retorna una instancia de Numero con el valor dado.
     * Almacena en caché números comúnmente usados para eficiencia.
     */
    public static numero obtenerValor(long valor) {
        return cacheNumeros.computeIfAbsent(valor, numero::new);
    }
    
    /**
     * Obtiene el valor de este número.
     */
    public long obtenerValor() {
        return valor;
    }
    
    @Override
    public boolean esNumero() {
        return true;
    }
    
    @Override
    public void imprimir(PrintStream salida) {
        salida.print(valor);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof numero)) return false;
        return valor == ((numero) obj).valor;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(valor);
    }
}
