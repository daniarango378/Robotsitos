import kareltherobot.*;
import java.awt.Color;

public class NoDeadlockIntersection implements Directions {
    public static void main(String[] args) {
        // Set up the world
        World.setSize(10, 10);
        World.setVisible(true);
		World.setDelay(20);
		World.showSpeedControl(true, true); //Needed to make them start

        // Create 10 robots, all heading to the intersection at (5, 5)
        RobotOp[] robots = new RobotOp[10];
		
        for (int i = 0; i < 10; i++) {
			robots[i] = new RobotOp(1, i + 1, East, 0); // Place robots along the first row
        }

        // Start all robots towards the intersection
        for (RobotOp robot : robots) {
            new Thread(() -> {
                while (robot.frontIsClear()) {
                    if (robot.street() == 5 && robot.avenue() == 5)
					{
						robot.move();
                        break;
					}
                    if (robot.street() < 5 && !robot.facingNorth())
                    {
						while(!robot.facingNorth())
							robot.turnLeft();
					}
                    if (robot.street() > 5 && !robot.facingSouth())
                    {
						while(!robot.facingSouth())
							robot.turnLeft();
					}
                    if (robot.street() == 5 && robot.avenue() < 5 && !robot.facingEast())
                    {
						while(!robot.facingEast())
							robot.turnLeft();
					}
                    if (robot.street() == 5 && robot.avenue() > 5 && !robot.facingWest())
                    {
						while(!robot.facingWest())
							robot.turnLeft();
					}
                    robot.move();
				}
            }).start();
        }
    }
}

class RobotOp extends Robot implements Directions {
	public RobotOp(int street, int avenue, Direction direction, int beeps)
	{
		super(street, avenue, direction, beeps);
	}
	
	public RobotOp(int street, int avenue, Direction direction, int beeps, Color color)
	{
		super(street, avenue, direction, beeps, color);
	}
	
	public int street()
	{
		String mensaje = this.toString();
		int posicion = mensaje.indexOf("street: ");
		int posFinal = mensaje.indexOf(")", posicion);
		return Integer.valueOf(mensaje.substring(posicion+8, posFinal));
	}
	
	public int avenue()
	{
		String mensaje = this.toString();
		int posicion = mensaje.indexOf("avenue: ");
		int posFinal = mensaje.indexOf(")", posicion);
		return Integer.valueOf(mensaje.substring(posicion+8, posFinal));
	}
}
