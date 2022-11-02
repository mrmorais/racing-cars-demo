import "./score.css";

interface ScoreProps {
  teamColor: string;
  isUserTeam: boolean;
}

export function Score({ teamColor, isUserTeam }: ScoreProps) {
  return (
    <div className="Score">
      <div className="Score-value" style={{ backgroundColor: teamColor }}>
        <div>100</div>
      </div>
      {isUserTeam ? (
        <span style={{ fontSize: "20pt" }}>Seu time</span>
      ) : (
        <span style={{ fontSize: "20pt" }}>Time rival</span>
      )}
    </div>
  );
}
