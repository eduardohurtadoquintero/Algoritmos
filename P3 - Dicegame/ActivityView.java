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
 * Ventana Activity – historial de rolls y movimientos por estacion (JavaFX puro).
 */
public class ActivityView extends Stage {

    private static final Color XP_TOP    = Color.rgb( 10,  36, 106);
    private static final Color XP_BOT    = Color.rgb( 58, 110, 165);
    private static final Color ROLL_CLR  = Color.rgb( 58, 110, 165);
    private static final Color MOV_CLR   = Color.rgb(196,  30,  30);
    private static final Color GRID_CLR  = Color.rgb(180, 200, 230);
    private static final Color BG_TOP    = Color.rgb(212, 228, 248);
    private static final Color BG_BOT    = Color.rgb(240, 246, 255);

    private final ActivityData data;
    private final int numEtapas;

    private Canvas   chartCanvas;
    private Button[] btnEstaciones;
    private ToggleButton btnToggleRoll;
    private ToggleButton btnToggleMov;

    private int     estacionActual = 0;
    private boolean showRoll       = true;
    private boolean showMov        = true;

    public ActivityView(ActivityData data) {
        this.data      = data;
        this.numEtapas = data.getNumEtapas();

        setTitle("Activity - Historial por Estacion");
        construirUI();
        setWidth(820);
        setHeight(520);
    }

    private void construirUI() {
        BorderPane root = new BorderPane();

        // --- Barra titulo ---
        HBox titleBar = new HBox();
        titleBar.setStyle("-fx-background-color: linear-gradient(to right, #0a246a, #3a6ea5);");
        titleBar.setPadding(new Insets(8, 16, 8, 16));
        titleBar.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label("Activity - Historial de Actividad por Estacion");
        lbl.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.WHITE);
        titleBar.getChildren().add(lbl);
        root.setTop(titleBar);

        // --- Panel izquierdo: selector estacion ---
        VBox panelIzq = new VBox(6);
        panelIzq.setAlignment(Pos.TOP_CENTER);
        panelIzq.setPadding(new Insets(16, 10, 16, 10));
        panelIzq.setPrefWidth(110);
        panelIzq.setStyle("-fx-background-color: linear-gradient(to bottom, #3a6ea5, #0a246a);");

        Label lblSel = new Label("Estacion");
        lblSel.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
        lblSel.setTextFill(Color.rgb(212, 228, 248));
        panelIzq.getChildren().add(lblSel);

        btnEstaciones = new Button[numEtapas];
        for (int i = 0; i < numEtapas; i++) {
            final int idx = i;
            Button btn = new Button("E" + (i + 1));
            btn.setFont(Font.font("Tahoma", 11));
            btn.setPrefSize(88, 28);
            btn.setMaxSize(88, 28);
            btn.setOnAction(e -> seleccionarEstacion(idx));
            estiloBtn(btn, false);
            btnEstaciones[i] = btn;
            panelIzq.getChildren().add(btn);
        }
        root.setLeft(panelIzq);

        // --- Canvas central ---
        chartCanvas = new Canvas(600, 400);
        StackPane wrap = new StackPane(chartCanvas);
        chartCanvas.widthProperty().bind(wrap.widthProperty());
        chartCanvas.heightProperty().bind(wrap.heightProperty());
        chartCanvas.widthProperty().addListener(e -> dibujar());
        chartCanvas.heightProperty().addListener(e -> dibujar());
        root.setCenter(wrap);

        // --- Panel inferior: toggles ---
        HBox panelSur = new HBox(16);
        panelSur.setAlignment(Pos.CENTER);
        panelSur.setPadding(new Insets(10));
        panelSur.setStyle("-fx-background-color: rgb(180,205,235); -fx-border-color: #0a246a; -fx-border-width: 1 0 0 0;");

        Label lblMostrar = new Label("Mostrar:");
        lblMostrar.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
        lblMostrar.setTextFill(Color.rgb(10, 36, 106));

        btnToggleRoll = new ToggleButton("Roll");
        btnToggleRoll.setSelected(true);
        estiloToggle(btnToggleRoll, ROLL_CLR);
        btnToggleRoll.selectedProperty().addListener((obs, o, n) -> { showRoll = n; dibujar(); });

        btnToggleMov = new ToggleButton("Movido");
        btnToggleMov.setSelected(true);
        estiloToggle(btnToggleMov, MOV_CLR);
        btnToggleMov.selectedProperty().addListener((obs, o, n) -> { showMov = n; dibujar(); });

        panelSur.getChildren().addAll(lblMostrar, btnToggleRoll, btnToggleMov);
        root.setBottom(panelSur);

        setScene(new Scene(root));
        seleccionarEstacion(0);
    }

    private void seleccionarEstacion(int idx) {
        this.estacionActual = idx;
        for (int i = 0; i < numEtapas; i++) estiloBtn(btnEstaciones[i], i == idx);
        dibujar();
    }

    public void actualizar() {
        dibujar();
        show();
    }

    private void dibujar() {
        GraphicsContext gc = chartCanvas.getGraphicsContext2D();
        double W = chartCanvas.getWidth(), H = chartCanvas.getHeight();
        int PL = 50, PR = 20, PT = 30, PB = 50;
        double cW = W - PL - PR, cH = H - PT - PB;

        gc.clearRect(0, 0, W, H);
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, BG_TOP), new Stop(1, BG_BOT));
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);

        List<Integer> rolls = data.getRolls(estacionActual);
        List<Integer> movs  = data.getMovimientos(estacionActual);

        String titulo = data.getNumTurnos() == 0
            ? "Estacion " + (estacionActual + 1)
            : "Estacion " + (estacionActual + 1) + " - Turnos 1 a " + data.getNumTurnos();

        if (rolls == null || rolls.isEmpty()) {
            gc.setFill(Color.rgb(100, 130, 180));
            gc.setFont(Font.font("Tahoma", FontPosture.ITALIC, 13));
            gc.fillText("Aun no hay turnos registrados", PL + 10, H / 2);
            gc.setFill(Color.rgb(10, 36, 106));
            gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
            gc.fillText(titulo, PL + 10, 18);
            return;
        }

        int n = rolls.size();
        int maxVal = 1;
        if (showRoll) for (int v : rolls) maxVal = Math.max(maxVal, v);
        if (showMov)  for (int v : movs)  maxVal = Math.max(maxVal, v);
        maxVal = Math.max(maxVal, 4);

        // Grid
        int gl = Math.min(maxVal, 8);
        gc.setFont(Font.font("Tahoma", 10));
        for (int i = 0; i <= gl; i++) {
            double y = PT + cH - (i * cH / gl);
            gc.setStroke(GRID_CLR);
            gc.setLineWidth(i == 0 ? 1.5 : 0.7);
            if (i > 0) gc.setLineDashes(4, 4); else gc.setLineDashes(0);
            gc.strokeLine(PL, y, PL + cW, y);
            gc.setLineDashes(0);
            gc.setFill(Color.rgb(60, 80, 130));
            gc.fillText(String.valueOf((int)(i * maxVal / gl)), 4, y + 4);
        }

        // Barras
        int numS = (showRoll ? 1 : 0) + (showMov ? 1 : 0);
        if (numS == 0) { dibujarEjes(gc, PL, PT, cW, cH); return; }

        double slotW = cW / n;
        double barW  = Math.max(4, slotW / numS - 3);

        for (int i = 0; i < n; i++) {
            double gx = PL + i * slotW;
            int s = 0;
            if (showRoll) {
                int v  = rolls.get(i);
                double bH = v * cH / maxVal;
                double bX = gx + s * (barW + 2), bY = PT + cH - bH;
                gc.setFill(ROLL_CLR.brighter()); gc.fillRoundRect(bX, bY, barW, bH, 3, 3);
                gc.setStroke(ROLL_CLR.darker()); gc.setLineWidth(1); gc.strokeRoundRect(bX, bY, barW, bH, 3, 3);
                gc.setFill(Color.rgb(10, 36, 106)); gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 8));
                gc.fillText(String.valueOf(v), bX + 1, bY - 2);
                s++;
            }
            if (showMov) {
                int v  = movs.get(i);
                double bH = v * cH / maxVal;
                double bX = gx + s * (barW + 2), bY = PT + cH - bH;
                gc.setFill(MOV_CLR.brighter()); gc.fillRoundRect(bX, bY, barW, bH, 3, 3);
                gc.setStroke(MOV_CLR.darker()); gc.setLineWidth(1); gc.strokeRoundRect(bX, bY, barW, bH, 3, 3);
                gc.setFill(Color.rgb(100, 0, 0)); gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 8));
                gc.fillText(String.valueOf(v), bX + 1, bY - 2);
            }
            gc.setFill(Color.rgb(60, 80, 130));
            gc.setFont(Font.font("Tahoma", 9));
            gc.fillText("T" + (i + 1), gx + (slotW / 2) - 6, PT + cH + 14);
        }

        dibujarEjes(gc, PL, PT, cW, cH);

        // Titulo
        gc.setFill(Color.rgb(10, 36, 106)); gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
        gc.fillText(titulo, PL + 10, 18);

        // Leyenda
        if (showRoll) {
            gc.setFill(ROLL_CLR); gc.fillRect(PL + 4, H - PB + 22, 12, 10);
            gc.setFill(Color.rgb(10, 36, 106)); gc.setFont(Font.font("Tahoma", 10));
            gc.fillText("Roll", PL + 20, H - PB + 31);
        }
        if (showMov) {
            gc.setFill(MOV_CLR); gc.fillRect(PL + 64, H - PB + 22, 12, 10);
            gc.setFill(Color.rgb(100, 0, 0)); gc.setFont(Font.font("Tahoma", 10));
            gc.fillText("Movido", PL + 80, H - PB + 31);
        }
    }

    private void dibujarEjes(GraphicsContext gc, int PL, int PT, double cW, double cH) {
        gc.setStroke(Color.rgb(10, 36, 106)); gc.setLineWidth(1.8); gc.setLineDashes(0);
        gc.strokeLine(PL, PT, PL, PT + cH);
        gc.strokeLine(PL, PT + cH, PL + cW, PT + cH);
    }

    private void estiloBtn(Button btn, boolean sel) {
        String bg = sel ? "rgb(10,36,106)"  : "rgb(212,228,248)";
        String fg = sel ? "rgb(255,255,255)" : "rgb(10,36,106)";
        String wt = sel ? "bold" : "normal";
        btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-weight: %s;" +
            "-fx-background-radius: 6; -fx-border-color: rgb(10,36,106);" +
            "-fx-border-radius: 6; -fx-border-width: 1; -fx-cursor: hand;", bg, fg, wt));
    }

    private void estiloToggle(ToggleButton btn, Color selColor) {
        btn.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
        btn.setPrefSize(110, 32);
        String r = (int)(selColor.getRed()*255)+","+(int)(selColor.getGreen()*255)+","+(int)(selColor.getBlue()*255);
        btn.setStyle(String.format(
            "-fx-background-color: rgb(%s); -fx-text-fill: white;" +
            "-fx-background-radius: 8; -fx-border-color: rgb(10,36,106);" +
            "-fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;", r));
        btn.selectedProperty().addListener((obs, o, n) -> {
            if (n) {
                btn.setStyle(String.format(
                    "-fx-background-color: rgb(%s); -fx-text-fill: white;" +
                    "-fx-background-radius: 8; -fx-border-color: rgb(10,36,106);" +
                    "-fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;", r));
            } else {
                btn.setStyle(
                    "-fx-background-color: rgb(190,210,235); -fx-text-fill: rgb(10,36,106);" +
                    "-fx-background-radius: 8; -fx-border-color: rgb(10,36,106);" +
                    "-fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;");
            }
        });
    }
}