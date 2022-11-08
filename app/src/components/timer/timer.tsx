import { useTimer } from "react-timer-hook";
import "./timer.css";

interface TimerProps {
  expiryTimestamp: Date;
  onFinish: () => any;
}

export function Timer({ expiryTimestamp, onFinish }: TimerProps) {
  const { seconds } = useTimer({
    expiryTimestamp,
    onExpire: onFinish
  });

  return <div className="Timer">Acaba em {seconds} segundos</div>;
}
