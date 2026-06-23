const express = require("express");

const app = express();

const unidades = [
    "", "uno", "dos", "tres", "cuatro", "cinco",
    "seis", "siete", "ocho", "nueve", "diez",
    "once", "doce", "trece", "catorce", "quince",
    "dieciséis", "diecisiete", "dieciocho", "diecinueve"
];

const decenas = [
    "", "", "veinte", "treinta", "cuarenta", "cincuenta",
    "sesenta", "setenta", "ochenta", "noventa"
];

function numeroALetras(n) {
    n = parseInt(n);

    if (n === 0) return "cero";
    if (n < 20) return unidades[n];
    if (n < 30) return n === 20 ? "veinte" : "veinti" + unidades[n - 20];
    if (n < 100) {
        let d = Math.floor(n / 10);
        let u = n % 10;
        return u === 0 ? decenas[d] : decenas[d] + " y " + unidades[u];
    }
    if (n === 100) return "cien";
    if (n < 200) return "ciento " + numeroALetras(n - 100);
    if (n < 1000) {
        let c = Math.floor(n / 100);
        let resto = n % 100;
        let centenas = ["", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"];
        return resto === 0 ? centenas[c] : centenas[c] + " " + numeroALetras(resto);
    }

    return "Número fuera de rango";
}

app.get("/", (req, res) => {
    const numero = req.query.n || 10;
    const resultado = numeroALetras(numero);
    res.send(resultado);
});

app.listen(3002, () => {
    console.log("Servidor en http://localhost:3002");
});