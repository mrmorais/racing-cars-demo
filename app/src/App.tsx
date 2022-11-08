import React, { useRef, useEffect, useState, useMemo } from "react";
import { generate as genRandomString } from "randomstring";
import { CarInfo } from "./model/car-info.model";
import { createCarsTrack } from "./components/cars-track/cars-track.sketch";
import "./App.css";
import { PushButton } from "./components/push-button/push-button";
import { Timer } from "./components/timer/timer";
import { Score } from "./components/score/score";

enum SessionState {
  LOBBYING,
  RUNNING,
  WAITING_WINNER,
  FINISHED,
}

function App() {
  const canvasRef = useRef<HTMLDivElement | null>(null);
  const ebRef = useRef<EventBus | null | any>(null);
  const redRef = useRef<CarInfo>(new CarInfo());
  const greenRef = useRef<CarInfo>(new CarInfo());

  const guestId = useMemo(() => genRandomString(), []);
  const [session, setSession] = useState<string | null>(null);

  const [guestTeam, setGuestTeam] = useState<string>("");
  const [winner, setWinner] = useState<string | null>(null);
  const [startKey, setStartKey] = useState<string | null>(null);
  const [sessionState, setSessionState] = useState<SessionState>(SessionState.LOBBYING);

  const [startedAt, setStartedAt] = useState<Date | null>(null);
  const [timerExpiry, setTimerExpiry] = useState<Date | null>(null);

  const [redSize, setRedSize] = useState<number>(0);
  const [greenSize, setGreenSize] = useState<number>(0);
  const [redScore, setRedScore] = useState<number>(0);
  const [greenScore, setGreenScore] = useState<number>(0);

  useEffect(() => {
    if (sessionState === SessionState.RUNNING && timerExpiry && startedAt) {
      const trackWidth = 750;
      const totalScore = redScore + greenScore;

      const diff = Math.abs(redScore - greenScore);
      const winnerMult = 1;
      const looserMult = 1 - diff / totalScore;

      const timeRatio = (Date.now() - startedAt.getTime()) / 20000;

      const redXPosition = trackWidth * timeRatio * (redScore > greenScore ? winnerMult : looserMult);
      const greenXPosition = trackWidth * timeRatio * (greenScore > redScore ? winnerMult : looserMult);

      greenRef.current.x = greenXPosition;
      redRef.current.x = redXPosition;
    }
  }, [greenScore, redScore]);

  useEffect(() => {
    if (!session) return;

    const eb = new EventBus("http://3.222.90.113/bus/");
    ebRef.current = eb;

    eb.onopen = () => {
      // Broadcast listener
      eb.registerHandler(`${session}.all`, {}, (_, { body }) => {
        console.log(body);
        const { type, data } = body ?? {};

        switch (type) {
          case "UPDATE_GUESTS_INFO":
            setRedSize(data?.redTeamSize ?? 0);
            setGreenSize(data?.greenTeamSize ?? 0);
            break;
          // case "UPDATE_TEAMS_SCORES":
          //   setRedScore(data?.redTeamScore ?? 0);
          //   setGreenScore(data?.greenTeamScore ?? 0);
          //   break;
          case "UPDATE_GREEN_SCORE":
            setGreenScore(data?.score ?? 0);
            break;
          case "UPDATE_RED_SCORE":
            setRedScore(data?.score ?? 0);
            break;
          case "START_CLICKING":
            const seconds = data?.duration ?? 0;
            const expiry = new Date();
            expiry.setSeconds(expiry.getSeconds() + seconds);
            setStartedAt(new Date());
            setTimerExpiry(expiry);
            setSessionState(SessionState.RUNNING);
            break;
          case "STOP_CLICKING":
            setSessionState(SessionState.WAITING_WINNER);
            break;
          case "ANNOUNCE_WINNER":
            setRedScore(data?.redScore ?? 0);
            setGreenScore(data?.greenScore ?? 0);
            setWinner(data?.winner);
            setSessionState(SessionState.FINISHED);
        }
      });

      // Guest direct events listener
      eb.registerHandler(`guest.${guestId}.events`, {}, (_, { body }) => {
        console.log(body);
        const { type, data } = body ?? {};

        switch (type) {
          case "SET_GUEST_TEAM":
            // const team = data?.team === "RED_TEAM" ? "red" : "green";
            setGuestTeam(data?.team);
            break;
          case "SET_GUEST_AS_HOST":
            setStartKey(data?.startKey);
            break;
        }
      });

      eb.send(`register`, { guestId, session });
    };
  }, [session, guestId]);

  useEffect(() => {
    const { cleanup } = createCarsTrack(
      canvasRef,
      { width: 1000, height: 250 },
      { redCar: redRef.current, greenCar: greenRef.current }
    );

    const paramSession = new URLSearchParams(window.location.search).get("session");
    const newSessionId = genRandomString(6);

    if (!paramSession) {
      window.location.search = `session=${newSessionId}`;
    } else {
      setSession(!!paramSession ? paramSession : genRandomString(6));
    }

    return cleanup;
  }, []);

  const handleClick = () => {
    if (sessionState === SessionState.RUNNING) {
      if (ebRef) {
        ebRef.current?.send(`${session}.click`, {
          guestId,
        });

        if (guestTeam === "RED_TEAM") {
          setRedScore(redScore + 1);
        } else {
          setGreenScore(greenScore + 1);
        }
      }
    }
  };

  const handleStart = () => {
    if (ebRef) {
      ebRef.current?.send(`${session}.start`, {
        startKey,
      });
    }
  };

  const handleFinish = () => {
    setSessionState(SessionState.WAITING_WINNER);
  };

  return (
    <div className="App">
      <div className="App-info">
        {sessionState === SessionState.LOBBYING && <span>Esperando começar!</span>}
        {sessionState === SessionState.RUNNING && (
          <Timer expiryTimestamp={timerExpiry ?? new Date()} onFinish={handleFinish} />
        )}
        {sessionState === SessionState.WAITING_WINNER && <span>O time vencedor é...</span>}
        {sessionState === SessionState.FINISHED && (
          <span>Parabéns time {winner === "RED_TEAM" ? "VERMELHO" : "VERDE"}</span>
        )}
      </div>
      <div ref={canvasRef} />
      <div className="App-scores">
        <Score score={redScore} teamColor={"red"} isUserTeam={guestTeam === "RED_TEAM"} teamSize={redSize} />
        <PushButton onClick={handleClick} />
        <Score score={greenScore} teamColor={"green"} isUserTeam={guestTeam === "GREEN_TEAM"} teamSize={greenSize} />
      </div>
      {startKey && <button onClick={handleStart}>Começar!</button>}
    </div>
  );
}

export default App;
