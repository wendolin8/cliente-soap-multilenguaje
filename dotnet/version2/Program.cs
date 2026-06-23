using System.Net;
using System.Text.Json;
using System.Xml.Linq;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

app.MapGet("/", async (int n) =>
{
    string xml = $@"<?xml version=""1.0"" encoding=""utf-8""?>
<soap:Envelope xmlns:soap=""http://schemas.xmlsoap.org/soap/envelope/"">
  <soap:Body>
    <NumberToWords xmlns=""http://www.dataaccess.com/webservicesserver/"">
      <ubiNum>{n}</ubiNum>
    </NumberToWords>
  </soap:Body>
</soap:Envelope>";

    using var cliente = new HttpClient();

    var contenido = new StringContent(
        xml,
        System.Text.Encoding.UTF8,
        "text/xml"
    );

    var respuesta = await cliente.PostAsync(
        "https://www.dataaccess.com/webservicesserver/NumberConversion.wso",
        contenido
    );

    var resultadoXml = await respuesta.Content.ReadAsStringAsync();

    XNamespace ns = "http://www.dataaccess.com/webservicesserver/";

    var documento = XDocument.Parse(resultadoXml);

    var textoIngles = documento
        .Descendants(ns + "NumberToWordsResult")
        .First()
        .Value
        .Trim();

    string urlTraductor =
        "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&q="
        + WebUtility.UrlEncode(textoIngles);

    var respuestaTraductor = await cliente.GetStringAsync(urlTraductor);

    var json = JsonDocument.Parse(respuestaTraductor);

    var textoEspanol = json.RootElement[0][0][0].GetString();

    return textoEspanol;
});

app.Run();