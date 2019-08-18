package me.zhangjin.lucene.demo.es.api;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;

public class TestClient {
    private static String CLUSTER_NAME = "kkking";
    private static String HOST_IP = "111.231.82.204";
    private static int TCP_PORT = 19300;

    public static void main(String[] args) throws Exception {
        Settings settings = Settings.builder()
                .put("cluster.name", CLUSTER_NAME)
                .build();

        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(HOST_IP), TCP_PORT));

        var response = client.prepareGet("books", "IT", "1").get();

        System.out.println(response.getSourceAsString());
    }

}
