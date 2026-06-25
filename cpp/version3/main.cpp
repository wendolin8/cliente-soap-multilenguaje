#include <iostream>
#include <string>
#include <winsock2.h>

#pragma comment(lib, "ws2_32.lib")

std::string convertir(int n) {
    std::string unidades[] = {
        "cero", "uno", "dos", "tres", "cuatro", "cinco",
        "seis", "siete", "ocho", "nueve", "diez",
        "once", "doce", "trece", "catorce", "quince",
        "dieciséis", "diecisiete", "dieciocho", "diecinueve"
    };

    std::string decenas[] = {
        "", "", "veinte", "treinta", "cuarenta", "cincuenta",
        "sesenta", "setenta", "ochenta", "noventa"
    };

    std::string centenas[] = {
        "", "ciento", "doscientos", "trescientos", "cuatrocientos",
        "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"
    };

    if (n < 20) return unidades[n];
    if (n == 20) return "veinte";
    if (n < 30) return "veinti" + unidades[n - 20];
    if (n < 100 && n % 10 == 0) return decenas[n / 10];
    if (n < 100) return decenas[n / 10] + " y " + unidades[n % 10];
    if (n == 100) return "cien";
    if (n < 1000 && n % 100 == 0) return centenas[n / 100];
    if (n < 1000) return centenas[n / 100] + " " + convertir(n % 100);

    if (n < 1000000) {
        int miles = n / 1000;
        int resto = n % 1000;

        std::string textoMiles = miles == 1 ? "mil" : convertir(miles) + " mil";

        if (resto == 0) return textoMiles;

        return textoMiles + " " + convertir(resto);
    }

    return "Número fuera de rango";
}

std::string obtenerNumero(const std::string& request) {
    size_t pos = request.find("GET /?n=");

    if (pos == std::string::npos) {
        return "10";
    }

    pos += 8;
    size_t fin = request.find(" ", pos);

    return request.substr(pos, fin - pos);
}

int main() {
    WSADATA wsaData;
    WSAStartup(MAKEWORD(2, 2), &wsaData);

    SOCKET servidor = socket(AF_INET, SOCK_STREAM, 0);

    sockaddr_in direccion;
    direccion.sin_family = AF_INET;
    direccion.sin_addr.s_addr = INADDR_ANY;
    direccion.sin_port = htons(8132);

    bind(servidor, (sockaddr*)&direccion, sizeof(direccion));
    listen(servidor, 5);

    std::cout << "Servidor C++ version3 en http://localhost:8132/?n=10" << std::endl;

    while (true) {
        SOCKET cliente = accept(servidor, nullptr, nullptr);

        char buffer[4096] = {0};
        recv(cliente, buffer, sizeof(buffer), 0);

        std::string request(buffer);
        std::string numeroTexto = obtenerNumero(request);
        int numero = std::stoi(numeroTexto);

        std::string resultado = convertir(numero);

        std::string respuesta =
            "HTTP/1.1 200 OK\r\n"
            "Content-Type: text/plain; charset=utf-8\r\n\r\n" +
            resultado;

        send(cliente, respuesta.c_str(), respuesta.size(), 0);
        closesocket(cliente);
    }

    closesocket(servidor);
    WSACleanup();

    return 0;
}