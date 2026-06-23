package main

import (
	"fmt"
	"io"
	"net/http"
	"strings"
)

func main() {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		numero := r.URL.Query().Get("n")

		xml := fmt.Sprintf(`<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
      <ubiNum>%s</ubiNum>
    </NumberToWords>
  </soap:Body>
</soap:Envelope>`, numero)

		req, err := http.NewRequest(
			"POST",
			"https://www.dataaccess.com/webservicesserver/NumberConversion.wso",
			strings.NewReader(xml),
		)

		if err != nil {
			fmt.Fprint(w, "Error: ", err)
			return
		}

		req.Header.Set("Content-Type", "text/xml; charset=utf-8")
		req.Header.Set("SOAPAction", "http://www.dataaccess.com/webservicesserver/NumberToWords")

		cliente := &http.Client{}
		resp, err := cliente.Do(req)

		if err != nil {
			fmt.Fprint(w, "Error: ", err)
			return
		}

		defer resp.Body.Close()

		body, _ := io.ReadAll(resp.Body)
		respuesta := string(body)

		inicio := strings.Index(respuesta, "<m:NumberToWordsResult>")
		fin := strings.Index(respuesta, "</m:NumberToWordsResult>")

		if inicio == -1 || fin == -1 {
			inicio = strings.Index(respuesta, "<NumberToWordsResult>")
			fin = strings.Index(respuesta, "</NumberToWordsResult>")

			if inicio == -1 || fin == -1 {
				fmt.Fprint(w, "No se encontró resultado")
				return
			}

			inicio += len("<NumberToWordsResult>")
		} else {
			inicio += len("<m:NumberToWordsResult>")
		}

		resultado := strings.TrimSpace(respuesta[inicio:fin])
		fmt.Fprint(w, resultado)
	})

	fmt.Println("Servidor Go version1 en http://localhost:8090/?n=10")
	http.ListenAndServe(":8090", nil)
}