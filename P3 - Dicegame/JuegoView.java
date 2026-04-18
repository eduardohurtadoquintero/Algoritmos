import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.*;

/**
 * Vista principal del juego en JavaFX.
 * Todos los sub-stages (Activity, Throughput, etc.) son JavaFX puros.
 */
public class JuegoView {

    private static final int MAX_TURNOS = 20;

    private enum Fase { INICIO, ROLL_HECHO, MOVIMIENTO_HECHO, FIN }

    private static final String XP_TITLE_CSS =
        "-fx-background-color: linear-gradient(to right, #0a246a, #3a6ea5);";
    private static final String XP_PANEL_CSS =
        "-fx-background-color: linear-gradient(to bottom, #3a6ea5, #0a246a);";
    private static final String XP_BOARD_CSS =
        "-fx-background-color: #c7daf1;";
    private static final Color  XP_BTN_FACE  = Color.rgb(212, 228, 248);
    private static final Color  XP_BTN_TEXT  = Color.rgb( 10,  36, 106);
    private static final Color  XP_BTN_MOVE  = Color.rgb(100, 170, 100);
    private static final Color  XP_BTN_RESET = Color.rgb(180,  60,  60);
    private static final Color  XP_BTN_DIS   = Color.rgb(160, 175, 195);

    private final Etapa[]      etapas;
    private final ActivityData activityData;
    private final Stage        stage;

    private TableroPane        tableroPane;
    private Label              lblCiclo;
    private Button             btnAccion;
    private Button             btnTimeInSystem;
    private Fase               fase = Fase.INICIO;
    private int                cicloActual = 0;

    private ActivityView        activityView;
    private ThroughputView      throughputView;
    private NumberInSystemView  numberInSystemView;
    private TimeInSystemView    timeInSystemView;

    public JuegoView(Etapa[] etapas, Stage stage) {
        this.etapas       = etapas;
        this.activityData = new ActivityData(etapas.length);
        this.stage        = stage;
        etapas[etapas.length - 1].setActivityData(activityData);
    }

    public void mostrar() {
        BorderPane root = new BorderPane();
        root.setTop(crearTitleBar());

        tableroPane = new TableroPane(etapas);
        ScrollPane scroll = new ScrollPane(tableroPane);
        scroll.setStyle(XP_BOARD_CSS + "-fx-border-color:transparent;");
        scroll.setFitToHeight(false);
        tableroPane.actualizar(etapas);
        root.setCenter(scroll);

        root.setRight(crearPanelLateral());

        Scene scene = new Scene(root, 1150, 720);
        stage.setTitle("DiceGame - Simulacion de Produccion");
        stage.setScene(scene);
        stage.show();
    }

    private Node crearTitleBar() {
        HBox bar = new HBox();
        bar.setStyle(XP_TITLE_CSS);
        bar.setPadding(new Insets(8, 16, 8, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label("DiceGame - Simulacion de Produccion");
        lbl.setFont(Font.font("Tahoma", FontWeight.BOLD, 14));
        lbl.setTextFill(Color.WHITE);
        bar.getChildren().add(lbl);
        return bar;
    }

    private Node crearPanelLateral() {
        VBox panel = new VBox(8);
        panel.setStyle(XP_PANEL_CSS);
        panel.setPadding(new Insets(16, 10, 16, 10));
        panel.setPrefWidth(150);
        panel.setAlignment(Pos.TOP_CENTER);

        // Seccion de estadisticas
        Label lblStats = new Label("Estadisticas");
        lblStats.setFont(Font.font("Tahoma", FontWeight.BOLD, 11));
        lblStats.setTextFill(Color.rgb(180, 205, 235));

        Button btnActivity = crearBtn("Activity", XP_BTN_FACE, XP_BTN_TEXT);
        btnActivity.setOnAction(e -> abrirActivity());

        Button btnThroughput = crearBtn("Throughput", XP_BTN_FACE, XP_BTN_TEXT);
        btnThroughput.setOnAction(e -> abrirThroughput());

        Button btnNIS = crearBtn("Num. in System", XP_BTN_FACE, XP_BTN_TEXT);
        btnNIS.setOnAction(e -> abrirNumberInSystem());

        btnTimeInSystem = crearBtn("Time in System", XP_BTN_DIS, Color.rgb(120, 130, 150));
        btnTimeInSystem.setDisable(true);
        btnTimeInSystem.setOnAction(e -> abrirTimeInSystem());

        // Leyenda
        Canvas leyenda = crearLeyendaCanvas();

        // Separador
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Contador turnos
        Label lblTurnsTitle = new Label("Turnos");
        lblTurnsTitle.setFont(Font.font("Tahoma", 12));
        lblTurnsTitle.setTextFill(Color.rgb(212, 228, 248));

        lblCiclo = new Label("0 / " + MAX_TURNOS);
        lblCiclo.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
        lblCiclo.setTextFill(Color.WHITE);

        // Boton Roll/Move
        btnAccion = crearBtn("Roll", XP_BTN_FACE, XP_BTN_TEXT);
        btnAccion.setFont(Font.font("Tahoma", FontWeight.BOLD, 18));
        btnAccion.setPrefSize(126, 44);
        btnAccion.setMaxSize(126, 44);
        btnAccion.setOnAction(e -> manejarAccion());

        // Boton Reset
        Button btnReset = crearBtn("Reiniciar", XP_BTN_RESET, Color.WHITE);
        btnReset.setOnAction(e -> resetear());

        panel.getChildren().addAll(
            lblStats,
            btnActivity, btnThroughput, btnNIS, btnTimeInSystem,
            leyenda, spacer,
            lblTurnsTitle, lblCiclo,
            btnAccion, btnReset
        );
        return panel;
    }

    private Canvas crearLeyendaCanvas() {
        Canvas c = new Canvas(126, 52);
        GraphicsContext gc = c.getGraphicsContext2D();
        // Fondo pill
        gc.setFill(Color.rgb(255, 255, 255, 0.08));
        gc.fillRoundRect(2, 2, 122, 48, 8, 8);
        gc.setStroke(Color.rgb(255, 255, 255, 0.18));
        gc.setLineWidth(1);
        gc.strokeRoundRect(2, 2, 122, 48, 8, 8);

        gc.setFill(Color.rgb(60, 90, 160, 0.85));
        gc.fillRoundRect(10, 12, 13, 13, 3, 3);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Tahoma", 9));
        gc.fillText("Ficha Inicial", 28, 22);

        gc.setFill(Color.rgb(30, 180, 140, 0.90));
        gc.fillRoundRect(10, 30, 13, 13, 3, 3);
        gc.setFill(Color.WHITE);
        gc.fillText("Ficha Nueva", 28, 40);
        return c;
    }

    private Button crearBtn(String texto, Color bg, Color fg) {
        Button btn = new Button(texto);
        btn.setWrapText(true);
        btn.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        btn.setFont(Font.font("Tahoma", 11));
        btn.setTextFill(fg);
        btn.setPrefSize(126, 36);
        btn.setMaxSize(126, 36);
        String r = colorStr(bg), f = colorStr(fg);
        btn.setStyle(String.format(
            "-fx-background-color: rgb(%s); -fx-text-fill: rgb(%s);" +
            "-fx-background-radius: 8; -fx-border-color: rgb(10,36,106);" +
            "-fx-border-radius: 8; -fx-border-width: 1.5; -fx-cursor: hand;", r, f));
        return btn;
    }

    private String colorStr(Color c) {
        return (int)(c.getRed()*255) + "," + (int)(c.getGreen()*255) + "," + (int)(c.getBlue()*255);
    }

    private void setBtnEstilo(Button btn, Color bg, Color fg) {
        btn.setStyle(String.format(
            "-fx-background-color: rgb(%s); -fx-text-fill: rgb(%s);" +
            "-fx-background-radius: 8; -fx-border-color: rgb(10,36,106);" +
            "-fx-border-radius: 8; -fx-border-width: 1.5; -fx-cursor: hand;",
            colorStr(bg), colorStr(fg)));
    }

    private void manejarAccion() {
        if (fase == Fase.FIN) return;

        if (fase == Fase.INICIO || fase == Fase.MOVIMIENTO_HECHO) {
            // --- ROLL ---
            for (Etapa e : etapas) e.tirarDado();
            tableroPane.mostrarRolls(etapas);
            setBtnEstilo(btnAccion, XP_BTN_MOVE, Color.WHITE);
            btnAccion.setText("Mover");
            fase = Fase.ROLL_HECHO;

        } else if (fase == Fase.ROLL_HECHO) {
            // --- MOVE ---
            cicloActual++;
            lblCiclo.setText(cicloActual + " / " + MAX_TURNOS);
            // Mover de atras hacia adelante para evitar doble movimiento
            for (int i = etapas.length - 1; i >= 0; i--) etapas[i].mover(cicloActual);
            tableroPane.mostrarMovimientos(etapas);
            activityData.registrarTurno(etapas);

            // Actualizar vistas abiertas
            if (activityView       != null && activityView.isShowing())        activityView.actualizar();
            if (throughputView     != null && throughputView.isShowing())       throughputView.actualizar();
            if (numberInSystemView != null && numberInSystemView.isShowing())   numberInSystemView.actualizar();

            if (cicloActual >= MAX_TURNOS) {
                terminarPartida();
            } else {
                setBtnEstilo(btnAccion, XP_BTN_FACE, XP_BTN_TEXT);
                btnAccion.setText("Roll");
                fase = Fase.MOVIMIENTO_HECHO;
            }
        }
    }

    private void terminarPartida() {
        fase = Fase.FIN;
        btnAccion.setText("Fin");
        setBtnEstilo(btnAccion, XP_BTN_DIS, Color.rgb(80, 80, 90));
        btnAccion.setDisable(true);

        // Habilitar Time in System
        btnTimeInSystem.setDisable(false);
        setBtnEstilo(btnTimeInSystem, Color.rgb(200, 160, 220), Color.rgb(60, 20, 80));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fin de partida");
        alert.setHeaderText("Partida terminada");
        alert.setContentText(
            "Se completaron " + MAX_TURNOS + " turnos.\n" +
            "Fichas completadas: " + etapas[etapas.length - 1].getTotalSalidas() + "\n\n" +
            "Ya puedes consultar Time in System.");
        alert.show();
    }

    // --- Apertura de vistas ---

    private void abrirActivity() {
        if (activityView == null || !activityView.isShowing()) {
            activityView = new ActivityView(activityData);
        }
        activityView.actualizar();
        activityView.show();
        activityView.toFront();
    }

    private void abrirThroughput() {
        if (throughputView == null || !throughputView.isShowing()) {
            throughputView = new ThroughputView(activityData);
        }
        throughputView.actualizar();
        throughputView.toFront();
    }

    private void abrirNumberInSystem() {
        if (numberInSystemView == null || !numberInSystemView.isShowing()) {
            numberInSystemView = new NumberInSystemView(activityData);
        }
        numberInSystemView.actualizar();
        numberInSystemView.toFront();
    }

    private void abrirTimeInSystem() {
        if (timeInSystemView == null || !timeInSystemView.isShowing()) {
            timeInSystemView = new TimeInSystemView(activityData);
        }
        timeInSystemView.mostrar();
        timeInSystemView.toFront();
    }

    private void resetear() {
        if (activityView       != null) activityView.close();
        if (throughputView     != null) throughputView.close();
        if (numberInSystemView != null) numberInSystemView.close();
        if (timeInSystemView   != null) timeInSystemView.close();
        Juego.iniciar();
    }
}