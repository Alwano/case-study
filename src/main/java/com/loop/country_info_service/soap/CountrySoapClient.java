
package com.loop.country_info_service.soap;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class CountrySoapClient {

    private static final String ENDPOINT =
            "http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso";

    private static final String SOAP_NS =
            "http://schemas.xmlsoap.org/soap/envelope/";

    private static final String SERVICE_NS =
            "http://www.oorsprong.org/websamples.countryinfo";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String getIsoCode(String countryName) {
        String body = """
            <web:CountryISOCode>
                <web:sCountryName>%s</web:sCountryName>
            </web:CountryISOCode>
            """.formatted(escapeXml(countryName));

        Document response = sendSoapRequest(body);

        String isoCode = getFirstText(response, "CountryISOCodeResult");

        if (isoCode == null || isoCode.isBlank()) {
            throw new SoapIntegrationException(
                    "No ISO code found for country: " + countryName
            );
        }

        return isoCode.trim();
    }

    public CountrySoapData getFullCountryInfo(String isoCode) {
        String body = """
            <web:FullCountryInfo>
                <web:sCountryISOCode>%s</web:sCountryISOCode>
            </web:FullCountryInfo>
            """.formatted(escapeXml(isoCode));

        Document response = sendSoapRequest(body);

        Element result = getFirstElement(response, "FullCountryInfoResult");

        if (result == null) {
            throw new SoapIntegrationException(
                    "FullCountryInfo returned no country data for: " + isoCode
            );
        }

        List<LanguageData> languages = new ArrayList<>();
        NodeList languageNodes =
                result.getElementsByTagNameNS("*", "tLanguage");

        for (int i = 0; i < languageNodes.getLength(); i++) {
            Element languageElement = (Element) languageNodes.item(i);

            languages.add(new LanguageData(
                    childText(languageElement, "sISOCode"),
                    childText(languageElement, "sName")
            ));
        }

        return new CountrySoapData(
                childText(result, "sISOCode"),
                childText(result, "sName"),
                childText(result, "sCapitalCity"),
                childText(result, "sPhoneCode"),
                childText(result, "sContinentCode"),
                childText(result, "sCurrencyISOCode"),
                childText(result, "sCountryFlag"),
                languages
        );
    }

    private Document sendSoapRequest(String operationBody) {
        String envelope = """
            <?xml version="1.0" encoding="utf-8"?>
            <soapenv:Envelope
                xmlns:soapenv="%s"
                xmlns:web="%s">
                <soapenv:Header/>
                <soapenv:Body>
                    %s
                </soapenv:Body>
            </soapenv:Envelope>
            """.formatted(SOAP_NS, SERVICE_NS, operationBody);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "text/xml; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(envelope))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            Document document = parseXml(response.body());

            Element fault = getFirstElement(document, "Fault");
            if (fault != null) {
                throw new SoapIntegrationException(
                        "SOAP fault: " + fault.getTextContent().trim()
                );
            }

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new SoapIntegrationException(
                        "SOAP service returned HTTP " + response.statusCode()
                );
            }

            return document;

        } catch (SoapIntegrationException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SoapIntegrationException(
                    "SOAP request was interrupted", e
            );
        } catch (Exception e) {
            throw new SoapIntegrationException(
                    "Could not call or parse the country SOAP service", e
            );
        }
    }

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl", true
        );
        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities", false
        );
        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities", false
        );
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        return factory.newDocumentBuilder().parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))
        );
    }

    private String getFirstText(Document document, String localName) {
        Element element = getFirstElement(document, localName);
        return element == null ? null : element.getTextContent().trim();
    }

    private Element getFirstElement(Document document, String localName) {
        NodeList nodes = document.getElementsByTagNameNS("*", localName);
        return nodes.getLength() == 0 ? null : (Element) nodes.item(0);
    }

    private String childText(Element parent, String localName) {
        NodeList nodes = parent.getElementsByTagNameNS("*", localName);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getParentNode() == parent) {
                return node.getTextContent().trim();
            }
        }

        return "";
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public record CountrySoapData(
            String isoCode,
            String name,
            String capitalCity,
            String phoneCode,
            String continentCode,
            String currencyIsoCode,
            String flagUrl,
            List<LanguageData> languages
    ) {}

    public record LanguageData(String isoCode, String name) {}
}