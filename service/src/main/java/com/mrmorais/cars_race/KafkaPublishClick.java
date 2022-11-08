package com.mrmorais.cars_race;

import io.vertx.core.json.JsonObject;

public class KafkaPublishClick {
  static JsonObject from(String sessionKey, CarRaceTeam team) {
    return JsonObject.of(
      "session", sessionKey,
      "team", team
    );
  }
}
