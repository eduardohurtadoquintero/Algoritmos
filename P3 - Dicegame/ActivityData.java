import java.util.ArrayList;
import java.util.List;

public class ActivityData {

    public static class RegistroSalida {
        public final int turnoEntrada;
        public final int turnoSalida;
        public final int duracion;
        public RegistroSalida(int e, int s, int d) { turnoEntrada=e; turnoSalida=s; duracion=d; }
    }

    private final int numEtapas;
    private final List<List<Integer>> rolls;
    private final List<List<Integer>> movimientos;
    private final List<Integer>       throughputPorTurno;
    private final List<Integer>       numberInSystem;
    private final List<RegistroSalida> registrosSalida; 
    private int salidasAnterior = 0;

    public ActivityData(int numEtapas) {
        this.numEtapas          = numEtapas;
        this.rolls              = new ArrayList<>();
        this.movimientos        = new ArrayList<>();
        this.throughputPorTurno = new ArrayList<>();
        this.numberInSystem     = new ArrayList<>();
        this.registrosSalida    = new ArrayList<>();
        for (int i = 0; i < numEtapas; i++) {
            rolls.add(new ArrayList<>());
            movimientos.add(new ArrayList<>());
        }
    }

    public void registrarTurno(Etapa[] etapas) {
        for (int i = 0; i < numEtapas; i++) {
            rolls.get(i).add(etapas[i].getUltimoRoll());
            movimientos.get(i).add(etapas[i].getUltimoMovimiento());
        }
        int salidasActual = etapas[numEtapas-1].getTotalSalidas();
        throughputPorTurno.add(salidasActual - salidasAnterior);
        salidasAnterior = salidasActual;

        int enSistema = 0;
        for (Etapa e : etapas)
            if (!e.isEntradaInfinita()) enSistema += e.getCantidadFichas();
        numberInSystem.add(enSistema);
    }

    /** Llamado por la Etapa final cuando una ficha NUEVA sale del sistema. */
    public void registrarSalida(int turnoEntrada, int turnoSalida, int duracion) {
        registrosSalida.add(new RegistroSalida(turnoEntrada, turnoSalida, duracion));
    }

    public List<Integer>        getRolls(int i)            { return rolls.get(i); }
    public List<Integer>        getMovimientos(int i)      { return movimientos.get(i); }
    public List<Integer>        getThroughputPorTurno()    { return throughputPorTurno; }
    public List<Integer>        getNumberInSystem()        { return numberInSystem; }
    public List<RegistroSalida> getRegistrosSalida()       { return registrosSalida; }
    public int                  getNumEtapas()             { return numEtapas; }
    public int                  getNumTurnos()             { return rolls.get(0).size(); }
}