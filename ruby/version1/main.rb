require 'webrick'
require 'net/http'

server = WEBrick::HTTPServer.new(
  Port: 8100
)

server.mount_proc '/' do |req, res|

  numero = req.query['n']

  xml = <<~XML
  <?xml version="1.0" encoding="utf-8"?>
  <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
    <soap:Body>
      <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
        <ubiNum>#{numero}</ubiNum>
      </NumberToWords>
    </soap:Body>
  </soap:Envelope>
  XML

  uri = URI(
    'https://www.dataaccess.com/webservicesserver/NumberConversion.wso'
  )

  respuesta = Net::HTTP.post(
    uri,
    xml,
    {
      'Content-Type' => 'text/xml; charset=utf-8',
      'SOAPAction' => 'http://www.dataaccess.com/webservicesserver/NumberToWords'
    }
  )

  cuerpo = respuesta.body

  resultado = cuerpo[
    /<m:NumberToWordsResult>(.*?)<\/m:NumberToWordsResult>/m,
    1
  ]

  if resultado.nil?

    resultado = cuerpo[
      /<NumberToWordsResult>(.*?)<\/NumberToWordsResult>/m,
      1
    ]

  end

  res.body = resultado
end

trap('INT') do
  server.shutdown
end

puts 'Servidor Ruby version1 en http://localhost:8100/?n=10'

server.start