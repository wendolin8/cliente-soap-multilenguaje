require 'webrick'

def convertir(n)
  unidades = [
    'cero', 'uno', 'dos', 'tres', 'cuatro', 'cinco',
    'seis', 'siete', 'ocho', 'nueve', 'diez',
    'once', 'doce', 'trece', 'catorce', 'quince',
    'dieciséis', 'diecisiete', 'dieciocho', 'diecinueve'
  ]

  decenas = ['', '', 'veinte', 'treinta', 'cuarenta', 'cincuenta',
             'sesenta', 'setenta', 'ochenta', 'noventa']

  centenas = ['', 'ciento', 'doscientos', 'trescientos', 'cuatrocientos',
              'quinientos', 'seiscientos', 'setecientos', 'ochocientos', 'novecientos']

  return unidades[n] if n < 20
  return 'veinte' if n == 20
  return 'veinti' + unidades[n - 20] if n < 30
  return decenas[n / 10] if n < 100 && n % 10 == 0
  return decenas[n / 10] + ' y ' + unidades[n % 10] if n < 100
  return 'cien' if n == 100
  return centenas[n / 100] if n < 1000 && n % 100 == 0
  return centenas[n / 100] + ' ' + convertir(n % 100) if n < 1000

  if n < 1_000_000
    miles = n / 1000
    resto = n % 1000

    texto_miles = miles == 1 ? 'mil' : convertir(miles) + ' mil'
    return texto_miles if resto == 0
    return texto_miles + ' ' + convertir(resto)
  end

  'Número fuera de rango'
end

server = WEBrick::HTTPServer.new(
  Port: 8102
)

server.mount_proc '/' do |req, res|
  numero = req.query['n'].to_i
  res.body = convertir(numero)
end

trap('INT') do
  server.shutdown
end

puts 'Servidor Ruby version3 en http://localhost:8102/?n=10'

server.start