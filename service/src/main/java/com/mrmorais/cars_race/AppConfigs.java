package com.mrmorais.cars_race;

import com.typesafe.config.ConfigFactory;

public class AppConfigs {

  static class ConfigsData {
    public String kafkaBootstrapServers;
    public String kafkaUsername;
    public String kafkaPassword;
    public String kafkaGroupId;

    public ConfigsData(String kafkaBootstrapServers, String kafkaUsername, String kafkaPassword, String kafkaGroupId) {
      this.kafkaBootstrapServers = kafkaBootstrapServers;
      this.kafkaUsername = kafkaUsername;
      this.kafkaPassword = kafkaPassword;
      this.kafkaGroupId = kafkaGroupId;
    }
  }

  static ConfigsData getConfig() {
    var conf = ConfigFactory.load();

    return new ConfigsData(
      conf.getString("kafka.bootstrapServers"),
      conf.getString("kafka.username"),
      conf.getString("kafka.password"),
      conf.getString("kafka.groupId")
    );
  }
}
