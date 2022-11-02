import P5 from "p5";
import { CarInfo } from "../../model/car-info.model";

export type SketchCleanup = { cleanup: () => void };

export const createCarsTrack = (
  canvasRef: any,
  { width, height }: { width: number; height: number },
  { redCar, greenCar }: { redCar: CarInfo; greenCar: CarInfo }
): SketchCleanup => {
  let redImg: P5.Image;
  let greenImg: P5.Image;
  let finishImg: P5.Image;

  let currentRedPosition = 0;
  let currentGreenPosition = 0;

  const sketch = (p5: P5) => {
    p5.setup = () => {
      console.log("setup");

      p5.createCanvas(width, height);
      redImg = p5.loadImage("/assets/red-car.png");
      greenImg = p5.loadImage("/assets/green-car.png");
      finishImg = p5.loadImage("/assets/finish.png");
    };

    const drawTrackLines = () => {
      for (let i = 0; i < 5; i++) {
        p5.fill("yellow");
        p5.rect(50 + i * 190, 110, 100, 12);
      }
    };

    p5.draw = () => {
      p5.background(0);
      const desiredRedPosition = redCar.x ?? 0;
      const redDelta = desiredRedPosition - currentRedPosition;

      const desiredGreenPosition = greenCar.x ?? 0;
      const greenDelta = desiredGreenPosition - currentGreenPosition;

      currentRedPosition += redDelta / 20;
      currentGreenPosition += greenDelta / 20;

      p5.image(finishImg, 980, 0);
      drawTrackLines();
      p5.image(redImg, currentRedPosition, 0);
      p5.image(greenImg, currentGreenPosition, 120);
    };
  };

  const p5Instance = new P5(sketch, canvasRef.current);

  return {
    cleanup: p5Instance.remove,
  };
};
