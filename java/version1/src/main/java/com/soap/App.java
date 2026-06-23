package com.soap;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class App {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", (HttpExchange exchange) -> {
            try {
                String query = exchange.getRequestURI().getQuery();
                String numero = obtenerParametro(query, "n");

                if (numero == null) {
                    numero = "10";
                }

                String resultado = consumirSOAP(numero);

                byte[] respuesta = resultado.getBytes(StandardCharsets.UTF_8);

                exchange.sendResponseHeaders(200, respuesta.length);
                OutputStream os = exchange.getResponseBody();
                os.write(respuesta);
                os.close();

            } catch (Exception e) {
                String error = "Error: " + e.getMessage();
                byte[] respuesta = error.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, respuesta.length);
                OutputStream os = exchange.getResponseBody();
                os.write(respuesta);
                os.close();
            }
        });

        server.start();

        System.out.println("Servidor Java version1 en http://localhost:8080/?n=10");
    }

    public static String obtenerParametro(String query, String nombre) {
        if (query == null) {
            return null;
        }

        String[] parametros = query.split("&");

        for (String parametro : parametros) {
            String[] partes = parametro.split("=");

            if (partes.length == 2 && partes[0].equals(nombre)) {
                return URLDecoder.decode(partes[1], StandardCharsets.UTF_8);
            }
        }

        return null;
    }

    public static String consumirSOAP(String numero) throws Exception {

        String xml = """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
              <ubiNum>%s</ubiNum>
            </NumberToWords>
          </soap:Body>
        </soap:Envelope>
        """.formatted(numero);

        HttpClient cliente = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.dataaccess.com/webservicesserver/NumberConversion.wso"))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "http://www.dataaccess.com/webservicesserver/NumberToWords")
                .POST(HttpRequest.BodyPublishers.ofString(xml))
                .build();

        HttpResponse<String> response = cliente.send(request, HttpResponse.BodyHandlers.ofString());

        String respuesta = response.body();

        int inicio = respuesta.indexOf("<m:NumberToWordsResult>");
        int fin = respuesta.indexOf("</m:NumberToWordsResult>");

        if (inicio == -1 || fin == -1) {
            inicio = respuesta.indexOf("<NumberToWordsResult>");
            fin = respuesta.indexOf("</NumberToWordsResult>");

            if (inicio == -1 || fin == -1) {
                return "No se encontró resultado";
            }

            inicio += "<NumberToWordsResult>".length();
        } else {
            inicio += "<m:NumberToWordsResult>".length();
        }

        return respuesta.substring(inicio, fin).trim();
    }
}