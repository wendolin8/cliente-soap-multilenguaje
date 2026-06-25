use std::io::{Read, Write};
use std::net::{TcpListener, TcpStream};

fn manejar_cliente(mut stream: TcpStream) {
    let mut buffer = [0; 2048];
    stream.read(&mut buffer).unwrap();

    let peticion = String::from_utf8_lossy(&buffer);

    let numero = peticion
        .split("GET /?n=")
        .nth(1)
        .and_then(|s| s.split(' ').next())
        .unwrap_or("10");

    let ingles = consumir_soap(numero);
    let espanol = traducir(&ingles);

    let respuesta = format!(
        "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\n\r\n{}",
        espanol
    );

    stream.write_all(respuesta.as_bytes()).unwrap();
}

fn consumir_soap(numero: &str) -> String {
    let xml = format!(
r#"<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
<soap:Body>
<NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
<ubiNum>{}</ubiNum>
</NumberToWords>
</soap:Body>
</soap:Envelope>"#, numero);

    let cliente = reqwest::blocking::Client::new();

    let respuesta = cliente
        .post("https://www.dataaccess.com/webservicesserver/NumberConversion.wso")
        .header("Content-Type", "text/xml; charset=utf-8")
        .header("SOAPAction", "http://www.dataaccess.com/webservicesserver/NumberToWords")
        .body(xml)
        .send()
        .unwrap()
        .text()
        .unwrap();

    if let Some(inicio) = respuesta.find("<m:NumberToWordsResult>") {
        let inicio = inicio + "<m:NumberToWordsResult>".len();
        let fin = respuesta.find("</m:NumberToWordsResult>").unwrap();
        return respuesta[inicio..fin].trim().to_string();
    }

    let inicio = respuesta.find("<NumberToWordsResult>").unwrap()
        + "<NumberToWordsResult>".len();

    let fin = respuesta.find("</NumberToWordsResult>").unwrap();

    respuesta[inicio..fin].trim().to_string()
}

fn traducir(texto: &str) -> String {
    let texto_codificado = urlencoding::encode(texto);

    let url = format!(
        "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&q={}",
        texto_codificado
    );

    let respuesta = reqwest::blocking::get(url)
        .unwrap()
        .text()
        .unwrap();

    let inicio = respuesta.find("[[[\"").unwrap() + 4;
    let fin = respuesta[inicio..].find("\",\"").unwrap() + inicio;

    respuesta[inicio..fin].to_string()
}

fn main() {
    let listener = TcpListener::bind("127.0.0.1:8121").unwrap();

    println!("Servidor Rust version2 en http://localhost:8121/?n=10");

    for stream in listener.incoming() {
        manejar_cliente(stream.unwrap());
    }
}
