use std::io::{Read, Write};
use std::net::{TcpListener, TcpStream};

fn convertir(n: i32) -> String {
    let unidades = [
        "cero", "uno", "dos", "tres", "cuatro", "cinco",
        "seis", "siete", "ocho", "nueve", "diez",
        "once", "doce", "trece", "catorce", "quince",
        "dieciséis", "diecisiete", "dieciocho", "diecinueve",
    ];

    let decenas = [
        "", "", "veinte", "treinta", "cuarenta", "cincuenta",
        "sesenta", "setenta", "ochenta", "noventa",
    ];

    let centenas = [
        "", "ciento", "doscientos", "trescientos", "cuatrocientos",
        "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos",
    ];

    if n < 20 {
        return unidades[n as usize].to_string();
    }

    if n == 20 {
        return "veinte".to_string();
    }

    if n < 30 {
        return format!("veinti{}", unidades[(n - 20) as usize]);
    }

    if n < 100 {
        let d = n / 10;
        let u = n % 10;

        if u == 0 {
            return decenas[d as usize].to_string();
        }

        return format!("{} y {}", decenas[d as usize], unidades[u as usize]);
    }

    if n == 100 {
        return "cien".to_string();
    }

    if n < 1000 {
        let c = n / 100;
        let resto = n % 100;

        if resto == 0 {
            return centenas[c as usize].to_string();
        }

        return format!("{} {}", centenas[c as usize], convertir(resto));
    }

    if n < 1_000_000 {
        let miles = n / 1000;
        let resto = n % 1000;

        let texto_miles = if miles == 1 {
            "mil".to_string()
        } else {
            format!("{} mil", convertir(miles))
        };

        if resto == 0 {
            return texto_miles;
        }

        return format!("{} {}", texto_miles, convertir(resto));
    }

    "Número fuera de rango".to_string()
}

fn manejar_cliente(mut stream: TcpStream) {
    let mut buffer = [0; 2048];
    stream.read(&mut buffer).unwrap();

    let peticion = String::from_utf8_lossy(&buffer);

    let numero_texto = peticion
        .split("GET /?n=")
        .nth(1)
        .and_then(|s| s.split(' ').next())
        .unwrap_or("10");

    let numero: i32 = numero_texto.parse().unwrap_or(10);

    let resultado = convertir(numero);

    let respuesta = format!(
        "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\n\r\n{}",
        resultado
    );

    stream.write_all(respuesta.as_bytes()).unwrap();
}

fn main() {
    let listener = TcpListener::bind("127.0.0.1:8122").unwrap();

    println!("Servidor Rust version3 en http://localhost:8122/?n=10");

    for stream in listener.incoming() {
        manejar_cliente(stream.unwrap());
    }
}