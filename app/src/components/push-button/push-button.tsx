import "./push-button.css";

interface PushButtonProps {
  label: string;
  onClick: () => void;
}

export function PushButton({ label, onClick }: PushButtonProps) {
  return (
    <button onClick={onClick} className="start-btn">
      {label}
    </button>
  );
}

PushButton.defaultProps = {
  label: "CLIQUE!",
};
