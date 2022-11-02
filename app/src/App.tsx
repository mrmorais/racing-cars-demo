import React, { useRef, useEffect, useState, useCallback, useMemo } from "react";
import { CarInfo } from "./model/car-info.model";
import { createCarsTrack } from "./components/cars-track/cars-track.sketch";
import "./App.css";
import { PushButton } from "./components/push-button/push-button";
import { Timer } from "./components/timer/timer";
import { Score } from "./components/score/score";

function App() {
  const canvasRef = useRef<HTMLDivElement | null>(null);
  const redRef = useRef<CarInfo>(new CarInfo());
  const greenRef = useRef<CarInfo>(new CarInfo());

  useEffect(() => {
    const { cleanup } = createCarsTrack(
      canvasRef,
      { width: 1000, height: 250 },
      { redCar: redRef.current, greenCar: greenRef.current }
    );

    return cleanup;
  }, []);

  const expire = new Date();
  expire.setSeconds(expire.getSeconds() + 10);

  return (
    <div className="App">
      <div>
        <Timer expiryTimestamp={expire} />
      </div>
      <div ref={canvasRef} />
      <div className="App-scores">
        <Score teamColor={"red"} isUserTeam={true} />
        <PushButton onClick={console.log} />
        <Score teamColor={"green"} isUserTeam={false} />
      </div>
    </div>
  );
}

export default App;
