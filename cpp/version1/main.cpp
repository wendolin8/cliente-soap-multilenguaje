#include <iostream>
#include <string>
#include <fstream>
#include <sstream>
#include <cstdio>
#include <winsock2.h>

#pragma comment(lib, "ws2_32.lib")

std::string ejecutarComando(const std::string& comando) {
    std::string resultado;
    char buffer[256];

    FILE* pipe = _popen(comando.c_str(), "r");

    if (!pipe) return "Error al ejecutar curl";

    while (fgets(buffer, sizeof(buffer), pipe) != nullptr) {
        resultado += buffer;
    }

    _pclose(pipe);
    return resultado;
}

std::string extraerResultado(const std::string& texto) {
    std::string inicioTag = "<m:NumberToWordsResult>";
    std::string finTag = "</m:NumberToWordsResult>";

    size_t inicio = texto.find(inicioTag);
    size_t fin = texto.find(finTag);

    if (inicio == std::string::npos || fin == std::string::npos) {
        inicioTag = "<NumberToWordsResult>";
        finTag = "</NumberToWordsResult>";
        inicio = texto.find(inicioTag);
        fin = texto.find(finTag);
    }

    if (inicio == std::string::npos || fin == std::string::npos) {
        return "No se encontro resultado";
    }

    inicio += inicioTag.length();
    return texto.substr(inicio, fin - inicio);
}

std::string consumirSOAP(const std::string& numero) {
    std::ofstream archivo("soap.xml");

    archivo << "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            << "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            << "<soap:Body>"
            << "<NumberToWords xmlns=\"http://www.dataaccess.com/webservicesserver/\">"
            << "<ubiNum>" << numero << "</ubiNum>"
            << "</NumberToWords>"
            << "</soap:Body>"
            << "</soap:Envelope>";

    archivo.close();

    std::string comando =
        "curl -s -X POST "
        "-H \"Content-Type: text/xml; charset=utf-8\" "
        "-H \"SOAPAction: http://www.dataaccess.com/webservicesserver/NumberToWords\" "
        "--data-binary @soap.xml "
        "https://www.dataaccess.com/webservicesserver/NumberConversion.wso";

    std::string respuesta = ejecutarComando(comando);
    return extraerResultado(respuesta);
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
    direccion.sin_port = htons(8130);

    bind(servidor, (sockaddr*)&direccion, sizeof(direccion));
    listen(servidor, 5);

    std::cout << "Servidor C++ version1 en http://localhost:8130/?n=10" << std::endl;

    while (true) {
        SOCKET cliente = accept(servidor, nullptr, nullptr);

        char buffer[4096] = {0};
        recv(cliente, buffer, sizeof(buffer), 0);

        std::string request(buffer);
        std::string numero = obtenerNumero(request);
        std::string resultado = consumirSOAP(numero);

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