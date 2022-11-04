package com.mrmorais.cars_race;

import io.vertx.core.json.JsonObject;

public class CarRaceBroadcastMessage {
  static enum EventType {UPDATE_GUESTS_INFO, UPDATE_TEAMS_SCORES, START_CLICKING, STOP_CLICKING};

  static JsonObject updateGuestsInfo(int redTeamSize, int greenTeamSize) {
    return JsonObject.of(
      "type", EventType.UPDATE_GUESTS_INFO,
      "data", JsonObject.of(
        "redTeamSize", redTeamSize,
        "greenTeamSize", greenTeamSize
      )
    );
  }

  static JsonObject updateTeamsScores(int redTeamScore, int greenTeamScore) {
    return JsonObject.of(
      "type", EventType.UPDATE_TEAMS_SCORES,
      "data", JsonObject.of(
        "redTeamScore", redTeamScore,
        "greenTeamScore", greenTeamScore
      )
    );
  }

  static JsonObject startClicking(int duration) {
    return JsonObject.of(
      "type", EventType.START_CLICKING,
      "data", JsonObject.of(
        "duration", duration
      )
    );
  }

  static JsonObject stopClicking() {
    return JsonObject.of(
      "type", EventType.STOP_CLICKING,
      "data", JsonObject.of()
    );
  }
}
