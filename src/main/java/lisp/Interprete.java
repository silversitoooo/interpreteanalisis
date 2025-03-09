package lisp;

import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase principal para el intérprete LISP.
 */
public class Interprete {
    private final contexto contextoGlobal;
    private final lisp.analizador analizador;
    private final PrintStream salida;

    /**
     * Crea un nuevo intérprete LISP.
     */
    public Interprete() {
        this.contextoGlobal = crearContextoGlobal();
        this.analizador = new analizador(new InputStreamReader(System.in));
        this.salida = System.out;
    }

    /**
     * Evalúa una expresión LISP.
     *
     * @param expr La expresión a evaluar
     * @param ctx El contexto en el que evaluar la expresión
     * @return El resultado de evaluar la expresión
     * @throws ExcepcionLisp si hay un error durante la evaluación
     */
    public ExpresionLisp evaluar(ExpresionLisp expr, contexto ctx) throws ExcepcionLisp {
        // Evalúa átomos
        if (expr.esAtomo()) {
            if (expr.esSimbolo()) {
                simbolo sim = (simbolo) expr;
                if (sim == simbolo.NULO || sim == simbolo.VERDADERO) {
                    return sim; // Auto-evaluación
                }
                return ctx.obtener(sim);
            } else {
                return expr; // Los números se evalúan a sí mismos
            }
        }

        // Evalúa listas
        ExpresionLisp primero = expr.primero();
        ExpresionLisp resto = expr.resto();

        // Maneja formas especiales
        if (primero.esSimbolo()) {
            simbolo op = (simbolo) primero;

            if (op == simbolo.CITAR) {
                // (CITAR expr) => expr
                return resto.primero();
            } else if (op == simbolo.ASIGNAR) {
                // (ASIGNAR var expr)
                simbolo var = (simbolo) resto.primero();
                ExpresionLisp valor = evaluar(resto.resto().primero(), ctx);
                ctx.establecer(var, valor);
                return valor;
            } else if (op == simbolo.DEFINIR_FUNCION) {
                // (DEFINIR_FUNCION nombre params cuerpo)
                simbolo nombre = (simbolo) resto.primero();
                ExpresionLisp params = resto.resto().primero();
                ExpresionLisp cuerpo = resto.resto().resto().primero();

                Funcion func = new Funcion(params, cuerpo, ctx);
                ctx.establecer(nombre, func);
                return nombre;
            } else if (op == simbolo.CONDICIONAL) {
                // (CONDICIONAL (cond1 expr1) (cond2 expr2) ...)
                ExpresionLisp clausulas = resto;
                while (clausulas != simbolo.NULO) {
                    ExpresionLisp clausula = clausulas.primero();
                    ExpresionLisp condicion = clausula.primero();
                    ExpresionLisp resultado = evaluar(condicion, ctx);

                    if (resultado != simbolo.NULO) {
                        // La condición es verdadera, evalúa la expresión
                        return evaluar(clausula.resto().primero(), ctx);
                    }

                    clausulas = clausulas.resto();
                }
                return simbolo.NULO;
            } else if (op == simbolo.IMPRIMIR) {
                // (IMPRIMIR expr)
                ExpresionLisp valor = evaluar(resto.primero(), ctx);
                valor.imprimir(salida);
                salida.println();
                return valor;
            }
        }

        // Aplicación de función
        ExpresionLisp funcion = evaluar(primero, ctx);
        List<ExpresionLisp> args = new ArrayList<>();

        // Evalúa argumentos
        ExpresionLisp listaArgs = resto;
        while (listaArgs != simbolo.NULO) {
            args.add(evaluar(listaArgs.primero(), ctx));
            listaArgs = listaArgs.resto();
        }

        return aplicar(funcion, args);
    }

    /**
     * Aplica una función a argumentos.
     *
     * @param funcion La función a aplicar
     * @param args Los argumentos a aplicar a la función
     * @return El resultado de aplicar la función
     * @throws ExcepcionLisp si hay un error durante la aplicación
     */
    private ExpresionLisp aplicar(ExpresionLisp funcion, List<ExpresionLisp> args) throws ExcepcionLisp {
        if (funcion instanceof Funcion) {
            // Función definida por el usuario
            Funcion func = (Funcion) funcion;
            return func.aplicar(args, this);
        } else if (funcion.esSimbolo()) {
            // Función incorporada
            simbolo op = (simbolo) funcion;

            if (op == simbolo.PRIMERO) {
                verificarCantidadArgumentos(args, 1);
                return args.get(0).primero();
            } else if (op == simbolo.RESTO) {
                verificarCantidadArgumentos(args, 1);
                return args.get(0).resto();
            } else if (op == simbolo.CONSTRUIR) {
                verificarCantidadArgumentos(args, 2);
                return new par(args.get(0), args.get(1));
            } else if (op == simbolo.LISTA) {
                // Convierte lista de args a una lista LISP adecuada
                ExpresionLisp resultado = simbolo.NULO;
                for (int i = args.size() - 1; i >= 0; i--) {
                    resultado = new par(args.get(i), resultado);
                }
                return resultado;
            } else if (op == simbolo.ES_IGUAL_REF) {
                verificarCantidadArgumentos(args, 2);
                return args.get(0) == args.get(1) ? simbolo.VERDADERO : simbolo.NULO;
            } else if (op == simbolo.ES_IGUAL) {
                verificarCantidadArgumentos(args, 2);
                return esIgual(args.get(0), args.get(1)) ? simbolo.VERDADERO : simbolo.NULO;
            } else if (op == simbolo.ES_ATOMO) {
                verificarCantidadArgumentos(args, 1);
                return args.get(0).esAtomo() ? simbolo.VERDADERO : simbolo.NULO;
            } else if (op == simbolo.SUMAR) {
                long resultado = 0;
                for (ExpresionLisp arg : args) {
                    if (!arg.esNumero()) {
                        throw new ExcepcionLisp("+ requiere argumentos numéricos");
                    }
                    resultado += ((numero) arg).obtenerValor();
                }
                return numero.obtenerValor(resultado);
            } else if (op == simbolo.RESTAR) {
                if (args.size() == 0) {
                    throw new ExcepcionLisp("- requiere al menos un argumento");
                }

                if (!args.get(0).esNumero()) {
                    throw new ExcepcionLisp("- requiere argumentos numéricos");
                }

                if (args.size() == 1) {
                    // Menos unario
                    return numero.obtenerValor(-((numero) args.get(0)).obtenerValor());
                }

                // Menos binario
                long resultado = ((numero) args.get(0)).obtenerValor();
                for (int i = 1; i < args.size(); i++) {
                    if (!args.get(i).esNumero()) {
                        throw new ExcepcionLisp("- requiere argumentos numéricos");
                    }
                    resultado -= ((numero) args.get(i)).obtenerValor();
                }
                return numero.obtenerValor(resultado);
            } else if (op == simbolo.MULTIPLICAR) {
                long resultado = 1;
                for (ExpresionLisp arg : args) {
                    if (!arg.esNumero()) {
                        throw new ExcepcionLisp("* requiere argumentos numéricos");
                    }
                    resultado *= ((numero) arg).obtenerValor();
                }
                return numero.obtenerValor(resultado);
            } else if (op == simbolo.DIVIDIR) {
                if (args.size() == 0) {
                    throw new ExcepcionLisp("/ requiere al menos un argumento");
                }

                if (!args.get(0).esNumero()) {
                    throw new ExcepcionLisp("/ requiere argumentos numéricos");
                }

                if (args.size() == 1) {
                    // Inversión
                    long valor = ((numero) args.get(0)).obtenerValor();
                    if (valor == 0) {
                        throw new ExcepcionLisp("División por cero");
                    }
                    return numero.obtenerValor(1 / valor);
                }

                // División normal
                long resultado = ((numero) args.get(0)).obtenerValor();
                for (int i = 1; i < args.size(); i++) {
                    if (!args.get(i).esNumero()) {
                        throw new ExcepcionLisp("/ requiere argumentos numéricos");
                    }
                    long divisor = ((numero) args.get(i)).obtenerValor();
                    if (divisor == 0) {
                        throw new ExcepcionLisp("División por cero");
                    }
                    resultado /= divisor;
                }
                return numero.obtenerValor(resultado);
            } else if (op == simbolo.MENOR_QUE) {
                verificarCantidadArgumentos(args, 2);
                if (!args.get(0).esNumero() || !args.get(1).esNumero()) {
                    throw new ExcepcionLisp("< requiere argumentos numéricos");
                }
                long a = ((numero) args.get(0)).obtenerValor();
                long b = ((numero) args.get(1)).obtenerValor();
                return a < b ? simbolo.VERDADERO : simbolo.NULO;
            } else if (op == simbolo.MAYOR_QUE) {
                verificarCantidadArgumentos(args, 2);
                if (!args.get(0).esNumero() || !args.get(1).esNumero()) {
                    throw new ExcepcionLisp("> requiere argumentos numéricos");
                }
                long a = ((numero) args.get(0)).obtenerValor();
                long b = ((numero) args.get(1)).obtenerValor();
                return a > b ? simbolo.VERDADERO : simbolo.NULO;
            } else {
                throw new ExcepcionLisp("Función desconocida: " + op.obtenerNombre());
            }
        } else {
            throw new ExcepcionLisp("No se puede aplicar: " + funcion);
        }
    }

    /**
     * Verifica que el número de argumentos sea igual al esperado.
     */
    private void verificarCantidadArgumentos(List<ExpresionLisp> args, int esperado) throws ExcepcionLisp {
        if (args.size() != esperado) {
            throw new ExcepcionLisp("Se esperaban " + esperado + " argumentos, se recibieron " + args.size());
        }
    }

    /**
     * Comprueba si dos expresiones S son iguales en valor.
     */
    private boolean esIgual(ExpresionLisp a, ExpresionLisp b) {
        if (a == b) {
            return true;
        }
        
        if (a.esAtomo() && b.esAtomo()) {
            if (a.esNumero() && b.esNumero()) {
                return ((numero) a).obtenerValor() == ((numero) b).obtenerValor();
            }
            return false; // Los símbolos se comparan por referencia (==)
        }
        
        if (!a.esAtomo() && !b.esAtomo()) {
            try {
                // Recursivamente comparar listas
                return esIgual(a.primero(), b.primero()) && esIgual(a.resto(), b.resto());
            } catch (ExcepcionLisp e) {
                return false;
            }
        }
        
        return false; // Un átomo y una lista nunca son iguales
    }

    /**
     * Crea un contexto global con las definiciones estándar.
     */
    private contexto crearContextoGlobal() {
        contexto ctx = new contexto();
        
        // Definir constantes
        ctx.establecer(simbolo.NULO, simbolo.NULO);
        ctx.establecer(simbolo.VERDADERO, simbolo.VERDADERO);
        
        // Definir funciones incorporadas
        ctx.establecer(simbolo.PRIMERO, simbolo.PRIMERO);
        ctx.establecer(simbolo.RESTO, simbolo.RESTO);
        ctx.establecer(simbolo.CONSTRUIR, simbolo.CONSTRUIR);
        ctx.establecer(simbolo.LISTA, simbolo.LISTA);
        ctx.establecer(simbolo.ES_ATOMO, simbolo.ES_ATOMO);
        ctx.establecer(simbolo.ES_IGUAL_REF, simbolo.ES_IGUAL_REF);
        ctx.establecer(simbolo.ES_IGUAL, simbolo.ES_IGUAL);
        ctx.establecer(simbolo.IMPRIMIR, simbolo.IMPRIMIR);
        
        // Definir operadores aritméticos
        ctx.establecer(simbolo.SUMAR, simbolo.SUMAR);
        ctx.establecer(simbolo.RESTAR, simbolo.RESTAR);
        ctx.establecer(simbolo.MULTIPLICAR, simbolo.MULTIPLICAR);
        ctx.establecer(simbolo.DIVIDIR, simbolo.DIVIDIR);
        
        // Definir operadores de comparación
        ctx.establecer(simbolo.MENOR_QUE, simbolo.MENOR_QUE);
        ctx.establecer(simbolo.MAYOR_QUE, simbolo.MAYOR_QUE);
        
        return ctx;
    }

    /**
     * Ejecuta el intérprete en un bucle leer-evaluar-imprimir (REPL).
     */
    public void repl() {
        salida.println("Intérprete LISP");
        salida.println("Escribe expresiones LISP para evaluar, Ctrl+D para salir");
        
        while (true) {
            try {
                salida.print("> ");
                salida.flush();
                
                ExpresionLisp expr = analizador.analizar();
                if (expr == null) {
                    break; // Fin de entrada
                }
                
                ExpresionLisp resultado = evaluar(expr, contextoGlobal);
                salida.print("=> ");
                resultado.imprimir(salida);
                salida.println();
            } catch (ExcepcionLisp e) {
                salida.println("Error: " + e.getMessage());
            }
        }
        
        salida.println("¡Adiós!");
    }

    /**
     * Evalúa una expresión LISP de cadena.
     *
     * @param expr La expresión a evaluar como cadena
     * @return El resultado de evaluar la expresión
     * @throws ExcepcionLisp si hay un error durante la evaluación
     */
    public ExpresionLisp evaluar(String expr) throws ExcepcionLisp {
        lisp.analizador analizadorTemp = new analizador(new StringReader(expr));
        ExpresionLisp s = analizadorTemp.analizar();
        if (s == null) {
            throw new ExcepcionLisp("Expresión vacía");
        }
        return evaluar(s, contextoGlobal);
    }

    /**
     * Clase interna para representar funciones definidas por el usuario.
     */
    private class Funcion extends ExpresionLisp {
        private final ExpresionLisp parametros;
        private final ExpresionLisp cuerpo;
        private final contexto cierreLexico;
        
        public Funcion(ExpresionLisp parametros, ExpresionLisp cuerpo, contexto cierreLexico) {
            this.parametros = parametros;
            this.cuerpo = cuerpo;
            this.cierreLexico = cierreLexico;
        }
        
        public ExpresionLisp aplicar(List<ExpresionLisp> args, Interprete interprete) throws ExcepcionLisp {
            // Convierte la lista de args a una lista LISP adecuada
            ExpresionLisp listaArgs = simbolo.NULO;
            for (int i = args.size() - 1; i >= 0; i--) {
                listaArgs = new par(args.get(i), listaArgs);
            }
            
            // Crea un nuevo contexto extendido con los parámetros enlazados a los argumentos
            contexto nuevoContexto = cierreLexico.extender(parametros, listaArgs);
            
            // Evalúa el cuerpo de la función en el nuevo contexto
            return interprete.evaluar(cuerpo, nuevoContexto);
        }
        
        @Override
        public void imprimir(PrintStream salida) {
            salida.print("#<FUNCION>");
        }
        
        @Override
        public ExpresionLisp primero() throws ExcepcionLisp {
            throw new ExcepcionLisp("No se puede obtener el primer elemento de una función");
        }
        
        @Override
        public ExpresionLisp resto() throws ExcepcionLisp {
            throw new ExcepcionLisp("No se puede obtener el resto de una función");
        }
    }

    /**
     * Método principal.
     */
    public static void main(String[] args) {
        Interprete interprete = new Interprete();
        interprete.repl();
    }
}
