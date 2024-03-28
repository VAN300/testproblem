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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CrptApi {
    private static final Logger logger = Logger.getLogger(CrptApi.class.getName());
    private final TimeUnit timeUnit;
    private final int requestLimit;
    public static final String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    public static final String METHOD = "POST";

    private static Document documentToSend;

    static {
        ArrayList<Product> arrayList = new ArrayList<>();
        arrayList.add(new Product(METHOD, new Date(), METHOD, METHOD, METHOD, new Date(), METHOD, URL, METHOD));

        documentToSend = new Document(
                new Description(METHOD),
                METHOD, METHOD, METHOD, false, METHOD, METHOD, METHOD, new Date(), URL,
                arrayList, new Date(), METHOD);
    }

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
    }

    public void request(Document document) {
        for (int i = 0; i < this.requestLimit; i++) {
            DocumentSender sender = new DocumentSender(CrptApi.URL, CrptApi.METHOD);
            sender.send(document);
        }
        try {
            timeUnit.sleep(1);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Error", e);
            Thread.currentThread().interrupt();
        }

    }

    public static void main(String[] args) {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 16);
        logger.log(Level.INFO, new DocumentSerializer().serialize(documentToSend));
        Long k = 0L;
        boolean b = true;
        while (b) {
            k++;
            b = k < Long.MAX_VALUE;
            api.request(documentToSend);
        }
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
    private final Logger logger = Logger.getLogger(CrptApi.class.getName());
    private URI URI;
    private String METHOD;

    public DocumentSender(String url, String method) {
        try {
            this.URI = new URI(url);
        } catch (URISyntaxException e) {
            logger.log(Level.WARNING, "Error", e);
            Thread.currentThread().interrupt();
        }
        this.METHOD = method.toUpperCase();
    }

    public void send(Document document) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest
                .newBuilder(URI)
                .method(METHOD,
                        BodyPublishers.ofString(new DocumentSerializer().serialize(document)))
                .build();

        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            logger.log(Level.INFO, response.body());
        } catch (IOException e) {
            e.printStackTrace();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

/**
 * Records
 */
record Product(
        String certificateDocument, Date certificateDocumentDate,
        String certificateDocumentNumber, String ownerInn, String producerInn, Date productionDate,
        String tnvedCode, String uitCode, String uituCode) {
}

record Description(String participantInn) {
}

record Document(
        Description description,

        String id,
        String status,
        String type,
        boolean importRequest,
        String ownerInn,
        String participantInn,
        String producerInn,
        Date production_date,
        String productionType,

        ArrayList<Product> products,
        Date regDate,
        String regNumbe) {
}
