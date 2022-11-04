import React, { useRef, useEffect, useState, useCallback, useMemo } from "react";
import { generate as genRandomString } from "randomstring";
import { CarInfo } from "./model/car-info.model";
import { createCarsTrack } from "./components/cars-track/cars-track.sketch";
import "./App.css";
import { PushButton } from "./components/push-button/push-button";
import { Timer } from "./components/timer/timer";
import { Score } from "./components/score/score";

function App() {
  const canvasRef = useRef<HTMLDivElement | null>(null);
  const ebRef = useRef<EventBus | null>(null);
  const redRef = useRef<CarInfo>(new CarInfo());
  const greenRef = useRef<CarInfo>(new CarInfo());

  const guestId = useMemo(() => genRandomString(), []);

  const [guestTeam, setGuestTeam] = useState<string>("");
  const [startKey, setStartKey] = useState<string | null>(null);
  const [timerExpiry, setTimerExpiry] = useState<Date | null>(null);

  const [redSize, setRedSize] = useState<number>(0);
  const [greenSize, setGreenSize] = useState<number>(0);
  const [redScore, setRedScore] = useState<number>(0);
  const [greenScore, setGreenScore] = useState<number>(0);

  const registerEventBus = useCallback(() => {
    const eb = new EventBus("http://192.168.1.3:8080/bus/");
    ebRef.current = eb;

    eb.onopen = () => {
      // Broadcast listener
      eb.registerHandler("abc.all", {}, (_, { body }) => {
        console.log(body);
        const { type, data } = body ?? {};

        switch (type) {
          case "UPDATE_GUESTS_INFO":
            setRedSize(data?.redTeamSize ?? 0);
            setGreenSize(data?.greenTeamSize ?? 0);
            break;
          case "UPDATE_TEAMS_SCORES":
            setRedScore(data?.redTeamScore ?? 0);
            setGreenScore(data?.greenTeamScore ?? 0);
            break;
          case "START_CLICKING":
            const seconds = data?.duration ?? 0;
            const expiry = new Date();
            expiry.setSeconds(expiry.getSeconds() + seconds);
            setTimerExpiry(expiry);
            break;
        }
      });

      // Guest direct events listener
      eb.registerHandler(`guest.${guestId}.events`, {}, (_, { body }) => {
        console.log(body);
        const { type, data } = body ?? {};

        switch (type) {
          case "SET_GUEST_TEAM":
            const team = data?.team === "RED_TEAM" ? "red" : "green";
            setGuestTeam(team);
            break;
          case "SET_GUEST_AS_HOST":
            setStartKey(data?.startKey);
            break;
        }
      });

      eb.send("abc.register", { guestId });
    };
  }, [guestId]);

  useEffect(() => {
    const { cleanup } = createCarsTrack(
      canvasRef,
      { width: 1000, height: 250 },
      { redCar: redRef.current, greenCar: greenRef.current }
    );

    registerEventBus();

    return cleanup;
  }, [registerEventBus]);

  const handleClick = () => {
    if (ebRef) {
      ebRef.current?.send("abc.click", {
        guestId,
      });
    }
  };

  const handleStart = () => {
    if (ebRef) {
      ebRef.current?.send("abc.start", {
        startKey,
      });
    }
  };

  return (
    <div className="App">
      <div>{timerExpiry && <Timer expiryTimestamp={timerExpiry} />}</div>
      <div ref={canvasRef} />
      <div className="App-scores">
        <Score score={redScore} teamColor={"red"} isUserTeam={guestTeam === "red"} teamSize={redSize} />
        <PushButton onClick={handleClick} />
        <Score score={greenScore} teamColor={"green"} isUserTeam={guestTeam === "green"} teamSize={greenSize} />
      </div>
      {startKey && <button onClick={handleStart}>Começar!</button>}
    </div>
  );
}

export default App;
