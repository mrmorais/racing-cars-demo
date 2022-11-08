package com.mrmorais.cars_race;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;

import java.util.*;

public class CarRaceVerticle extends AbstractVerticle {
  private String sessionCode;
  private CarRaceStatus status;

  private String startKey;

  private Set<String> redTeam = new HashSet<>();
  private Set<String> greenTeam = new HashSet<>();

  private int greenScore = 0;
  private int redScore = 0;

  private MessageProducer<JsonObject> broadcast;

  private EventBus eb;

  public CarRaceVerticle(String sessionCode) {
    this.sessionCode = sessionCode;
    this.startKey = UUID.randomUUID().toString();
    this.status = CarRaceStatus.LOBBYING;
  }

  @Override
  public void start() throws Exception {
    super.start();

    this.eb = vertx.eventBus();
    this.broadcast = this.eb.publisher(String.format("%s.all", this.sessionCode));

    this.eb.consumer(String.format("%s.register", this.sessionCode), (message -> {
      String guestId = JsonObject.mapFrom(message.body()).getString("guestId");

      this.assignGuest(guestId);
    }));

    this.eb.consumer(String.format("%s.click", this.sessionCode), (message -> {
      String guestId = JsonObject.mapFrom(message.body()).getString("guestId");

      this.bookClick(guestId);
    }));

    this.eb.consumer(String.format("%s.update.score", this.sessionCode), (message) -> {
      var team = JsonObject.mapFrom(message.body()).getString("team");
      var score = JsonObject.mapFrom(message.body()).getInteger("score");

      System.out.println(message.body());
      System.out.println(team);
      System.out.println(CarRaceTeam.GREEN_TEAM.name().equals(team));

      switch (CarRaceTeam.valueOf(team)) {
        case GREEN_TEAM:
          this.greenScore = score;
          break;
        case RED_TEAM:
          this.redScore = score;
          break;
      }

      this.broadcast.write(CarRaceBroadcastMessage.updateTeamScore(CarRaceTeam.valueOf(team), score));
    });

    this.eb.consumer(String.format("%s.start", this.sessionCode), (message -> {
      String passedStartKey = JsonObject.mapFrom(message.body()).getString("startKey");

      if (passedStartKey.equals(this.startKey)) {
        try {
          this.startSession();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }));

    System.out.println("CarRaceVerticle with session " + this.sessionCode + " started with status " + this.status.name());
  }

  @Override
  public void stop() throws Exception {
    super.stop();
  }

  private void assignGuest(String guestId) {
    boolean guestIsHost = this.redTeam.size() == 0 && this.greenTeam.size() == 0;
    CarRaceTeam team = this.redTeam.size() < this.greenTeam.size() ? CarRaceTeam.RED_TEAM : CarRaceTeam.GREEN_TEAM;

    switch (team) {
      case RED_TEAM:
        redTeam.add(guestId);
        break;
      case GREEN_TEAM:
        greenTeam.add(guestId);
        break;
    }

    System.out.println(String.format("Added %s to %s. Balance is now RED=%d, GREEN=%d", guestId, team.name(), redTeam.size(), greenTeam.size()));
    this.eb.publish(String.format("guest.%s.events", guestId), CarRaceGuestMessage.setGuestTeam(team));
    if (guestIsHost) this.eb.publish(String.format("guest.%s.events", guestId), CarRaceGuestMessage.setGuestAsHost(this.startKey));
    this.broadcast.write(CarRaceBroadcastMessage.updateGuestsInfo(redTeam.size(), greenTeam.size()));
    this.broadcast.write(CarRaceBroadcastMessage.updateTeamsScores(redScore, greenScore));
  }

  private void bookClick(String guestId) {
    if (this.status != CarRaceStatus.RUNNING) return;
    CarRaceTeam team = this.redTeam.contains(guestId) ? CarRaceTeam.RED_TEAM : CarRaceTeam.GREEN_TEAM;

    this.eb.publish("kafka.publish.click", KafkaPublishClick.from(this.sessionCode, team));
  }

  private void startSession() throws InterruptedException {
    this.status = CarRaceStatus.RUNNING;

    this.broadcast.write(CarRaceBroadcastMessage.startClicking(20));
    vertx.setTimer(20000, id -> {
      this.status = CarRaceStatus.FINISHED;
      this.broadcast.write(CarRaceBroadcastMessage.stopClicking());

      vertx.setTimer(3000, id2 -> {
        var winner = redScore > greenScore ? CarRaceTeam.RED_TEAM : CarRaceTeam.GREEN_TEAM;
        this.broadcast.write(CarRaceBroadcastMessage.announceWinner(winner, redScore, greenScore));
      });
    });
  }
}
