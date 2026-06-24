use strict;
use warnings;
use HTTP::Daemon;
use HTTP::Response;

sub convertir {
    my ($n) = @_;

    my @unidades = ("cero","uno","dos","tres","cuatro","cinco","seis","siete","ocho","nueve","diez","once","doce","trece","catorce","quince","dieciséis","diecisiete","dieciocho","diecinueve");
    my @decenas = ("","","veinte","treinta","cuarenta","cincuenta","sesenta","setenta","ochenta","noventa");
    my @centenas = ("","ciento","doscientos","trescientos","cuatrocientos","quinientos","seiscientos","setecientos","ochocientos","novecientos");

    return $unidades[$n] if $n < 20;
    return "veinte" if $n == 20;
    return "veinti" . $unidades[$n - 20] if $n < 30;
    return $decenas[int($n / 10)] if $n < 100 && $n % 10 == 0;
    return $decenas[int($n / 10)] . " y " . $unidades[$n % 10] if $n < 100;
    return "cien" if $n == 100;
    return $centenas[int($n / 100)] if $n < 1000 && $n % 100 == 0;
    return $centenas[int($n / 100)] . " " . convertir($n % 100) if $n < 1000;

    if ($n < 1000000) {
        my $miles = int($n / 1000);
        my $resto = $n % 1000;

        my $texto_miles = $miles == 1 ? "mil" : convertir($miles) . " mil";

        return $texto_miles if $resto == 0;
        return $texto_miles . " " . convertir($resto);
    }

    return "Número fuera de rango";
}

my $server = HTTP::Daemon->new(LocalPort => 8112, ReuseAddr => 1)
    or die "No se pudo iniciar el servidor";

print "Servidor Perl version3 en http://localhost:8112/?n=10\n";

while (my $conexion = $server->accept) {
    while (my $request = $conexion->get_request) {

        my $numero = 10;

        if ($request->uri->query =~ /n=(\d+)/) {
            $numero = $1;
        }

        my $resultado = convertir($numero);

        my $response = HTTP::Response->new(
            200,
            'OK',
            ['Content-Type' => 'text/plain; charset=utf-8'],
            $resultado
        );

        $conexion->send_response($response);
    }

    $conexion->close;
}