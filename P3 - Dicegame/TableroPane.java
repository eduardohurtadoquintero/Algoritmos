import javafx.scene.canvas.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;

/**
 * Pane principal del tablero en serpentina con flechas entre estaciones.
 * Mejoras: flechas mas claras con numeros de orden, zona de salida visible.
 */
public class TableroPane extends Pane {

    private static final int COLS  = 5;
    private static final int ST_W  = 120, ST_H  = 260;
    private static final int GAP_X = 55,  GAP_Y = 80;
    private static final int PAD_X = 30,  PAD_Y = 30;

    private final EstacionPane[] panes;
    private final SalidaPane     salidaPane;
    private final int            numEtapas;
    private final Canvas         arrowCanvas;

    public TableroPane(Etapa[] etapas) {
        this.numEtapas  = etapas.length;
        this.panes      = new EstacionPane[numEtapas];
        this.salidaPane = new SalidaPane();

        int filas  = (int) Math.ceil((double) numEtapas / COLS);
        // Espacio extra abajo para la zona de salida (debajo de E10)
        int SALIDA_GAP = 50;
        int totalW = PAD_X * 2 + COLS * ST_W + (COLS - 1) * GAP_X;
        int totalH = PAD_Y * 2 + filas * ST_H + (filas - 1) * GAP_Y + SALIDA_GAP + ST_H;
        setPrefSize(totalW, totalH);

        arrowCanvas = new Canvas(totalW, totalH);
        getChildren().add(arrowCanvas);

        for (int i = 0; i < numEtapas; i++) {
            Etapa e = etapas[i];
            panes[i] = new EstacionPane(i + 1, e.getSidesDelDado(), e.getCupoMax(), e.isEntradaInfinita());
            int[] pos = posicion(i);
            panes[i].setLayoutX(pos[0]);
            panes[i].setLayoutY(pos[1]);
            getChildren().add(panes[i]);
        }

        // Zona de salida DEBAJO de E10 (ultima estacion)
        int[] posUltima = posicion(numEtapas - 1);
        salidaPane.setLayoutX(posUltima[0]);
        salidaPane.setLayoutY(posUltima[1] + ST_H + SALIDA_GAP);
        getChildren().add(salidaPane);

        dibujarFlechas();
    }

    /** Calcula posicion XY en la serpentina para el indice dado. */
    private int[] posicion(int i) {
        int fila = i / COLS;
        int col  = i % COLS;
        // Filas impares van de derecha a izquierda
        if (fila % 2 == 1) col = COLS - 1 - col;
        return new int[]{ PAD_X + col * (ST_W + GAP_X), PAD_Y + fila * (ST_H + GAP_Y) };
    }

    private void dibujarFlechas() {
        GraphicsContext gc = arrowCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, arrowCanvas.getWidth(), arrowCanvas.getHeight());

        Color arrColor  = Color.rgb(10, 36, 106, 0.85);
        Color numColor  = Color.rgb(255, 255, 255, 0.95);
        Color pillColor = Color.rgb(10, 36, 106, 0.75);

        for (int i = 0; i < numEtapas - 1; i++) {
            int[] p1 = posicion(i), p2 = posicion(i + 1);
            int orden = i + 1; // numero de flecha = transicion entre E(i+1) y E(i+2)

            if (p1[1] == p2[1]) {
                // Misma fila: flecha horizontal
                boolean izqDer = p2[0] > p1[0];
                int x1 = izqDer ? p1[0] + ST_W : p1[0];
                int x2 = izqDer ? p2[0]         : p2[0] + ST_W;
                int cy = p1[1] + ST_H / 2;
                dibujarFlechaH(gc, x1, x2, cy, izqDer, arrColor);
                // Etiqueta numerada en el centro de la flecha
                dibujarPillNumero(gc, (x1 + x2) / 2, cy, orden, pillColor, numColor);
            } else {
                // Cambio de fila: flecha en L
                int cx1 = p1[0] + ST_W / 2;
                int cx2 = p2[0] + ST_W / 2;
                int y1  = p1[1] + ST_H;
                int y2  = p2[1] + ST_H / 2;
                dibujarFlechaL(gc, cx1, y1, cx2, y2, arrColor);
                int midY = y1 + (y2 - y1) / 2;
                dibujarPillNumero(gc, (cx1 + cx2) / 2, midY, orden, pillColor, numColor);
            }
        }

        // Flecha E(ultima) -> Salida: vertical hacia abajo
        int[] pUlt = posicion(numEtapas - 1);
        int fxArr  = pUlt[0] + ST_W / 2;          // centro horizontal de E10
        int fyArr1 = pUlt[1] + ST_H;              // base de E10
        int fyArr2 = (int) salidaPane.getLayoutY(); // tope de SalidaPane
        Color salidaColor = Color.rgb(0, 150, 100, 0.9);
        gc.setStroke(salidaColor);
        gc.setLineWidth(2.5);
        gc.setLineDashes(0);
        gc.strokeLine(fxArr, fyArr1 + 4, fxArr, fyArr2 - 4);
        // Punta flecha hacia abajo
        gc.setFill(salidaColor);
        gc.fillPolygon(
            new double[]{ fxArr, fxArr - 6, fxArr + 6 },
            new double[]{ fyArr2 - 2, fyArr2 - 12, fyArr2 - 12 }, 3);
        // Pill con numero en el centro
        int pillY = (fyArr1 + fyArr2) / 2;
        dibujarPillNumero(gc, fxArr, pillY, numEtapas, Color.rgb(0, 130, 80, 0.85), numColor);
    }

    private void dibujarFlechaH(GraphicsContext gc, int x1, int x2, int cy,
                                  boolean derecha, Color c) {
        gc.setStroke(c);
        gc.setLineWidth(2.5);
        gc.setLineDashes(0);
        gc.strokeLine(x1 + 4, cy, x2 - 4, cy);

        // Punta de flecha
        double ax = derecha ? x2 - 4 : x1 + 4;
        double dx = derecha ? 1 : -1;
        gc.setFill(c);
        gc.fillPolygon(
            new double[]{ ax, ax - dx * 10, ax - dx * 10 },
            new double[]{ cy, cy - 5, cy + 5 }, 3);

        // Lineas decorativas punteadas (mas suaves)
        gc.setStroke(c.deriveColor(0, 1, 1, 0.3));
        gc.setLineWidth(1);
        gc.setLineDashes(4, 4);
        gc.strokeLine(x1 + 4, cy - 8, x2 - 4, cy - 8);
        gc.strokeLine(x1 + 4, cy + 8, x2 - 4, cy + 8);
        gc.setLineDashes(0);
    }

    private void dibujarFlechaL(GraphicsContext gc, int x1, int y1,
                                  int x2, int y2, Color c) {
        gc.setStroke(c);
        gc.setLineWidth(2.5);
        gc.setLineDashes(0);
        int midY = y1 + (y2 - y1) / 2;
        // Tramo 1: vertical desde base de estacion origen hasta mitad del gap
        gc.strokeLine(x1, y1 + 4, x1, midY);
        // Tramo 2: horizontal entre los dos centros
        gc.strokeLine(x1, midY, x2, midY);
        // Tramo 3: vertical desde mitad del gap hasta el lado de la estacion destino
        gc.strokeLine(x2, midY, x2, y2 - 10);

        // Punta de flecha al final del tramo 3, apuntando HACIA y2 (siempre baja al siguiente row)
        gc.setFill(c);
        gc.fillPolygon(
            new double[]{ x2,      x2 - 7,  x2 + 7  },
            new double[]{ y2,      y2 - 11, y2 - 11 }, 3);
    }

    /**
     * Dibuja un ovalo con numero encima de la flecha para indicar el orden de flujo.
     */
    private void dibujarPillNumero(GraphicsContext gc, double cx, double cy,
                                    int numero, Color bg, Color fg) {
        double pw = 20, ph = 16;
        gc.setFill(bg);
        gc.fillRoundRect(cx - pw / 2, cy - ph / 2, pw, ph, ph, ph);
        gc.setStroke(fg.deriveColor(0, 1, 1, 0.5));
        gc.setLineWidth(0.8);
        gc.strokeRoundRect(cx - pw / 2, cy - ph / 2, pw, ph, ph, ph);
        gc.setFill(fg);
        gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 9));
        String txt = String.valueOf(numero);
        gc.fillText(txt, cx - (txt.length() > 1 ? 5 : 3), cy + 4);
    }

    public void actualizar(Etapa[] etapas) {
        for (int i = 0; i < numEtapas; i++)
            panes[i].actualizar(etapas[i].getTiposFichas(),
                                etapas[i].getUltimoRoll(),
                                etapas[i].getUltimoMovimiento());
        salidaPane.setTotal(etapas[numEtapas - 1].getTotalSalidas());
    }

    public void mostrarRolls(Etapa[] etapas) {
        for (int i = 0; i < numEtapas; i++)
            panes[i].mostrarRoll(etapas[i].getUltimoRoll());
    }

    public void mostrarMovimientos(Etapa[] etapas) {
        for (int i = 0; i < numEtapas; i++) {
            panes[i].mostrarMovimiento(etapas[i].getUltimoMovimiento());
            panes[i].setTiposFichas(etapas[i].getTiposFichas());
        }
        salidaPane.setTotal(etapas[numEtapas - 1].getTotalSalidas());
    }
}