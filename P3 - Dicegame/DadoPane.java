import javafx.scene.canvas.*;
import javafx.scene.paint.*;

/**
 * Canvas que dibuja un dado con pips para valores 1-6,
 * y el número directamente para dados de más caras.
 */
public class DadoPane extends Canvas {

    private static final int SIZE    = 48;
    private static final int PIP     = 6;
    private final int sides;
    private int valor = 0;

    public DadoPane(int sides) {
        super(SIZE + 4, SIZE + 4);
        this.sides = sides;
        dibujar();
    }

    public void setValor(int v) { this.valor = v; dibujar(); }
    public int  getValor()      { return valor; }

    private void dibujar() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, SIZE + 4, SIZE + 4);

        int ox = 2, oy = 2;

        // Sombra
        gc.setFill(Color.rgb(0, 0, 0, 0.18));
        gc.fillRoundRect(ox + 2, oy + 3, SIZE, SIZE, 10, 10);

        // Cuerpo
        if (valor == 0) {
            gc.setFill(Color.rgb(200, 210, 230));
        } else {
            gc.setFill(Color.rgb(245, 248, 255));
        }
        gc.fillRoundRect(ox, oy, SIZE, SIZE, 10, 10);

        // Borde
        gc.setStroke(Color.rgb(80, 80, 100));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(ox, oy, SIZE, SIZE, 10, 10);

        if (valor == 0) return;

        gc.setFill(Color.rgb(30, 30, 50));

        if (valor <= 6) {
            dibujarPips(gc, ox, oy);
        } else {
            gc.setFill(Color.rgb(10, 36, 106));
            gc.setFont(javafx.scene.text.Font.font("Tahoma", javafx.scene.text.FontWeight.BOLD, 16));
            String t = String.valueOf(valor);
            gc.fillText(t, ox + SIZE / 2 - 5, oy + SIZE / 2 + 6);
        }
    }

    private void dibujarPips(GraphicsContext gc, int ox, int oy) {
        int m = SIZE / 4, c = SIZE / 2;
        int[][] pips;
        switch (valor) {
            case 1: pips = new int[][]{{c,c}}; break;
            case 2: pips = new int[][]{{m,m},{SIZE-m,SIZE-m}}; break;
            case 3: pips = new int[][]{{m,m},{c,c},{SIZE-m,SIZE-m}}; break;
            case 4: pips = new int[][]{{m,m},{SIZE-m,m},{m,SIZE-m},{SIZE-m,SIZE-m}}; break;
            case 5: pips = new int[][]{{m,m},{SIZE-m,m},{c,c},{m,SIZE-m},{SIZE-m,SIZE-m}}; break;
            default: pips = new int[][]{{m,m},{SIZE-m,m},{m,c},{SIZE-m,c},{m,SIZE-m},{SIZE-m,SIZE-m}}; break;
        }
        for (int[] p : pips) {
            gc.fillOval(ox + p[0] - PIP/2, oy + p[1] - PIP/2, PIP, PIP);
        }
    }
}