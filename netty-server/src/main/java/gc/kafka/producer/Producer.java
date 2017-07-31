package gc.kafka.producer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.connect.json.JsonSerializer;

import java.util.Properties;

public class Producer {
	public void pushJSON(String jsonstring) {
		try {
		String topicName = "KAFKA_TOPIC";

		// Configure the Producer
		Properties configProperties = new Properties();
		configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		configProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		configProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		org.apache.kafka.clients.producer.Producer producer = new KafkaProducer(configProperties);
		ProducerRecord<String, String> rec = new ProducerRecord<String, String>(topicName, jsonstring);
		producer.send(rec);
		System.out.println("Message Sent");
		producer.close();
		return;
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}