public class Ficha {
    public enum Tipo { INICIAL, NUEVA }

    private int  id;
    private Tipo tipo;
    private int  turnoEntrada; // turno en que entró al sistema

    public Ficha(int id, Tipo tipo) {
        this.id           = id;
        this.tipo         = tipo;
        this.turnoEntrada = 0;
    }

    public Ficha(int id) { this(id, Tipo.NUEVA); }

    public int  getId()           { return id; }
    public Tipo getTipo()         { return tipo; }
    public int  getTurnoEntrada() { return turnoEntrada; }
    public void setTurnoEntrada(int t) { this.turnoEntrada = t; }
    public void setId(int id)     { this.id = id; }
}