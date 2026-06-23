package com.soap;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.ibm.icu.text.RuleBasedNumberFormat;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class App {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8082), 0);

        server.createContext("/", (HttpExchange exchange) -> {
            try {
                String query = exchange.getRequestURI().getQuery();
                String numeroTexto = obtenerParametro(query, "n");

                int numero = Integer.parseInt(numeroTexto);

                RuleBasedNumberFormat formateador =
                        new RuleBasedNumberFormat(new Locale("es"), RuleBasedNumberFormat.SPELLOUT);

                String resultado = formateador.format(numero);

                responder(exchange, resultado);

            } catch (Exception e) {
                try {
                    responder(exchange, "Error: " + e.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        server.start();
        System.out.println("Servidor Java version3 en http://localhost:8082/?n=10");
    }

    public static String obtenerParametro(String query, String nombre) {
        if (query == null) return null;

        String[] parametros = query.split("&");

        for (String parametro : parametros) {
            String[] partes = parametro.split("=");

            if (partes.length == 2 && partes[0].equals(nombre)) {
                return URLDecoder.decode(partes[1], StandardCharsets.UTF_8);
            }
        }

        return null;
    }

    public static void responder(HttpExchange exchange, String mensaje) throws Exception {
        byte[] respuesta = mensaje.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, respuesta.length);
        OutputStream os = exchange.getResponseBody();
        os.write(respuesta);
        os.close();
    }
}