package com.mrmorais.cars_race;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.HashMap;
import java.util.Map;

public class KafkaBridgeVerticle extends AbstractVerticle {
  private KafkaProducer<String, String> producer;
  private KafkaConsumer<String, String> consumer;

  @Override
  public void start(Promise<Void> startPromise) {
    System.out.println("Starting Kafka Bridge");
    try {
      this.producer = this.createKafkaProducer();
      this.consumer = this.createKafkaConsumer();

      EventBus eb = vertx.eventBus();

      eb.consumer("kafka.publish.click", (message) -> {
        var session = JsonObject.mapFrom(message.body()).getString("session");

        var record = KafkaProducerRecord.create(
          "cars_race_clicks",
          session,
          message.body().toString()
        );
        this.producer.send(record);
      });

      this.consumer.handler(record -> {
        var key = JsonObject.mapFrom(Json.decodeValue(record.key()));
        var value = JsonObject.mapFrom(Json.decodeValue(record.value()));

        eb.publish(
          String.format("%s.update.score", key.getString("SESSION")),
          JsonObject.of("team", key.getString("TEAM"), "score", value.getInteger("SCORE"))
        );
      });

      this.consumer.subscribe("pksqlc-8dwkmCAR_RACE_SCORES");

      System.out.println("Kafka Bridge is Up " + this.producer.toString() + this.consumer);
      startPromise.complete();
    } catch (Exception error) {
      startPromise.fail(error);
    }
  }

  private KafkaProducer<String, String> createKafkaProducer() {
    Map<String, String> producerConfig = new HashMap<>();
    producerConfig.put("bootstrap.servers", AppConfigs.getConfig().kafkaBootstrapServers);
    producerConfig.put("security.protocol", "SASL_SSL");
    producerConfig.put("sasl.mechanism", "PLAIN");
    producerConfig.put("sasl.jaas.config", String.format("org.apache.kafka.common.security.plain.PlainLoginModule   required username='%s'   password='%s';", AppConfigs.getConfig().kafkaUsername, AppConfigs.getConfig().kafkaPassword));
    producerConfig.put("client.dns.lookup", "use_all_dns_ips");
    producerConfig.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    producerConfig.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    producerConfig.put("linger.ms", "0");
    producerConfig.put("acks", "0");

    return KafkaProducer.create(vertx, producerConfig);
  }

  private KafkaConsumer<String, String> createKafkaConsumer() {
    Map<String, String> consumerConfig = new HashMap<>();
    consumerConfig.put("bootstrap.servers", AppConfigs.getConfig().kafkaBootstrapServers);
    consumerConfig.put("security.protocol", "SASL_SSL");
    consumerConfig.put("sasl.mechanism", "PLAIN");
    consumerConfig.put("sasl.jaas.config", String.format("org.apache.kafka.common.security.plain.PlainLoginModule   required username='%s'   password='%s';", AppConfigs.getConfig().kafkaUsername, AppConfigs.getConfig().kafkaPassword));
    consumerConfig.put("client.dns.lookup", "use_all_dns_ips");
    consumerConfig.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    consumerConfig.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    consumerConfig.put("group.id", AppConfigs.getConfig().kafkaGroupId);
    consumerConfig.put("fetch.min.bytes", "1");
    consumerConfig.put("auto.offset.reset", "latest");
    consumerConfig.put("enable.auto.commit", "true");

    return KafkaConsumer.create(vertx, consumerConfig);
  }
}
