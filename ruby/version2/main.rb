require 'webrick'
require 'net/http'
require 'uri'

server = WEBrick::HTTPServer.new(
  Port: 8101
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

  uri = URI('https://www.dataaccess.com/webservicesserver/NumberConversion.wso')

  respuesta = Net::HTTP.post(
    uri,
    xml,
    {
      'Content-Type' => 'text/xml; charset=utf-8',
      'SOAPAction' => 'http://www.dataaccess.com/webservicesserver/NumberToWords'
    }
  )

  cuerpo = respuesta.body

  ingles = cuerpo[
    /<m:NumberToWordsResult>(.*?)<\/m:NumberToWordsResult>/m,
    1
  ]

  if ingles.nil?
    ingles = cuerpo[
      /<NumberToWordsResult>(.*?)<\/NumberToWordsResult>/m,
      1
    ]
  end

  url_traductor = URI(
    'https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&q=' +
    URI.encode_www_form_component(ingles)
  )

  respuesta_traductor = Net::HTTP.get(url_traductor)

  espanol = respuesta_traductor[
    /\[\[\["(.*?)"/,
    1
  ]

  res.body = espanol
end

trap('INT') do
  server.shutdown
end

puts 'Servidor Ruby version2 en http://localhost:8101/?n=10'

server.start