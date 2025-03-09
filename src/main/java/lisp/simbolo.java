package lisp;

import java.io.PrintStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Representa un símbolo en LISP.
 */
public class simbolo extends atomo {
    private static final Map<String, simbolo> tablaSimbolo = new ConcurrentHashMap<>();
    
    // Símbolos estándar
    public static final simbolo NULO = internamente("NULO");
    public static final simbolo VERDADERO = internamente("VERDADERO");
    
    // Formas especiales
    public static final simbolo CITAR = internamente("CITAR");
    public static final simbolo ASIGNAR = internamente("ASIGNAR");
    public static final simbolo DEFINIR_FUNCION = internamente("DEFINIR_FUNCION");
    public static final simbolo CONDICIONAL = internamente("CONDICIONAL");
    
    // Funciones estándar
    public static final simbolo PRIMERO = internamente("PRIMERO");
    public static final simbolo RESTO = internamente("RESTO");
    public static final simbolo CONSTRUIR = internamente("CONSTRUIR");
    public static final simbolo LISTA = internamente("LISTA");
    public static final simbolo ES_ATOMO = internamente("ES_ATOMO");
    public static final simbolo ES_IGUAL_REF = internamente("ES_IGUAL_REF");
    public static final simbolo ES_IGUAL = internamente("ES_IGUAL");
    public static final simbolo IMPRIMIR = internamente("IMPRIMIR");
    
    // Operadores aritméticos
    public static final simbolo SUMAR = internamente("+");
    public static final simbolo RESTAR = internamente("-");
    public static final simbolo MULTIPLICAR = internamente("*");
    public static final simbolo DIVIDIR = internamente("/");
    
    // Operadores de comparación
    public static final simbolo MENOR_QUE = internamente("<");
    public static final simbolo MAYOR_QUE = internamente(">");
    
    private final String nombre;
    
    private simbolo(String nombre) {
        this.nombre = nombre.toUpperCase();
    }
    
    /**
     * Retorna un símbolo con el nombre dado. Si ya existe un símbolo con este nombre,
     * retorna el existente.
     */
    public static simbolo internamente(String nombre) {
        return tablaSimbolo.computeIfAbsent(nombre.toUpperCase(), simbolo::new);
    }
    
    /**
     * Obtiene el nombre de este símbolo.
     */
    public String obtenerNombre() {
        return nombre;
    }
    
    @Override
    public boolean esSimbolo() {
        return true;
    }
    
    @Override
    public void imprimir(PrintStream salida) {
        salida.print(nombre);
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj; // Los símbolos son únicos (internalizados)
    }
    
    @Override
    public int hashCode() {
        return nombre.hashCode();
    }
}
