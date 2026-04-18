import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.*;
import java.util.*;

public class ThroughputView extends Stage {

    private static final Color BAR  = Color.rgb(30,180,140);
    private static final Color LINE = Color.rgb(0,120,80,0.7);
    private static final Color BG1  = Color.rgb(212,240,228);
    private static final Color BG2  = Color.rgb(230,248,240);
    private static final Color GRID = Color.rgb(160,210,190);
    private static final int PL=55,PR=20,PT=40,PB=50;

    private final ActivityData data;
    private Canvas chartCanvas;

    public ThroughputView(ActivityData data) {
        this.data = data;
        setTitle("Throughput – Fichas completadas acumuladas");
        construirUI();
        setWidth(780); setHeight(480);
    }

    private void construirUI() {
        BorderPane root = new BorderPane();

        HBox title = new HBox();
        title.setStyle("-fx-background-color: linear-gradient(to right,#0a246a,#3a6ea5);");
        title.setPadding(new Insets(6,12,6,12));
        Label lt = new Label("📦  Throughput – Fichas completadas acumuladas por turno");
        lt.setFont(Font.font("Tahoma",FontWeight.BOLD,13));
        lt.setTextFill(Color.WHITE);
        title.getChildren().add(lt);
        root.setTop(title);

        chartCanvas = new Canvas(700, 380);
        StackPane wrap = new StackPane(chartCanvas);
        chartCanvas.widthProperty().bind(wrap.widthProperty());
        chartCanvas.heightProperty().bind(wrap.heightProperty());
        chartCanvas.widthProperty().addListener(e -> dibujar());
        chartCanvas.heightProperty().addListener(e -> dibujar());
        root.setCenter(wrap);

        setScene(new Scene(root));
    }

    public void actualizar() { dibujar(); show(); }

    private void dibujar() {
        GraphicsContext gc = chartCanvas.getGraphicsContext2D();
        double W = chartCanvas.getWidth(), H = chartCanvas.getHeight();
        double cW = W-PL-PR, cH = H-PT-PB;
        gc.clearRect(0,0,W,H);

        LinearGradient bg = new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE,
            new Stop(0,BG1),new Stop(1,BG2));
        gc.setFill(bg); gc.fillRect(0,0,W,H);

        List<Integer> raw = data.getThroughputPorTurno();
        if (raw == null || raw.isEmpty()) {
            gc.setFill(Color.rgb(0,100,60)); gc.setFont(Font.font("Tahoma",FontPosture.ITALIC,14));
            gc.fillText("Aún no hay turnos registrados", W/2-120, H/2); return;
        }

        List<Integer> acum = new ArrayList<>();
        int total = 0;
        for (int v : raw) { total += v; acum.add(total); }

        int n = acum.size();
        int maxVal = Math.max(acum.get(n-1), 1);
        int gl = Math.min(maxVal,8);

        gc.setFont(Font.font("Tahoma",10));
        for (int i=0;i<=gl;i++) {
            double y = PT+cH-(i*cH/gl);
            gc.setStroke(GRID); gc.setLineWidth(i==0?1.5:.7);
            if (i>0) gc.setLineDashes(4,4); else gc.setLineDashes(0);
            gc.strokeLine(PL,y,PL+cW,y); gc.setLineDashes(0);
            gc.setFill(Color.rgb(0,80,50));
            gc.fillText(String.valueOf((int)(i*maxVal/gl)), 4, y+4);
        }

        double slotW = cW/n;
        double barW = Math.max(4, slotW-4);
        double[] lx = new double[n], ly = new double[n];
        for (int i=0;i<n;i++) {
            int v = acum.get(i);
            double bH = (double)v/maxVal*cH;
            double bX = PL+i*slotW+(slotW-barW)/2;
            double bY = PT+cH-bH;
            gc.setFill(BAR.brighter()); gc.fillRoundRect(bX,bY,barW,bH,3,3);
            gc.setStroke(BAR.darker()); gc.setLineWidth(1); gc.strokeRoundRect(bX,bY,barW,bH,3,3);
            gc.setFill(Color.rgb(0,60,40)); gc.setFont(Font.font("Tahoma",FontWeight.BOLD,9));
            gc.fillText(String.valueOf(v), bX+1, bY-2);
            gc.setFill(Color.rgb(0,80,50)); gc.setFont(Font.font("Tahoma",9));
            gc.fillText("T"+(i+1), PL+i*slotW+slotW/2-8, PT+cH+14);
            lx[i] = PL+i*slotW+slotW/2; ly[i] = bY;
        }
        // Línea tendencia
        gc.setStroke(LINE); gc.setLineWidth(2); gc.setLineDashes(0);
        for (int i=0;i<n-1;i++) gc.strokeLine(lx[i],ly[i],lx[i+1],ly[i+1]);

        gc.setStroke(Color.rgb(0,80,50)); gc.setLineWidth(1.8);
        gc.strokeLine(PL,PT,PL,PT+cH); gc.strokeLine(PL,PT+cH,PL+cW,PT+cH);

        gc.setFill(Color.rgb(0,80,50)); gc.setFont(Font.font("Tahoma",FontWeight.BOLD,13));
        gc.fillText("Fichas completadas – Acumulado  (Total: "+total+")", PL+10, 26);
    }
}