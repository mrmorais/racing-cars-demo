import { useTimer } from "react-timer-hook";
import "./timer.css";

interface TimerProps {
  expiryTimestamp: Date;
}

export function Timer({ expiryTimestamp }: TimerProps) {
  const { seconds, minutes, hours, days, isRunning, start, pause, resume, restart } = useTimer({
    expiryTimestamp,
    onExpire: () => {
      console.log("Finish!!");
    },
  });

  return <div className="Timer">Acaba em {seconds} segundos</div>;
}
