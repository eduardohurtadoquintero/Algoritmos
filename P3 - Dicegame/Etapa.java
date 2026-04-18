public class Etapa {
    private Dice dado;
    private Etapa siguienteEtapa;
    private ColaCircular<Ficha> fichas;
    private int cupoMax;
    private boolean entradaInfinita;
    private int contadorFichas;
    private int ultimoRoll;
    private int ultimoMovimiento;
    private int totalSalidas;
    private ActivityData activityData; // solo la etapa final lo usa

    public Etapa(int ladosDado, int cupoMax) {
        this.dado             = new Dice(ladosDado);
        this.fichas           = new ColaCircular<>(10000);
        this.cupoMax          = cupoMax;
        this.entradaInfinita  = false;
        this.contadorFichas   = 0;
        this.ultimoRoll       = 0;
        this.ultimoMovimiento = 0;
        this.totalSalidas     = 0;
    }

    public void setEntradaInfinita(boolean v)      { this.entradaInfinita = v; }
    public void setSiguienteEtapa(Etapa e)         { this.siguienteEtapa  = e; }
    public void setActivityData(ActivityData data) { this.activityData    = data; }

    public void recibirFicha(Ficha ficha) { fichas.insertar(ficha); }

    public void tirarDado() {
        if (!entradaInfinita && fichas.estaVacia()) { ultimoRoll = 0; return; }
        ultimoRoll = dado.roll();
    }

    /** Mover fichas usando el roll ya guardado. turnoActual se usa para calcular duración. */
    public int mover(int turnoActual) {
        if (!entradaInfinita && fichas.estaVacia()) { ultimoMovimiento = 0; return 0; }
        int cantidad = Math.min(ultimoRoll, cupoMax);
        int movidas  = 0;
        for (int i = 0; i < cantidad; i++) {
            Ficha f;
            if (entradaInfinita) {
                f = new Ficha(++contadorFichas, Ficha.Tipo.NUEVA);
                f.setTurnoEntrada(turnoActual);
            } else {
                if (fichas.estaVacia()) break;
                f = fichas.eliminar();
            }
            if (siguienteEtapa != null) {
                siguienteEtapa.recibirFicha(f);
            } else {
                // Etapa final: ficha completada
                totalSalidas++;
                if (activityData != null && f.getTipo() == Ficha.Tipo.NUEVA) {
                    int duracion = turnoActual - f.getTurnoEntrada();
                    activityData.registrarSalida(f.getTurnoEntrada(), turnoActual, duracion);
                }
            }
            movidas++;
        }
        ultimoMovimiento = movidas;
        return movidas;
    }

    public int procesar(int turnoActual) { tirarDado(); return mover(turnoActual); }

    public Ficha.Tipo[] getTiposFichas() {
        int n = fichas.getSize();
        Ficha.Tipo[] tipos = new Ficha.Tipo[n];
        ColaCircular<Ficha> temp = new ColaCircular<>(n + 1);
        for (int i = 0; i < n; i++) {
            Ficha f = fichas.eliminar();
            tipos[i] = f.getTipo();
            temp.insertar(f);
        }
        for (int i = 0; i < n; i++) fichas.insertar(temp.eliminar());
        return tipos;
    }

    public int     getCantidadFichas()   { return fichas.getSize(); }
    public int     getCupoMax()          { return cupoMax; }
    public int     getUltimoRoll()       { return ultimoRoll; }
    public int     getUltimoMovimiento() { return ultimoMovimiento; }
    public boolean isEntradaInfinita()   { return entradaInfinita; }
    public int     getSidesDelDado()     { return dado.getSides(); }
    public int     getTotalSalidas()     { return totalSalidas; }

    public String verEstado() {
        if (entradaInfinita) return "entrada: (∞)";
        int n = fichas.getSize();
        if (n == 0) return "(vacía)";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append("*");
        return sb.toString();
    }
}