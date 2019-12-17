package com.nextgeneducation.kafka;

import java.util.Date;
import java.util.Properties;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;


/**
 * Stock Price Producer
 *
 */
public class StockPriceProducer {
    public static void main( String[] args ) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: StockPriceProducer data.tsv");
            System.out.println("where data.tsv is a tab-delimited file with the following fields:");
            System.out.println("Symbol\tName\tPrice\tChange\t%Change");
            System.out.println("ATVI\tActivision Blizzard Inc\t58.65\t0.09\t%0.15%");
            System.exit(-1);
        }


        String dataFileName = args[0];
        HashMap<String, Float> stockPrices = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(dataFileName));
        String line = br.readLine(); // First line is a header
        while ((line = br.readLine()) != null) {
            String[] fields = line.split("\t");
            stockPrices.put(fields[0], new Float(fields[2]));
        }

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);

        boolean done = false;

        while (!done) {
            for (Map.Entry<String, Float> entry : stockPrices.entrySet()) {
                producer.send(new ProducerRecord<String, String>("stock_prices", entry.getKey(), entry.getKey() + "\t" + Float.toString(entry.getValue())));
                entry.setValue(entry.getValue() + (entry.getValue() * (-0.5F + ((float) Math.random()))) / 1000);
            }
            System.out.println("Updated Prices @" + new Date());
            producer.flush();
            Thread.sleep(5000);
        }

        producer.close();

    }
}
