import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.*;
import java.util.List;

public class NumberInSystemView extends Stage {

    private static final Color BAR  = Color.rgb(58,110,165);
    private static final Color LINE = Color.rgb(196,30,30);
    private static final Color BG1  = Color.rgb(210,224,248);
    private static final Color BG2  = Color.rgb(230,238,252);
    private static final Color GRID = Color.rgb(170,195,230);
    private static final int PL=55,PR=20,PT=40,PB=50;

    private final ActivityData data;
    private Canvas chartCanvas;
    private Label  lblActual;

    public NumberInSystemView(ActivityData data) {
        this.data = data;
        setTitle("Number in System – Fichas activas por turno");
        construirUI();
        setWidth(780); setHeight(480);
    }

    private void construirUI() {
        BorderPane root = new BorderPane();
        HBox title = new HBox();
        title.setStyle("-fx-background-color: linear-gradient(to right,#0a246a,#3a6ea5);");
        title.setPadding(new Insets(6,12,6,12));
        Label lt = new Label("🔵  Number in System – Fichas activas dentro del juego por turno");
        lt.setFont(Font.font("Tahoma",FontWeight.BOLD,13)); lt.setTextFill(Color.WHITE);
        title.getChildren().add(lt);
        root.setTop(title);

        chartCanvas = new Canvas(700,380);
        StackPane wrap = new StackPane(chartCanvas);
        chartCanvas.widthProperty().bind(wrap.widthProperty());
        chartCanvas.heightProperty().bind(wrap.heightProperty());
        chartCanvas.widthProperty().addListener(e -> dibujar());
        chartCanvas.heightProperty().addListener(e -> dibujar());
        root.setCenter(wrap);

        HBox bottom = new HBox(12);
        bottom.setAlignment(Pos.CENTER); bottom.setPadding(new Insets(8));
        bottom.setStyle("-fx-background-color:#bed2f0; -fx-border-color:#0a246a; -fx-border-width:1 0 0 0;");
        Label lt2 = new Label("Fichas en sistema ahora:");
        lt2.setFont(Font.font("Tahoma",FontWeight.BOLD,13)); lt2.setTextFill(Color.rgb(10,36,106));
        lblActual = new Label("—");
        lblActual.setFont(Font.font("Tahoma",FontWeight.BOLD,18)); lblActual.setTextFill(Color.rgb(10,36,106));
        bottom.getChildren().addAll(lt2, lblActual);
        root.setBottom(bottom);

        setScene(new Scene(root));
    }

    public void actualizar() {
        List<Integer> h = data.getNumberInSystem();
        if (!h.isEmpty()) lblActual.setText(String.valueOf(h.get(h.size()-1)));
        dibujar(); show();
    }

    private void dibujar() {
        GraphicsContext gc = chartCanvas.getGraphicsContext2D();
        double W = chartCanvas.getWidth(), H = chartCanvas.getHeight();
        double cW = W-PL-PR, cH = H-PT-PB;
        gc.clearRect(0,0,W,H);

        LinearGradient bg = new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE,
            new Stop(0,BG1),new Stop(1,BG2));
        gc.setFill(bg); gc.fillRect(0,0,W,H);

        List<Integer> h = data.getNumberInSystem();
        if (h.isEmpty()) {
            gc.setFill(Color.rgb(10,36,106)); gc.setFont(Font.font("Tahoma",FontPosture.ITALIC,14));
            gc.fillText("Aún no hay turnos registrados", W/2-120, H/2); return;
        }

        int n = h.size();
        int maxVal = Math.max(h.stream().mapToInt(Integer::intValue).max().getAsInt(), 1);
        int gl = Math.min(maxVal,8);

        gc.setFont(Font.font("Tahoma",10));
        for (int i=0;i<=gl;i++) {
            double y = PT+cH-(i*cH/gl);
            gc.setStroke(GRID); gc.setLineWidth(i==0?1.5:.7);
            if (i>0) gc.setLineDashes(4,4); else gc.setLineDashes(0);
            gc.strokeLine(PL,y,PL+cW,y); gc.setLineDashes(0);
            gc.setFill(Color.rgb(10,36,80)); gc.fillText(String.valueOf((int)(i*maxVal/gl)), 4, y+4);
        }

        double slotW = cW/n, barW = Math.max(4,slotW-4);
        double[] lx = new double[n], ly = new double[n];
        for (int i=0;i<n;i++) {
            int v = h.get(i);
            double bH = Math.max((double)v/maxVal*cH, 2);
            double bX = PL+i*slotW+(slotW-barW)/2, bY = PT+cH-bH;
            gc.setFill(BAR.brighter()); gc.fillRoundRect(bX,bY,barW,bH,3,3);
            gc.setStroke(BAR.darker()); gc.setLineWidth(1); gc.strokeRoundRect(bX,bY,barW,bH,3,3);
            gc.setFill(Color.rgb(10,36,80)); gc.setFont(Font.font("Tahoma",FontWeight.BOLD,9));
            gc.fillText(String.valueOf(v), bX+1, bY-2);
            gc.setFont(Font.font("Tahoma",9));
            gc.fillText("T"+(i+1), PL+i*slotW+slotW/2-8, PT+cH+14);
            lx[i] = PL+i*slotW+slotW/2; ly[i] = bY;
        }

        gc.setStroke(LINE); gc.setLineWidth(2); gc.setLineDashes(0);
        for (int i=0;i<n-1;i++) { gc.strokeLine(lx[i],ly[i],lx[i+1],ly[i+1]); gc.fillOval(lx[i]-4,ly[i]-4,8,8); }
        if (n>0) { gc.setFill(LINE); gc.fillOval(lx[n-1]-4,ly[n-1]-4,8,8); }

        gc.setStroke(Color.rgb(10,36,80)); gc.setLineWidth(1.8);
        gc.strokeLine(PL,PT,PL,PT+cH); gc.strokeLine(PL,PT+cH,PL+cW,PT+cH);

        int actual = h.get(h.size()-1);
        gc.setFill(Color.rgb(10,36,80)); gc.setFont(Font.font("Tahoma",FontWeight.BOLD,13));
        gc.fillText("Fichas activas en el sistema por turno  (Último: "+actual+")", PL+10, 26);
    }
}