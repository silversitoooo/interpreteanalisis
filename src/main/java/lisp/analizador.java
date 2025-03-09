package lisp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Analizador para expresiones LISP.
 */
public class analizador {
    private final Tokenizador tokenizador;
    
    public analizador(Reader lector) {
        this.tokenizador = new Tokenizador(lector);
    }
    
    /**
     * Analiza una expresión LISP desde la entrada.
     * 
     * @return La expresión S analizada, o null al final de la entrada
     * @throws ExcepcionLisp si hay un error de sintaxis
     */
    public ExpresionLisp analizar() throws ExcepcionLisp {
        String token = tokenizador.siguienteToken();
        if (token == null) {
            return null; // Fin de entrada
        }
        
        return analizarToken(token);
    }
    
    private ExpresionLisp analizarToken(String token) throws ExcepcionLisp {
        switch (token) {
            case "(":
                return analizarLista();
            case ")":
                throw new ExcepcionLisp("Paréntesis de cierre inesperado");
            case "'":
                // Abreviatura de cita: 'x => (CITAR x)
                return new par(simbolo.CITAR, new par(analizar(), simbolo.NULO));
            default:
                return analizarAtomo(token);
        }
    }
    
    private ExpresionLisp analizarLista() throws ExcepcionLisp {
        String token = tokenizador.siguienteToken();
        if (token == null) {
            throw new ExcepcionLisp("Fin de entrada inesperado, falta un paréntesis de cierre");
        }
        
        if (token.equals(")")) {
            return simbolo.NULO; // Lista vacía
        }
        
        ExpresionLisp primero = analizarToken(token);
        
        token = tokenizador.siguienteToken();
        if (token == null) {
            throw new ExcepcionLisp("Fin de entrada inesperado, falta un paréntesis de cierre");
        }
        
        if (token.equals(".")) {
            // Notación de par punteado
            ExpresionLisp resto = analizar();
            token = tokenizador.siguienteToken();
            if (!token.equals(")")) {
                throw new ExcepcionLisp("Se esperaba un paréntesis de cierre después del par punteado");
            }
            return new par(primero, resto);
        } else {
            // Lista regular
            tokenizador.devolver(token);
            ExpresionLisp resto = analizarLista();
            return new par(primero, resto);
        }
    }
    
    private ExpresionLisp analizarAtomo(String token) {
        // Intenta analizar como número
        try {
            long valor = Long.parseLong(token);
            return numero.obtenerValor(valor);
        } catch (NumberFormatException e) {
            // Si no es un número, es un símbolo
            return simbolo.internamente(token);
        }
    }
    
    /**
     * Tokenizador para expresiones LISP.
     */
    private static class Tokenizador {
        private final BufferedReader lector;
        private String tokenDevuelto = null;
        
        public Tokenizador(Reader lector) {
            this.lector = lector instanceof BufferedReader ? 
                (BufferedReader) lector : 
                new BufferedReader(lector);
        }
        
        /**
         * Devuelve el siguiente token de la entrada.
         * 
         * @return El siguiente token, o null al final de la entrada
         * @throws ExcepcionLisp si hay un error de E/S
         */
        public String siguienteToken() throws ExcepcionLisp {
            if (tokenDevuelto != null) {
                String token = tokenDevuelto;
                tokenDevuelto = null;
                return token;
            }
            
            try {
                saltarEspaciosEnBlanco();
                
                int c = lector.read();
                if (c == -1) {
                    return null; // Fin de entrada
                }
                
                char ch = (char) c;
                
                // Maneja tokens de un solo carácter
                switch (ch) {
                    case '(':
                    case ')':
                    case '\'':
                    case '.':
                        return String.valueOf(ch);
                    case ';':
                        // Omitir comentario
                        lector.readLine();
                        return siguienteToken();
                }
                
                // Maneja tokens de múltiples caracteres (símbolos y números)
                StringBuilder sb = new StringBuilder();
                sb.append(ch);
                
                lector.mark(1);
                c = lector.read();
                while (c != -1 && !esDelimitador((char) c)) {
                    sb.append((char) c);
                    lector.mark(1);
                    c = lector.read();
                }
                
                if (c != -1) {
                    lector.reset(); // Devuelve el delimitador
                }
                
                return sb.toString();
            } catch (IOException e) {
                throw new ExcepcionLisp("Error de E/S: " + e.getMessage());
            }
        }
        
        /**
         * Devuelve un token para ser retornado en la próxima llamada a siguienteToken().
         */
        public void devolver(String token) {
            tokenDevuelto = token;
        }
        
        private void saltarEspaciosEnBlanco() throws IOException {
            lector.mark(1);
            int c = lector.read();
            while (c != -1 && Character.isWhitespace((char) c)) {
                lector.mark(1);
                c = lector.read();
            }
            if (c != -1) {
                lector.reset();
            }
        }
        
        private boolean esDelimitador(char c) {
            return Character.isWhitespace(c) || c == '(' || c == ')' || c == '\'' || c == ';';
        }
    }
}
