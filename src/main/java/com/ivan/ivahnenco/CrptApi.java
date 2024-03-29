package com.ivan.ivahnenco;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CrptApi {
    private static final Logger logger = Logger.getLogger(CrptApi.class.getName());
    private static Document documentToSend;

    public static final String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    public static final String METHOD = "POST";

    static {
        ArrayList<Product> products = new ArrayList<>();
        Product product = new Product(
                "string",
                new Date(), "string", "string", "string", new Date(), "string", "string", "string");
        products.add(product);

        documentToSend = new Document();

        documentToSend.setDescription(new Description("string"));
        documentToSend.setId("string");
        documentToSend.setStatus("string");
        documentToSend.setType("string");
        documentToSend.setImportRequest(true);
        documentToSend.setOwnerInn("string");
        documentToSend.setParticipantInn("string");
        documentToSend.setProducerInn("string");
        documentToSend.setProductionDate(new Date());
        documentToSend.setProductionType("string");
        documentToSend.setProducts(products);
        documentToSend.setRegDate(new Date());
        documentToSend.setRegNumber("string");

    }

    public static void main(String[] args) {
        DocumentSender sender = new DocumentSender(TimeUnit.SECONDS, 16);
        logger.log(Level.INFO, new DocumentSerializer().serialize(documentToSend));

        sender.sendSeveral(documentToSend, "signature");
    }
}

// Uses Jackson for serialize document to json string
class DocumentSerializer {
    public String serialize(Document document) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(document);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        return null;
    }
}

// Send serialized Document onbject
class DocumentSender {
    private final Logger logger = Logger.getLogger(DocumentSender.class.getName());
    private TimeUnit timeUnit;
    private int requestLimit;
    private URI URI;
    private String METHOD = "POST";
    private HttpClient httpClient;

    public DocumentSender(TimeUnit timeUnit, int requestLimit) {
        try {
            this.URI = new URI("https://ismp.crpt.ru/api/v3/lk/documents/create");
        } catch (URISyntaxException e) {
            logger.log(Level.WARNING, "Error", e);
            Thread.currentThread().interrupt();
        }
        this.requestLimit = requestLimit;
        this.timeUnit = timeUnit;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void send(Document document, String signature) {
        HttpRequest request = HttpRequest
                .newBuilder(URI)
                .method(METHOD,
                        BodyPublishers.ofString(new DocumentSerializer().serialize(document)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            logger.log(Level.INFO, response.body());
        } catch (IOException e) {
            logger.log(Level.WARNING, "error", e);
            e.printStackTrace();

        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Inerropting", e);
            Thread.currentThread().interrupt();
        }

    }

    public void sendSeveral(Document document, String signature) {
        Long k = 0L;
        boolean b = true;
        while (b) {
            k++;
            b = k < Long.MAX_VALUE;

            for (int i = 0; i < this.requestLimit; i++)
                send(document, signature);

            try {
                timeUnit.sleep(1);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Error", e);
                Thread.currentThread().interrupt();
            }
        }

    }
}

/**
 * Data classes
 */
class Description {
    private String participantInn;

    public Description(String participantInn) {
        this.participantInn = participantInn;
    }

    public String getParticipantInn() {
        return participantInn;
    }

    public void setParticipantInn(String participantInn) {
        this.participantInn = participantInn;
    }
}

class Product {
    @JsonProperty("certificate_document")
    String certificateDocument;

    @JsonProperty("certificate_document_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-mm-dd")
    Date certificateDocumentDate;

    @JsonProperty("certificate_document_number")
    String certificateDocumentNumber;

    @JsonProperty("owner_inn")
    String ownerInn;

    @JsonProperty("producer_inn")
    String producerInn;

    @JsonProperty("production_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-mm-dd")
    Date productionDate;

    @JsonProperty("tnved_code")
    String tnvedCode;

    @JsonProperty("uit_code")
    String uitCode;

    @JsonProperty("uitu_code")
    String uituCode;

    public Product() {

    }

    public Product(String certificateDocument, Date certificateDocumentDate, String certificateDocumentNumber,
            String ownerInn, String producerInn, Date productionDate, String tnvedCode, String uitCode,
            String uituCode) {
        this.certificateDocument = certificateDocument;
        this.certificateDocumentDate = certificateDocumentDate;
        this.certificateDocumentNumber = certificateDocumentNumber;
        this.ownerInn = ownerInn;
        this.producerInn = producerInn;
        this.productionDate = productionDate;
        this.tnvedCode = tnvedCode;
        this.uitCode = uitCode;
        this.uituCode = uituCode;
    }

    public String getCertificateDocument() {
        return certificateDocument;
    }

    public void setCertificateDocument(String certificateDocument) {
        this.certificateDocument = certificateDocument;
    }

    public Date getCertificateDocumentDate() {
        return certificateDocumentDate;
    }

    public void setCertificateDocumentDate(Date certificateDocumentDate) {
        this.certificateDocumentDate = certificateDocumentDate;
    }

    public String getCertificateDocumentNumber() {
        return certificateDocumentNumber;
    }

    public void setCertificateDocumentNumber(String certificateDocumentNumber) {
        this.certificateDocumentNumber = certificateDocumentNumber;
    }

    public String getOwnerInn() {
        return ownerInn;
    }

    public void setOwnerInn(String ownerInn) {
        this.ownerInn = ownerInn;
    }

    public String getProducerInn() {
        return producerInn;
    }

    public void setProducerInn(String producerInn) {
        this.producerInn = producerInn;
    }

    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    public String getTnvedCode() {
        return tnvedCode;
    }

    public void setTnvedCode(String tnvedCode) {
        this.tnvedCode = tnvedCode;
    }

    public String getUitCode() {
        return uitCode;
    }

    public void setUitCode(String uitCode) {
        this.uitCode = uitCode;
    }

    public String getUituCode() {
        return uituCode;
    }

    public void setUituCode(String uituCode) {
        this.uituCode = uituCode;
    }
}

class Document {
    private Description description;
    @JsonProperty("doc_id")
    private String id;

    @JsonProperty("doc_status")
    private String status;

    @JsonProperty("doc_type")
    private String type;

    private boolean importRequest;

    @JsonProperty("owner_inn")
    private String ownerInn;

    @JsonProperty("participant_inn")
    private String participantInn;

    @JsonProperty("producer_inn")
    private String producerInn;

    @JsonProperty("production_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-mm-dd")
    private Date productionDate;

    @JsonProperty("production_type")
    private String productionType;

    private ArrayList<Product> products;

    @JsonProperty("")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-mm-dd")
    private Date regDate;

    @JsonProperty("")
    private String regNumber;

    public Document() {

    }

    public Document(Description description, String id, String status, String type, boolean importRequest,
            String ownerInn, String participantInn, String producerInn, Date productionDate, String productionType,
            ArrayList<Product> products, Date regDate, String regNumber) {
        this.description = description;
        this.id = id;
        this.status = status;
        this.type = type;
        this.importRequest = importRequest;
        this.ownerInn = ownerInn;
        this.participantInn = participantInn;
        this.producerInn = producerInn;
        this.productionDate = productionDate;
        this.productionType = productionType;
        this.products = products;
        this.regDate = regDate;
        this.regNumber = regNumber;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isImportRequest() {
        return importRequest;
    }

    public void setImportRequest(boolean importRequest) {
        this.importRequest = importRequest;
    }

    public String getOwnerInn() {
        return ownerInn;
    }

    public void setOwnerInn(String ownerInn) {
        this.ownerInn = ownerInn;
    }

    public String getParticipantInn() {
        return participantInn;
    }

    public void setParticipantInn(String participantInn) {
        this.participantInn = participantInn;
    }

    public String getProducerInn() {
        return producerInn;
    }

    public void setProducerInn(String producerInn) {
        this.producerInn = producerInn;
    }

    public Date getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(Date productionDate) {
        this.productionDate = productionDate;
    }

    public String getProductionType() {
        return productionType;
    }

    public void setProductionType(String productionType) {
        this.productionType = productionType;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }
}
