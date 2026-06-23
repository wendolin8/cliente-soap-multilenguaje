package com.soap;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class App {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        server.createContext("/", (HttpExchange exchange) -> {
            try {
                String query = exchange.getRequestURI().getQuery();
                String numero = obtenerParametro(query, "n");

                String textoIngles = consumirSOAP(numero);
                String textoEspanol = traducir(textoIngles);

                responder(exchange, textoEspanol);

            } catch (Exception e) {
    try {
        responder(exchange, "Error: " + e.getMessage());
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}
        });

        server.start();
        System.out.println("Servidor Java version2 en http://localhost:8081/?n=10");
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
            inicio += "<NumberToWordsResult>".length();
        } else {
            inicio += "<m:NumberToWordsResult>".length();
        }

        return respuesta.substring(inicio, fin).trim();
    }

    public static String traducir(String texto) throws Exception {
        HttpClient cliente = HttpClient.newHttpClient();

        String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&q="
                + URLEncoder.encode(texto, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = cliente.send(request, HttpResponse.BodyHandlers.ofString());

        String json = response.body();

        int inicio = json.indexOf("[[[\"");
        int fin = json.indexOf("\",\"", inicio + 4);

        return json.substring(inicio + 4, fin);
    }

    public static void responder(HttpExchange exchange, String mensaje) throws Exception {
        byte[] respuesta = mensaje.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, respuesta.length);
        OutputStream os = exchange.getResponseBody();
        os.write(respuesta);
        os.close();
    }
}