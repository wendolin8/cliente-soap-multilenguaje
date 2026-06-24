use strict;
use warnings;
use HTTP::Daemon;
use HTTP::Response;
use LWP::UserAgent;

my $server = HTTP::Daemon->new(
    LocalPort => 8110,
    ReuseAddr => 1
) or die "No se pudo iniciar el servidor";

print "Servidor Perl version1 en http://localhost:8110/?n=10\n";

while (my $conexion = $server->accept) {

    while (my $request = $conexion->get_request) {

        my $numero = 10;

        if ($request->uri->query =~ /n=(\d+)/) {
            $numero = $1;
        }

        my $xml = <<"XML";
<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
<soap:Body>
<NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
<ubiNum>$numero</ubiNum>
</NumberToWords>
</soap:Body>
</soap:Envelope>
XML

        my $cliente = LWP::UserAgent->new();

        my $respuesta = $cliente->post(
            'https://www.dataaccess.com/webservicesserver/NumberConversion.wso',
            Content_Type => 'text/xml; charset=utf-8',
            SOAPAction => 'http://www.dataaccess.com/webservicesserver/NumberToWords',
            Content => $xml
        );

        my $texto = $respuesta->decoded_content;

        my $resultado = '';

        if ($texto =~ /<m:NumberToWordsResult>(.*?)<\/m:NumberToWordsResult>/s) {
            $resultado = $1;
        }
        elsif ($texto =~ /<NumberToWordsResult>(.*?)<\/NumberToWordsResult>/s) {
            $resultado = $1;
        }

        my $response = HTTP::Response->new(
            200,
            'OK',
            ['Content-Type' => 'text/plain'],
            $resultado
        );

        $conexion->send_response($response);
    }

    $conexion->close;
}