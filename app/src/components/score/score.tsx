import "./score.css";

interface ScoreProps {
  score: number;
  teamColor: string;
  isUserTeam: boolean;
  teamSize: number;
}

export function Score({ score, teamColor, isUserTeam, teamSize }: ScoreProps) {
  return (
    <div className="Score">
      <div className="Score-value" style={{ backgroundColor: teamColor }}>
        <div>{score}</div>
      </div>
      {isUserTeam ? (
        <span style={{ fontSize: "20pt" }}>Seu time ({teamSize})</span>
      ) : (
        <span style={{ fontSize: "20pt" }}>Time rival ({teamSize})</span>
      )}
    </div>
  );
}
