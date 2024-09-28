import kareltherobot.*;
import java.util.concurrent.*;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

public class Proyecto2 {

    // HashMap global para almacenar la ubicación de todos los robots
    public static HashMap<String, String> posicionesDeRobots = new HashMap<>();

    // Variable global para contar la cantidad total de beepers que deben ser movidos
    public static int totalBeepersToMove = 1000; // Total inicial de beepers en calle 8, avenida 19

    // Clase Robot Conductor que sigue las rutas predefinidas
    public static class RobotConductor extends Robot implements Runnable {
        private Semaphore accesoBeepers; // Semáforo para controlar el acceso a los beepers (pasajeros)
        private Random aleatorio;
        private int paradaActual;
        private int calleActual;
        private int avenidaActual;
        private String idRobot; // Identificador del robot
        private static final int PARADA_1 = 1;
        private static final int PARADA_2 = 2;
        private static final int PARADA_3 = 3;
        private static final int PARADA_4 = 4;

        // Constructor
        public RobotConductor(int calle, int avenida, Direction direccion, int zumbadores, Semaphore accesoBeepers, String idRobot) {
            super(calle, avenida, direccion, zumbadores);
            this.accesoBeepers = accesoBeepers;
            this.calleActual = calle;
            this.avenidaActual = avenida;
            this.idRobot = idRobot;
            this.aleatorio = new Random();

            // Agregar el robot al mapa de posiciones globales
            synchronized (posicionesDeRobots) {
                posicionesDeRobots.put(idRobot, calleActual + "," + avenidaActual);
            }
        }

        // Método principal que controla el viaje del robot
        @Override
        public void run() {
            try {
                while (true) {
                    // Si no hay beepers en la bolsa, intenta recoger uno
                    if (!anyBeepersInBeeperBag()) {
                        recogerPasajero();
                    }

                    // Solo continuar si se ha recogido un beeper
                    if (anyBeepersInBeeperBag()) {
                        paradaActual = 1; // Asignar parada 1 por ahora
                        moverALaParada(paradaActual); // Moverse a la parada
                        putBeeper(); // Dejar el beeper (pasajero)
                        regresarAlParqueadero(paradaActual); // Regresar al parqueadero
                    }

                    // Detener el programa si ya se han movido todos los beepers
                    if (totalBeepersToMove <= 0) {
                        System.out.println("Todos los beepers han sido movidos.");
                        break; // Terminar cuando ya no quedan beepers por mover
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Método para recoger un beeper (pasajero)
        private void recogerPasajero() {
            try {
                accesoBeepers.acquire(); // Adquirir acceso exclusivo a los beepers
                moverAlPuntoDeRecogida(); // Moverse al punto de recogida

                // Verifica si el robot está en la posición (8, 19) y hay un beeper para recoger
                if (nextToABeeper() && calleActual == 8 && avenidaActual == 19) {
                    pickBeeper(); // Recoger el beeper (pasajero)
                    totalBeepersToMove--; // Disminuir el contador global de beepers
                    System.out.println("Beeper recogido. Beepers restantes: " + totalBeepersToMove);
                }
                accesoBeepers.release(); // Liberar el acceso a los beepers
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Método que mueve al robot al punto de recogida
        private void moverAlPuntoDeRecogida() {
            moveStraight(2);
            turnLeft();
            moveStraight(5);
            turnLeft();
        }

        private void moverALaParada(int parada) {
            switch (parada) {
                case PARADA_1:
                    rutaParada(new int[][] {
                            {8, 'L'}, {6, 'R'}, {9, 'R'}, {16, 'R'}, {4, 'R'}, {2, 'A'}
                    });
                    break;
                case PARADA_2:
                    rutaParada(new int[][] {
                            {8, 'L'}, {6, 'R'}, {9, 'R'}, {16, 'R'}, {7, 'R'}, {4, 'R'}, {6, 'L'}, {5, 'L'}, {14, 'L'}, {1, 'L'}, {10, 'R'}, {3, 'A'}
                    });
                    break;
                case PARADA_3:
                    rutaParada(new int[][] {
                            {8, 'L'}, {6, 'R'}, {9, 'R'}, {16, 'R'}, {7, 'R'}, {4, 'R'}, {6, 'L'}, {5, 'L'}, {3, 'R'}, {3, 'L'}, {2, 'L'}, {2, 'A'}
                    });
                    break;
                case PARADA_4:
                    rutaParada(new int[][] {
                            {8, 'L'}, {6, 'R'}, {9, 'R'}, {16, 'R'}, {7, 'R'}, {4, 'R'}, {6, 'L'}, {5, 'L'}, {14, 'L'}, {1, 'L'}, {2, 'R'}, {2, 'A'}, {1, 'R'}, {4, 'R'}, {4, 'R'}, {5, 'A'}, {1, 'L'}, {2, 'L'}, {2, 'L'}, {3, 'L'}, {6, 'A'}, {1, 'L'}, {1, 'R'}, {6, 'R'}, {5, 'R'}, {1, 'A'}, {1, 'R'}, {3, 'L'}, {1, 'L'}, {9, 'L'}, {1, 'A'}
                    });
                    break;
            }
        }

        private void regresarAlParqueadero(int parada) {
            switch (parada) {
                case PARADA_1:
                    rutaParada(new int[][] {
                            {2, 'R'}, {3, 'R'}, {4, 'R'}, {6, 'L'}, {5, 'L'}, {14, 'L'}, {1, 'L'}, {7, 'R'}, {8, 'R'}, {8, 'L'},{1, 'L'}, {16, 'L'}, {18, 'L'}, {18, 'L'}, {1, 'L'}, {2, 'R'}, {1, 'R'}
                    });
                    break;
                case PARADA_2:
                    rutaParada(new int[][] {
                            {3, 'R'}, {1, 'L'}, {4, 'L'}, {4, 'L'}, {12, 'R'}, {7, 'L'}, {1, 'L'},  {16, 'L'}, {18, 'L'}, {18, 'L'}, {1, 'L'}, {2, 'R'}, {1, 'R'}
                    });
                    break;
                case PARADA_3:
                    rutaParada(new int[][] {
                            {2, 'L'}, {2, 'L'}, {12, 'R'}, {7, 'L'}, {1, 'L'},  {16, 'L'}, {18, 'L'}, {18, 'L'}, {1, 'L'}, {2, 'R'}, {1, 'R'}
                    });
                    break;
                case PARADA_4:
                    rutaParada(new int[][] {
                            {1, 'R'}, {9, 'R'}, {1, 'R'}, {3, 'L'}, {1, 'A'}, {1, 'L'}, {5, 'L'}, {6, 'L'}, {1, 'R'}, {1, 'A'}, {6, 'R'}, {3, 'R'}, {2, 'R'}, {2, 'R'}, {1, 'A'}, {5, 'L'}, {4, 'L'}, {4, 'L'}, {1, 'A'}, {2, 'R'}, {5, 'R'}, {8, 'R'}, {7, 'L'}, {1, 'L'},  {16, 'L'}, {18, 'L'}, {18, 'L'}, {1, 'L'}, {2, 'R'}, {1, 'R'}
                    });
                    break;
            }
        }

        // Método para ejecutar la ruta de una parada con movimientos compactos
        private void rutaParada(int[][] movimientos) {
            for (int[] movimiento : movimientos) {
                int steps = movimiento[0];
                char direction = (char) movimiento[1];

                moveStraight(steps);
                switch (direction) {
                    case 'L': // Turn left
                        turnLeft();
                        break;
                    case 'R': // Turn right
                        turnRight();
                        break;
                    case 'A': // Turn around
                        turnAround();
                        break;
                }
            }
        }

        // Método para moverse sin colisionar (simplificado)
        private void moveStraight(int steps) {
            for (int i = 0; i < steps; i++) {
                if (frontIsClear()) {
                    moverseSinColision();
                }
            }
        }

        // Método para moverse sin colisionar y resolver el deadlock
        private void moverseSinColision() {
            if (frontIsClear()) {
                String siguientePosicion = obtenerSiguientePosicion();

                synchronized (posicionesDeRobots) {
                    String ocupanteActual = getRobotInPosition(siguientePosicion);

                    if (ocupanteActual != null && !ocupanteActual.equals(idRobot)) {
                        if (compareRobotIDs(idRobot, ocupanteActual)) {
                            realizarMovimiento(siguientePosicion);
                        } else {
                            while (posicionesDeRobots.containsValue(siguientePosicion)) {
                                try {
                                    Thread.sleep(100); // Esperar antes de reintentar
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            realizarMovimiento(siguientePosicion);
                        }
                    } else {
                        realizarMovimiento(siguientePosicion);
                    }
                }
            }
        }

        // Método para realizar el movimiento y actualizar la posición del robot
        private void realizarMovimiento(String siguientePosicion) {
            move();
            actualizarPosicion();
            posicionesDeRobots.put(idRobot, calleActual + "," + avenidaActual);
        }

        // Método auxiliar para obtener el robot que está en una posición específica
        private String getRobotInPosition(String posicion) {
            for (Map.Entry<String, String> entry : posicionesDeRobots.entrySet()) {
                if (entry.getValue().equals(posicion)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        // Método para comparar IDs de robots (retorna true si el ID actual tiene prioridad)
        private boolean compareRobotIDs(String idRobot1, String idRobot2) {
            return idRobot1.compareTo(idRobot2) < 0;
        }

        // Método para obtener la posición a la que el robot se va a mover
        private String obtenerSiguientePosicion() {
            int calleSiguiente = calleActual;
            int avenidaSiguiente = avenidaActual;
            if (facingNorth()) {
                calleSiguiente++;
            } else if (facingSouth()) {
                calleSiguiente--;
            } else if (facingEast()) {
                avenidaSiguiente++;
            } else if (facingWest()) {
                avenidaSiguiente--;
            }
            return calleSiguiente + "," + avenidaSiguiente;
        }

        // Método para actualizar la posición del robot después de moverse
        private void actualizarPosicion() {
            if (facingNorth()) {
                calleActual++;
            } else if (facingSouth()) {
                calleActual--;
            } else if (facingEast()) {
                avenidaActual++;
            } else if (facingWest()) {
                avenidaActual--;
            }
        }

        // Girar a la derecha
        private void turnRight() {
            turnLeft();
            turnLeft();
            turnLeft();
        }

        // Girar 180 grados
        private void turnAround() {
            turnLeft();
            turnLeft();
        }
    }

    // Método principal
    public static void main(String[] args) {
        World.readWorld("PracticaOperativos.kwld");
        World.setVisible(true);
        World.setDelay(10);

        Semaphore accesoBeepers = new Semaphore(1);

        ExecutorService ejecutorDeRobots = Executors.newFixedThreadPool(8);

        RobotConductor robot = new RobotConductor(3, 17, Directions.East, 0, accesoBeepers, "Robot" + 1);
        ejecutorDeRobots.execute(robot);

        ejecutorDeRobots.shutdown();
    }
}
