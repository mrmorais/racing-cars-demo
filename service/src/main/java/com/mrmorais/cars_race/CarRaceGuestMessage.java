package com.mrmorais.cars_race;

import io.vertx.core.json.JsonObject;

public class CarRaceGuestMessage {
  static enum EventType {SET_GUEST_TEAM, SET_GUEST_AS_HOST};

  static JsonObject setGuestTeam(CarRaceTeam team) {
    return JsonObject.of(
      "type", EventType.SET_GUEST_TEAM,
      "data", JsonObject.of("team", team.name())
    );
  }

  static JsonObject setGuestAsHost(String startKey) {
    return JsonObject.of(
      "type", EventType.SET_GUEST_AS_HOST,
      "data", JsonObject.of("startKey", startKey)
    );
  }
}
