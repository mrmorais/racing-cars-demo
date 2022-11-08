package com.mrmorais.cars_race;

import io.vertx.core.json.JsonObject;
import jdk.jfr.Event;

public class CarRaceBroadcastMessage {
  static enum EventType {
    UPDATE_GUESTS_INFO,
    UPDATE_TEAMS_SCORES,
    UPDATE_GREEN_SCORE,
    UPDATE_RED_SCORE,
    START_CLICKING,
    STOP_CLICKING,
    ANNOUNCE_WINNER
  };

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

  static JsonObject updateTeamScore(CarRaceTeam team, int score) {
    return JsonObject.of(
      "type", team.equals(CarRaceTeam.GREEN_TEAM) ? EventType.UPDATE_GREEN_SCORE : EventType.UPDATE_RED_SCORE,
      "data", JsonObject.of(
        "score", score
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

  static JsonObject announceWinner(CarRaceTeam winner, int redScore, int greenScore) {
    return JsonObject.of(
      "type", EventType.ANNOUNCE_WINNER,
      "data", JsonObject.of(
        "winner", winner,
        "redScore", redScore,
        "greenScore", greenScore
      )
    );
  }
}
