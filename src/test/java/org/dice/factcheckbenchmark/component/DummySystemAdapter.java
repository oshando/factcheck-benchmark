package org.dice.factcheckbenchmark.component;

import org.dice.factcheckbenchmark.system.api.Client;
import org.dice.factcheckbenchmark.system.api.FactCheckHobbitResponse;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractSystemAdapter;
import org.hobbit.sdk.JenaKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;


public class DummySystemAdapter extends AbstractSystemAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DummySystemAdapter.class);
    private static JenaKeyValue parameters;
    private String databaseContainer;
    private String factcheckContainer;
    private String factcheckContainerUrl;

    @Override
    public void init() throws Exception {
        super.init();
        logger.debug("Init()");
        // Your initialization code comes here...

        parameters = new JenaKeyValue.Builder().buildFrom(systemParamModel);
        logger.debug("SystemModel: " + parameters.encodeToString());
        // You can access the RDF model this.systemParamModel to retrieve meta data about this system adapter


    }

    @Override
    public void receiveGeneratedData(byte[] data) {
        // handle the incoming data as described in the benchmark description
        String dataStr = new String(data);
        logger.trace("receiveGeneratedData(" + new String(data) + "): " + dataStr);

    }

    @Override
    public void receiveGeneratedTask(String taskId, byte[] data) {
        // handle the incoming task and create a result
        logger.debug("receiveGeneratedTask({})->{}", taskId, new String(data));

        final String REGEX_SEPARATOR = ":\\*:";
        String[] split = taskId.split(REGEX_SEPARATOR);
        String urlTaskId = split[0];
        String fileTrace = split[1];

        String url = "http://localhost:8080/api/hobbitTask/" + urlTaskId;
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("dataISWC", data);
        map.add("fileTrace", fileTrace);

        Client client = new Client(map, MediaType.MULTIPART_FORM_DATA, url);
        ResponseEntity<FactCheckHobbitResponse> response = client.getResponse(HttpMethod.POST);
        FactCheckHobbitResponse result = response.getBody();

        try {
            logger.debug("sendResultToEvalStorage({})->{}", taskId, result.getTruthValue());
            sendResultToEvalStorage(taskId, String.valueOf(result.getTruthValue()).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        // Free the resources you requested here
        logger.debug("close()");

        // Always close the super class after yours!
        super.close();
    }

}

