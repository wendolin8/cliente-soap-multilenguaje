package main

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
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

		req, _ := http.NewRequest(
			"POST",
			"https://www.dataaccess.com/webservicesserver/NumberConversion.wso",
			strings.NewReader(xml),
		)

		req.Header.Set("Content-Type", "text/xml; charset=utf-8")
		req.Header.Set("SOAPAction", "http://www.dataaccess.com/webservicesserver/NumberToWords")

		cliente := &http.Client{}

		resp, err := cliente.Do(req)

		if err != nil {
			fmt.Fprint(w, err)
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

			inicio += len("<NumberToWordsResult>")

		} else {

			inicio += len("<m:NumberToWordsResult>")
		}

		ingles := strings.TrimSpace(respuesta[inicio:fin])

		urlTraductor := "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&dt=t&q=" + url.QueryEscape(ingles)

		respTraductor, err := http.Get(urlTraductor)

		if err != nil {
			fmt.Fprint(w, err)
			return
		}

		defer respTraductor.Body.Close()

		bodyTraductor, _ := io.ReadAll(respTraductor.Body)

		var datos []interface{}

		json.Unmarshal(bodyTraductor, &datos)

		espanol := datos[0].([]interface{})[0].([]interface{})[0].(string)

		fmt.Fprint(w, espanol)

	})

	fmt.Println("Servidor Go version2 en http://localhost:8091/?n=10")

	http.ListenAndServe(":8091", nil)
}
