import javafx.scene.canvas.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;

/**
 * Panel de fichas completadas (salida de la ultima estacion).
 * Aparece a la derecha de E10 en el tablero.
 */
public class SalidaPane extends Pane {

    private static final int W    = 130;
    private static final int H    = 260;
    private static final int FS   = 14;
    private static final int FG   = 3;
    private static final Color FILL = Color.rgb( 30, 180, 140, 0.92);
    private static final Color BORD = Color.rgb(  0, 120,  90, 0.92);
    private static final Color TOP  = Color.rgb(180, 235, 215);
    private static final Color BOT  = Color.rgb(215, 248, 235);
    private static final Color HDR  = Color.rgb(  0, 100,  60);

    private final Canvas canvas;
    private int total = 0;

    public SalidaPane() {
        setPrefSize(W, H);
        setMaxSize(W, H);
        setMinSize(W, H);
        setStyle(
            "-fx-border-color: rgb(0,140,90);" +
            "-fx-border-width: 2.5;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,140,90,0.35), 8, 0, 0, 2);"
        );
        canvas = new Canvas(W, H);
        getChildren().add(canvas);
        dibujar();
    }

    public void setTotal(int n) {
        this.total = n;
        dibujar();
    }

    private void dibujar() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, W, H);

        // Fondo
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, TOP), new Stop(1, BOT));
        gc.setFill(bg);
        gc.fillRoundRect(0, 0, W - 1, H - 1, 14, 14);

        // Cabecera coloreada
        gc.setFill(Color.rgb(0, 130, 80, 0.85));
        gc.fillRoundRect(0, 0, W - 1, 36, 14, 14);
        gc.fillRect(0, 18, W - 1, 18);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 11));
        gc.fillText("COMPLETADAS", 14, 22);

        // Contador grande
        gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 36));
        gc.setFill(Color.rgb(0, 110, 60));
        String c = String.valueOf(total);
        double cw = c.length() * 22.0;
        gc.fillText(c, (W - cw) / 2, 72);

        // Subtitulo
        gc.setFont(Font.font("Tahoma", FontPosture.ITALIC, 10));
        gc.setFill(Color.rgb(0, 80, 50));
        String sub = total == 1 ? "ficha" : "fichas";
        gc.fillText(sub, (W - sub.length() * 5.0) / 2, 85);

        // Grid de fichas visuales (max 40)
        int mostrar = Math.min(total, 40);
        if (mostrar > 0) {
            int cols   = 4;
            int startX = (W - (Math.min(mostrar, cols) * (FS + FG) - FG)) / 2;
            int startY = 94;

            for (int i = 0; i < mostrar; i++) {
                int fx = startX + (i % cols) * (FS + FG);
                int fy = startY + (i / cols) * (FS + FG);

                gc.setFill(FILL);
                gc.fillRoundRect(fx, fy, FS, FS, 4, 4);
                gc.setStroke(BORD);
                gc.setLineWidth(1);
                gc.strokeRoundRect(fx, fy, FS, FS, 4, 4);
                // pip
                gc.setFill(Color.rgb(255, 255, 255, 0.75));
                gc.fillOval(fx + (FS - 4) / 2.0, fy + (FS - 4) / 2.0, 4, 4);
            }

            if (total > 40) {
                gc.setFill(Color.rgb(0, 100, 60));
                gc.setFont(Font.font("Tahoma", FontPosture.ITALIC, 10));
                gc.fillText("+" + (total - 40) + " mas", 16, H - 14);
            }
        }

        // Linea separadora inferior con throughput acumulado
        gc.setStroke(Color.rgb(0, 140, 90, 0.4));
        gc.setLineWidth(1);
        gc.strokeLine(12, H - 30, W - 12, H - 30);
        gc.setFill(Color.rgb(0, 80, 50));
        gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 9));
        gc.fillText("Throughput: " + total, 14, H - 14);
    }
}