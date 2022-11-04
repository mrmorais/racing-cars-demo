package com.mrmorais.cars_race;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    CarRaceVerticle carRace = new CarRaceVerticle("abc");
    vertx.deployVerticle(carRace);

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
      .listen(8080)
      .onSuccess(server -> {
        startPromise.complete();
        System.out.println("HTTP service listening at " + server.actualPort());
      });
  }
}