import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.*;
import java.util.List;

/**
 * Ventana Time in System – duracion de cada ficha completada (JavaFX puro).
 * Solo disponible al terminar la partida.
 */
public class TimeInSystemView extends Stage {

    private static final Color XP_TOP    = Color.rgb( 10,  36, 106);
    private static final Color XP_BOT    = Color.rgb( 58, 110, 165);
    private static final Color COLOR_BAR = Color.rgb( 30, 180, 140);
    private static final Color COLOR_BAR_B = Color.rgb(  0, 120,  90);
    private static final Color COLOR_LINE  = Color.rgb(220, 140,  20);
    private static final Color COLOR_BG    = Color.rgb(230, 248, 240);
    private static final Color COLOR_BG2   = Color.rgb(210, 240, 228);
    private static final Color COLOR_GRID  = Color.rgb(160, 210, 190);

    private final ActivityData data;
    private Canvas chartCanvas;
    private HBox   panelStats;

    public TimeInSystemView(ActivityData data) {
        this.data = data;
        setTitle("Time in System - Duracion de cada ficha completada");
        construirUI();
        setWidth(840);
        setHeight(520);
    }

    private void construirUI() {
        BorderPane root = new BorderPane();

        // Barra titulo
        HBox titleBar = new HBox();
        titleBar.setStyle("-fx-background-color: linear-gradient(to right, #0a246a, #3a6ea5);");
        titleBar.setPadding(new Insets(8, 16, 8, 16));
        titleBar.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label("Time in System - Turnos que tardo cada ficha en completar el recorrido");
        lbl.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.WHITE);
        titleBar.getChildren().add(lbl);
        root.setTop(titleBar);

        // Canvas con scroll horizontal
        chartCanvas = new Canvas(700, 400);
        ScrollPane scroll = new ScrollPane();
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        StackPane wrap = new StackPane(chartCanvas);
        wrap.setMinWidth(700);
        // Canvas se redimensiona con wrap verticalmente
        chartCanvas.heightProperty().bind(wrap.heightProperty());
        chartCanvas.heightProperty().addListener(e -> dibujar());

        scroll.setContent(wrap);
        scroll.setFitToHeight(true);
        root.setCenter(scroll);

        // Panel inferior estadisticas
        panelStats = new HBox(24);
        panelStats.setAlignment(Pos.CENTER);
        panelStats.setPadding(new Insets(10));
        panelStats.setStyle("-fx-background-color: rgb(180,230,210); -fx-border-color: #0a246a; -fx-border-width: 1 0 0 0;");
        root.setBottom(panelStats);

        actualizarStats();
        setScene(new Scene(root));
    }

    private void actualizarStats() {
        panelStats.getChildren().clear();
        List<ActivityData.RegistroSalida> registros = data.getRegistrosSalida();
        if (registros.isEmpty()) {
            Label v = new Label("No hay fichas completadas.");
            v.setFont(Font.font("Tahoma", FontPosture.ITALIC, 12));
            v.setTextFill(Color.rgb(0, 80, 50));
            panelStats.getChildren().add(v);
        } else {
            int    minD = registros.stream().mapToInt(r -> r.duracion).min().getAsInt();
            int    maxD = registros.stream().mapToInt(r -> r.duracion).max().getAsInt();
            double avg  = registros.stream().mapToInt(r -> r.duracion).average().getAsDouble();
            panelStats.getChildren().addAll(
                crearStat("Total completadas", registros.size() + " fichas"),
                crearStat("Mas rapida",        minD + " turnos"),
                crearStat("Mas lenta",         maxD + " turnos"),
                crearStat("Promedio",           String.format("%.1f turnos", avg))
            );
        }
    }

    private VBox crearStat(String titulo, String valor) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        Label t = new Label(titulo);
        t.setFont(Font.font("Tahoma", 10));
        t.setTextFill(Color.rgb(0, 80, 50));
        Label v = new Label(valor);
        v.setFont(Font.font("Tahoma", FontWeight.BOLD, 14));
        v.setTextFill(Color.rgb(10, 36, 106));
        box.getChildren().addAll(t, v);
        return box;
    }

    public void mostrar() {
        actualizarStats();
        // Ajusta ancho del canvas segun fichas
        int n = data.getRegistrosSalida().size();
        double minW = Math.max(700, 55 + 20 + n * 32.0);
        chartCanvas.setWidth(minW);
        dibujar();
        show();
        toFront();
    }

    private void dibujar() {
        GraphicsContext gc = chartCanvas.getGraphicsContext2D();
        double W  = chartCanvas.getWidth();
        double H  = chartCanvas.getHeight();
        int PL = 55, PR = 20, PT = 40, PB = 60;
        double cW = W - PL - PR, cH = H - PT - PB;

        gc.clearRect(0, 0, W, H);

        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, COLOR_BG2), new Stop(1, COLOR_BG));
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);

        List<ActivityData.RegistroSalida> registros = data.getRegistrosSalida();

        if (registros.isEmpty()) {
            gc.setFill(Color.rgb(0, 100, 60));
            gc.setFont(Font.font("Tahoma", FontPosture.ITALIC, 14));
            gc.fillText("No hay fichas completadas", W / 2 - 100, H / 2);
            return;
        }

        int n      = registros.size();
        int maxVal = registros.stream().mapToInt(r -> r.duracion).max().getAsInt();
        maxVal     = Math.max(maxVal, 1);
        double avg = registros.stream().mapToInt(r -> r.duracion).average().getAsDouble();

        // Grid
        int gridLines = Math.min(maxVal, 10);
        gc.setFont(Font.font("Tahoma", 10));
        for (int gl = 0; gl <= gridLines; gl++) {
            double y = PT + cH - ((double) gl / gridLines * cH);
            gc.setStroke(COLOR_GRID);
            gc.setLineWidth(gl == 0 ? 1.5 : 0.7);
            if (gl > 0) gc.setLineDashes(4, 4); else gc.setLineDashes(0);
            gc.strokeLine(PL, y, PL + cW, y);
            gc.setLineDashes(0);
            String label = String.valueOf((int) Math.round((double) gl / gridLines * maxVal));
            gc.setFill(Color.rgb(0, 80, 50));
            gc.fillText(label, 4, y + 4);
        }

        double slotW = cW / n;
        double barW  = Math.max(6, slotW - 6);

        for (int i = 0; i < n; i++) {
            ActivityData.RegistroSalida r = registros.get(i);
            double bH = Math.max((double) r.duracion / maxVal * cH, 2);
            double bX = PL + i * slotW + (slotW - barW) / 2;
            double bY = PT + cH - bH;

            LinearGradient barGrad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, COLOR_BAR.brighter()), new Stop(1, COLOR_BAR.darker()));
            gc.setFill(barGrad);
            gc.fillRoundRect(bX, bY, barW, bH, 4, 4);
            gc.setStroke(COLOR_BAR_B);
            gc.setLineWidth(1);
            gc.strokeRoundRect(bX, bY, barW, bH, 4, 4);

            // Valor encima
            gc.setFill(Color.rgb(0, 60, 40));
            gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 9));
            String vs = String.valueOf(r.duracion);
            if (barW >= vs.length() * 5 + 2) gc.fillText(vs, bX + (barW - vs.length() * 5) / 2, bY - 3);

            // Etiqueta eje X
            gc.setFill(Color.rgb(0, 80, 50));
            gc.setFont(Font.font("Tahoma", 9));
            String tl = "#" + (i + 1);
            gc.fillText(tl, bX + (barW - tl.length() * 4) / 2, PT + cH + 14);
        }

        // Linea promedio
        double yAvg = PT + cH - (avg / maxVal * cH);
        gc.setStroke(COLOR_LINE);
        gc.setLineWidth(2);
        gc.setLineDashes(6, 4);
        gc.strokeLine(PL, yAvg, PL + cW, yAvg);
        gc.setLineDashes(0);
        gc.setFill(COLOR_LINE);
        gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 10));
        gc.fillText(String.format("Promedio: %.1f turnos", avg), PL + 6, yAvg - 5);

        // Ejes
        gc.setStroke(Color.rgb(0, 80, 50));
        gc.setLineWidth(1.8);
        gc.strokeLine(PL, PT, PL, PT + cH);
        gc.strokeLine(PL, PT + cH, PL + cW, PT + cH);

        // Titulo eje Y
        gc.setFill(Color.rgb(0, 80, 50));
        gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 11));
        gc.fillText("Turnos en sistema", PL, PT - 12);

        // Etiqueta eje X
        gc.setFont(Font.font("Tahoma", 10));
        String xLabel = "Ficha #1 a #" + n + "  (orden de salida)";
        gc.fillText(xLabel, PL + cW / 2 - xLabel.length() * 2.5, PT + cH + 32);

        // Titulo principal
        gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));
        gc.setFill(Color.rgb(0, 80, 50));
        gc.fillText("Time in System  (Total fichas: " + n + ")", PL + 10, 26);
    }
}