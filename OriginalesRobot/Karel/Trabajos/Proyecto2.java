import kareltherobot.*;
import java.util.concurrent.*;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public class Proyecto2 {

    public static ConcurrentHashMap<String, String> posicionesDeRobots = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Object> locksPosiciones = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> reservasDePosiciones = new ConcurrentHashMap<>();

    public static int totalBeepersToMove = 50;

    public static ConcurrentHashMap<String, SemaforoInteligente> semaforosPorUbicacion = new ConcurrentHashMap<>();

    public static class SemaforoInteligente {
        private String ubicacion;
        private boolean enVerde;
        private Semaphore semaforo;
        private int contadorDeVehiculos;
        private int limiteParaRojo;
        private List<String> posicionesVerde;
        private int vehiculosParaCambiarAVerde;  // Número de vehículos necesarios para cambiar a verde
        private int contadorVehiculosPosicionesVerde;  // Contador de vehículos que pasan por las posiciones en verde

        public SemaforoInteligente(String ubicacion, boolean iniciaEnVerde, int limiteParaRojo, List<String> posicionesVerde, int vehiculosParaCambiarAVerde) {
            this.ubicacion = ubicacion;
            this.enVerde = iniciaEnVerde;
            this.semaforo = new Semaphore(1, true);
            this.limiteParaRojo = limiteParaRojo;
            this.contadorDeVehiculos = 0;
            this.posicionesVerde = posicionesVerde;
            this.vehiculosParaCambiarAVerde = vehiculosParaCambiarAVerde;
            this.contadorVehiculosPosicionesVerde = 0;  // Inicializamos el contador en 0
        }

        public boolean estaEnVerde() {
            return enVerde;
        }

        public synchronized void cambiarARojo() {
            if (enVerde) {
                enVerde = false;
                System.out.println("Semáforo en " + ubicacion + " cambió a rojo.");
            }
        }

        public synchronized void cambiarAVerde() {
            if (!enVerde) {
                enVerde = true;
                System.out.println("Semáforo en " + ubicacion + " cambió a verde.");
                contadorVehiculosPosicionesVerde = 0;  // Reiniciar el contador cuando cambie a verde
                notifyAll();  // Notificar a los robots que están esperando
            }
        }

        public synchronized void gestionarSemaforoConVehiculos() {
            contadorDeVehiculos++;
            if (enVerde && contadorDeVehiculos >= limiteParaRojo) {
                cambiarARojo();
                contadorDeVehiculos = 0;  // Reiniciar el contador cuando cambia a rojo
            }
        }

        // Modificado: Contar vehículos en posicionesVerde aunque el semáforo esté en verde
        public synchronized void verificarYActivarVerde(String posicionRobot) {
            if (posicionesVerde.contains(posicionRobot)) {
                contadorVehiculosPosicionesVerde++;
                System.out.println("Vehículo pasó por la posición " + posicionRobot + ". Contador: " + contadorVehiculosPosicionesVerde);

                // Cambiar a verde solo si aún no está en verde y se alcanza el número de vehículos
                if (!enVerde && contadorVehiculosPosicionesVerde >= vehiculosParaCambiarAVerde) {
                    cambiarAVerde();  // Cambiar a verde si se ha alcanzado el número de vehículos necesarios
                }
            }
        }

        public synchronized void esperarSiSemaforoEnRojo(String idRobot) throws InterruptedException {
            while (!estaEnVerde()) {
                System.out.println(idRobot + " espera en semáforo en " + ubicacion);
                wait();
            }
            System.out.println(idRobot + " cruzó por semáforo en " + ubicacion);
        }
    }



    public static class RobotConductor extends Robot implements Runnable {
        private Random aleatorio;
        private int paradaActual;
        private int calleActual;
        private int avenidaActual;
        private String idRobot;
        private static final int PARADA_1 = 1;
        private static final int PARADA_2 = 2;
        private static final int PARADA_3 = 3;
        private static final int PARADA_4 = 4;

        public RobotConductor(int calle, int avenida, Direction direccion, int zumbadores, String idRobot, int paradaAsignada) {
            super(calle, avenida, direccion, zumbadores);
            this.calleActual = calle;
            this.avenidaActual = avenida;
            this.idRobot = idRobot;
            this.paradaActual = paradaAsignada;
            this.aleatorio = new Random();

            synchronized (posicionesDeRobots) {
                posicionesDeRobots.put(idRobot, calleActual + "," + avenidaActual);
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (!anyBeepersInBeeperBag()) {
                        recogerPasajero();
                    }

                    if (anyBeepersInBeeperBag()) {
                        moverALaParada(paradaActual);
                        putBeeper();
                        regresarAlParqueadero(paradaActual);
                    }

                    if (totalBeepersToMove <= 0) {
                        System.out.println("Todos los beepers han sido movidos.");
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void recogerPasajero() {
            try {
                moverAlPuntoDeRecogida();

                if (nextToABeeper() && calleActual == 8 && avenidaActual == 19) {
                    pickBeeper();
                    totalBeepersToMove--;
                    System.out.println("Beeper recogido. Beepers restantes: " + totalBeepersToMove);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
                            {8, 'L'}, {6, 'R'}, {9, 'R'}, {16, 'R'}, {7, 'R'}, {4, 'R'}, {6, 'L'}, {5, 'L'}, {14, 'L'}, {1, 'L'}, {2, 'R'}, {1, 'L'}, {4, 'R'}, {4, 'R'}, {5, 'A'}, {1, 'L'}, {2, 'L'}, {2, 'L'}, {3, 'L'}, {6, 'A'}, {1, 'L'}, {1, 'R'}, {6, 'R'}, {5, 'R'}, {1, 'A'}, {1, 'R'}, {3, 'L'}, {1, 'L'}, {9, 'L'}, {1, 'A'}
                    });
                    break;
            }
        }

        private void regresarAlParqueadero(int parada) {
            switch (parada) {
                case PARADA_1:
                    rutaParada(new int[][] {
                            {2, 'R'}, {3, 'R'}, {4, 'R'}, {6, 'L'}, {5, 'L'}, {14, 'L'}, {1, 'L'}, {7, 'R'}, {8, 'R'}, {8, 'L'},{1, 'L'}, {16, 'L'}, {18, 'L'}, {18, 'L'}, {1, 'L'}, {7, 'R'}, {5, 'R'}, {5, 'R'}, {4, 'L'}
                    });
                    break;
                case PARADA_2:
                    rutaParada(new int[][] {
                            {3, 'R'}, {1, 'L'}, {4, 'L'}, {4, 'L'}, {12, 'R'}, {7, 'L'}, {1, 'L'},  {16, 'L'}, {18, 'L'}, {18, 'L'}, {1, 'L'}, {7, 'R'}, {5, 'R'}, {5, 'R'}, {4, 'L'}
                    });
                    break;
                case PARADA_3:
                    rutaParada(new int[][] {
                            {2, 'L'}, {2, 'L'}, {12, 'R'}, {7, 'L'}, {1, 'L'},  {16, 'L'}, {18, 'L'}, {18, 'L'}, {1, 'L'}, {7, 'R'}, {5, 'R'}, {5, 'R'}, {4, 'L'}
                    });
                    break;
                case PARADA_4:
                    rutaParada(new int[][] {
                            {1, 'R'}, {9, 'R'}, {1, 'R'},  {8, 'L'}, {6, 'L'}, {1, 'L'}, {5, 'R'}, {3, 'R'}, {2, 'R'}, {2, 'L'}, {4, 'L'}, {4, 'L'}, {4, 'L'}, {1, 'A'}, {2, 'R'}, {5, 'R'}, {8, 'R'}, {7, 'L'}, {1, 'L'},  {16, 'L'}, {18, 'L'}, {18, 'L'}, {1, 'L'}, {7, 'R'}, {5, 'R'}, {5, 'R'}, {4, 'L'}
                    });
                    break;
            }
        }

        private void rutaParada(int[][] movimientos) {
            for (int[] movimiento : movimientos) {
                int steps = movimiento[0];
                char direction = (char) movimiento[1];

                moveStraight(steps);
                switch (direction) {
                    case 'L':
                        turnLeft();
                        break;
                    case 'R':
                        turnRight();
                        break;
                    case 'A':
                        turnAround();
                        break;
                }
            }
        }

        private void moveStraight(int steps) {
            for (int i = 0; i < steps; i++) {
                if (frontIsClear()) {
                    reservarYMover();

                    // Obtener la posición actual después de moverse
                    String posicionActual = calleActual + "," + avenidaActual;
                    SemaforoInteligente semaforo = semaforosPorUbicacion.get(posicionActual);

                    if (semaforo != null) {
                        try {
                            semaforo.esperarSiSemaforoEnRojo(idRobot);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // Gestionar el tráfico incrementando el contador de vehículos en el semáforo actual
                        semaforo.gestionarSemaforoConVehiculos();
                    }

                    verificarSemaforo(posicionActual);
                }
            }
        }

        private void reservarYMover() {
            String siguientePosicion = obtenerSiguientePosicion();

            while (!reservarPosicion(siguientePosicion)) {
                esperarYReintentar(siguientePosicion);
            }

            realizarMovimiento(siguientePosicion);
        }

        private void verificarSemaforo(String posicion) {
            for (SemaforoInteligente s : semaforosPorUbicacion.values()) {
                s.verificarYActivarVerde(posicion);
            }
        }

        private boolean reservarPosicion(String siguientePosicion) {
            synchronized (reservasDePosiciones) {
                if (reservasDePosiciones.get(siguientePosicion) == null) {
                    reservasDePosiciones.put(siguientePosicion, idRobot);
                    System.out.println(idRobot + " reservó la casilla " + siguientePosicion);
                    return true;
                }
                return false;
            }
        }

        private void esperarYReintentar(String siguientePosicion) {
            System.out.println("El robot " + idRobot + " está esperando para moverse a " + siguientePosicion);
            try {
                Thread.sleep(50 + aleatorio.nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void realizarMovimiento(String siguientePosicion) {
            move();

            synchronized (posicionesDeRobots) {
                reservasDePosiciones.remove(calleActual + "," + avenidaActual);
                posicionesDeRobots.put(idRobot, siguientePosicion);
                System.out.println(idRobot + " se movió a la casilla " + siguientePosicion);
            }

            actualizarPosicion();
        }

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

        private void turnRight() {
            turnLeft();
            turnLeft();
            turnLeft();
        }

        private void turnAround() {
            turnLeft();
            turnLeft();
        }
    }

    public static void main(String[] args) {
        World.readWorld("PracticaOperativos.kwld");
        World.setVisible(true);
        World.setDelay(10);

        ExecutorService ejecutorDeRobots = Executors.newFixedThreadPool(14);

        SemaforoInteligente semaforo1 = new SemaforoInteligente("18,5", true, 1,  List.of("18,7"),1);
        SemaforoInteligente semaforo2 = new SemaforoInteligente("10,8", true, 1,  List.of("10,6"),1);
        SemaforoInteligente semaforo3 = new SemaforoInteligente("6,7", true, 1,  List.of("6,9"),1);

        SemaforoInteligente semaforo4 = new SemaforoInteligente("10,16", true, 7,  List.of("10,14"),7);

        SemaforoInteligente semaforo5 = new SemaforoInteligente("15,16", true, 1,  List.of("16,11"),1);
        SemaforoInteligente semaforo6 = new SemaforoInteligente("16,11", true, 1,  List.of("12,17"),1);
        SemaforoInteligente semaforo7 = new SemaforoInteligente("12,17", true, 1,  List.of("12,15"),1);





        semaforosPorUbicacion.put("18,5", semaforo1);
        semaforosPorUbicacion.put("10,8", semaforo2);
        semaforosPorUbicacion.put("6,7", semaforo3);

        semaforosPorUbicacion.put("10,16", semaforo4);

        semaforosPorUbicacion.put("15,16", semaforo5);
        semaforosPorUbicacion.put("16,11", semaforo6);
        semaforosPorUbicacion.put("12,17", semaforo7);




        for (int i = 1; i <= 14; i++) {
            int paradaAsignada = (i % 4) + 1;
            RobotConductor robot = new RobotConductor(3, 17, Directions.East, 0, "Robot" + i, paradaAsignada);
            ejecutorDeRobots.execute(robot);
        }

        ejecutorDeRobots.shutdown();
    }
}
