package main

import (
	"fmt"
	"net/http"
	"strconv"
)

func unidades(n int) string {
	u := []string{"cero", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve", "diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve"}
	return u[n]
}

func convertir(n int) string {
	decenas := []string{"", "", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"}
	centenas := []string{"", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"}

	if n < 20 {
		return unidades(n)
	}
	if n < 30 {
		if n == 20 {
			return "veinte"
		}
		return "veinti" + unidades(n-20)
	}
	if n < 100 {
		if n%10 == 0 {
			return decenas[n/10]
		}
		return decenas[n/10] + " y " + unidades(n%10)
	}
	if n == 100 {
		return "cien"
	}
	if n < 1000 {
		if n%100 == 0 {
			return centenas[n/100]
		}
		return centenas[n/100] + " " + convertir(n%100)
	}
	if n < 1000000 {
		miles := n / 1000
		resto := n % 1000

		textoMiles := ""
		if miles == 1 {
			textoMiles = "mil"
		} else {
			textoMiles = convertir(miles) + " mil"
		}

		if resto == 0 {
			return textoMiles
		}

		return textoMiles + " " + convertir(resto)
	}

	return "Número fuera de rango"
}

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		numeroTexto := r.URL.Query().Get("n")
		numero, err := strconv.Atoi(numeroTexto)

		if err != nil {
			fmt.Fprint(w, "Debes proporcionar un número válido. Ejemplo: http://localhost:8092/?n=10")
			return
		}

		fmt.Fprint(w, convertir(numero))
	})

	fmt.Println("Servidor Go version3 en http://localhost:8092/?n=10")
	http.ListenAndServe(":8092", nil)
}