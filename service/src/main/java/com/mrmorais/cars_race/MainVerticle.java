package com.mrmorais.cars_race;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

import java.util.HashSet;
import java.util.Set;

public class MainVerticle extends AbstractVerticle {
  private Set<String> sessions = new HashSet();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    KafkaBridgeVerticle kafkaBridge = new KafkaBridgeVerticle();
    vertx.deployVerticle(kafkaBridge);

    var eb = vertx.eventBus();
    eb.consumer("register", (message) -> {
      System.out.println(message.body());
      var session = JsonObject.mapFrom(message.body()).getString("session");

      if (this.sessions.contains(session)) {
        eb.publish(String.format("%s.register", session), message.body());
      } else {
        var newCarRacSession = new CarRaceVerticle(session);
        vertx.deployVerticle(newCarRacSession).onSuccess((success) -> {
          this.sessions.add(session);
          eb.publish(String.format("%s.register", session), message.body());
        });
      }
    });

    Router router = Router.router(vertx);

    SockJSHandlerOptions options = new SockJSHandlerOptions()
      .setRegisterWriteHandler(true);

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
    SockJSBridgeOptions bridgeOptions = new SockJSBridgeOptions()
      .addInboundPermitted(new PermittedOptions())
      .addOutboundPermitted(new PermittedOptions());

    router.route("/bus/*")
        .subRouter(sockJSHandler.bridge(bridgeOptions));

    vertx.createHttpServer().requestHandler(router)
      .listen(80)
      .onSuccess(server -> {
        startPromise.complete();
        System.out.println("HTTP service listening at " + server.actualPort());
      });
  }
}