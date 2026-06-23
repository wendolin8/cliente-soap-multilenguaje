const express = require("express");
const axios = require("axios");
const xml2js = require("xml2js");

const app = express();

app.get("/", async (req, res) => {
    const numero = req.query.n || 10;

    const xml = `<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
      <ubiNum>${numero}</ubiNum>
    </NumberToWords>
  </soap:Body>
</soap:Envelope>`;

    try {
        const respuesta = await axios.post(
            "https://www.dataaccess.com/webservicesserver/NumberConversion.wso",
            xml,
            {
                headers: {
                    "Content-Type": "text/xml; charset=utf-8",
                    "SOAPAction": "http://www.dataaccess.com/webservicesserver/NumberToWords"
                }
            }
        );

        const resultado = await xml2js.parseStringPromise(respuesta.data, {
            explicitArray: false
        });

        const texto =
            resultado["soap:Envelope"]["soap:Body"]
            ["m:NumberToWordsResponse"]
            ["m:NumberToWordsResult"];

        res.send(texto);

    } catch (error) {
        console.log(error.message);
        res.send("Error: " + error.message);
    }
});

app.listen(3000, () => {
    console.log("Servidor en http://localhost:3000");
});