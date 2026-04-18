import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import java.io.File;
 
/**
 * Panel de una estacion: avatar, fichas en grid, dado y movimiento.
 */
public class EstacionPane extends VBox {
 
    private static final int W         = 120;
    private static final int H         = 260;
    private static final int AVATAR_S  = 80;
    private static final int FICHA_S   = 14;
    private static final int FICHA_GAP = 3;
 
    private static final Color XP_TOP        = Color.rgb(166, 202, 240);
    private static final Color XP_BOT        = Color.rgb(212, 228, 248);
    private static final Color BORDER_NORMAL = Color.rgb( 10,  36, 106);
    private static final Color BORDER_ROLL   = Color.rgb(220, 160,   0);
    private static final Color BORDER_MOVE   = Color.rgb( 50, 170,  80);
    private static final Color FICHA_INIC    = Color.rgb( 60,  90, 160, 0.82);
    private static final Color FICHA_INIC_B  = Color.rgb( 20,  50, 120, 0.92);
    private static final Color FICHA_NUEVA   = Color.rgb( 30, 180, 140, 0.90);
    private static final Color FICHA_NUEVA_B = Color.rgb(  0, 120,  90, 0.90);
 
    private final int     numero;
    private final boolean esFuente;
    // true = flujo hacia la derecha (E1-E5), false = flujo hacia la izquierda (E6-E10)
    private final boolean flujoDerecha;
 
    private final Canvas   cardCanvas;
    private final DadoPane dadoPane;
    private final Label    lblMovimiento;
    private Ficha.Tipo[]   tiposFichas = new Ficha.Tipo[0];
    private Image          avatar;
 
    public EstacionPane(int numero, int sides, int cupoMax, boolean esFuente) {
        this.numero      = numero;
        this.esFuente    = esFuente;
        // E1-E5 fluyen hacia la derecha, E6-E10 hacia la izquierda (serpentina de 5 columnas)
        this.flujoDerecha = ((numero - 1) / 5) % 2 == 0;
 
        setAlignment(Pos.TOP_CENTER);
        setSpacing(0);
        setPrefSize(W, H);
        setMaxSize(W, H);
        setMinSize(W, H);
 
        cargarAvatar(numero);
 
        cardCanvas = new Canvas(W, H);
        dibujarCard();
 
        dadoPane = new DadoPane(sides);
        dadoPane.setTranslateY(-8);
 
        lblMovimiento = new Label();
        lblMovimiento.setFont(Font.font("Tahoma", FontWeight.BOLD, 10));
        lblMovimiento.setTextFill(Color.rgb(0, 110, 0));
 
        StackPane stack = new StackPane(cardCanvas);
        stack.setPrefSize(W, H);
        stack.setMaxSize(W, H);
 
        VBox overlay = new VBox(2, dadoPane, lblMovimiento);
        overlay.setAlignment(Pos.BOTTOM_CENTER);
        overlay.setPadding(new Insets(0, 0, 10, 0));
        stack.getChildren().add(overlay);
 
        getChildren().add(stack);
        actualizarBorde(BORDER_NORMAL, 1);
    }
 
    private void cargarAvatar(int n) {
        try {
            File f = new File("images/estacion" + n + ".png");
            if (f.exists()) avatar = new Image(f.toURI().toString(), AVATAR_S, AVATAR_S, true, true);
        } catch (Exception ignored) {}
    }
 
    private void dibujarCard() {
        GraphicsContext gc = cardCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, W, H);
 
        // Fondo gradiente
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, XP_TOP), new Stop(1, XP_BOT));
        gc.setFill(bg);
        gc.fillRoundRect(0, 0, W - 1, H - 1, 14, 14);
 
        // Banda de encabezado con numero de estacion
        gc.setFill(Color.rgb(10, 36, 106, 0.80));
        gc.fillRoundRect(0, 0, W - 1, 26, 14, 14);
        gc.fillRect(0, 13, W - 1, 13);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 11));
        String label = esFuente ? "E" + numero + "  [Entrada]" : "Estacion " + numero;
        // Recorta si es largo
        double labelX = Math.max(6, (W - label.length() * 6.5) / 2);
        gc.fillText(label, labelX, 18);
 
        // Avatar
        int avatarX = (W - AVATAR_S) / 2;
        int avatarY = 30;
        if (avatar != null) {
            gc.drawImage(avatar, avatarX, avatarY, AVATAR_S, AVATAR_S);
        } else {
            // Circulo placeholder
            gc.setFill(Color.rgb(100, 140, 200, 0.7));
            gc.fillOval(avatarX, avatarY, AVATAR_S, AVATAR_S);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 22));
            gc.fillText("E" + numero, avatarX + (numero < 10 ? 24 : 18), avatarY + AVATAR_S / 2 + 8);
        }
 
        // Fichas
        if (esFuente) {
            gc.setFill(Color.rgb(10, 36, 106));
            gc.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
            gc.fillText("inf", W / 2 - 18, H / 2 + 18);
        } else if (tiposFichas.length > 0) {
            int avatarBottom = avatarY + AVATAR_S + 6;
            int dadoTop      = H - 82;
            int zonaH        = dadoTop - avatarBottom;
            int cols         = 4;
            int n            = tiposFichas.length;
            int filas        = (int) Math.ceil((double) n / cols);
            int totalH       = filas * (FICHA_S + FICHA_GAP) - FICHA_GAP;
            int totalW       = Math.min(n, cols) * (FICHA_S + FICHA_GAP) - FICHA_GAP;
            int startX       = (W - totalW) / 2;
            int startY       = avatarBottom + Math.max(0, (zonaH - totalH) / 2);
 
            for (int i = 0; i < n; i++) {
                boolean inicial = tiposFichas[i] == Ficha.Tipo.INICIAL;
                Color fill  = inicial ? FICHA_INIC   : FICHA_NUEVA;
                Color borde = inicial ? FICHA_INIC_B : FICHA_NUEVA_B;
 
                int col = i % cols;
                int row = i / cols;
                int fx  = startX + col * (FICHA_S + FICHA_GAP);
                int fy  = startY + row * (FICHA_S + FICHA_GAP);
 
                gc.setFill(fill);
                gc.fillRoundRect(fx, fy, FICHA_S, FICHA_S, 4, 4);
                gc.setStroke(borde);
                gc.setLineWidth(1);
                gc.strokeRoundRect(fx, fy, FICHA_S, FICHA_S, 4, 4);
                gc.setFill(Color.rgb(255, 255, 255, 0.7));
                gc.fillOval(fx + (FICHA_S - 4) / 2.0, fy + (FICHA_S - 4) / 2.0, 4, 4);
            }
        } else if (!esFuente) {
            // Vacía
            gc.setFill(Color.rgb(10, 36, 106, 0.25));
            gc.setFont(Font.font("Tahoma", FontPosture.ITALIC, 10));
            gc.fillText("(vacia)", W / 2 - 18, H / 2 + 10);
        }
    }
 
    public void mostrarRoll(int roll) {
        dadoPane.setValor(roll);
        lblMovimiento.setText("");
        actualizarBorde(BORDER_ROLL, 2.5);
        dibujarCard();
    }
 
    public void mostrarMovimiento(int mov) {
        String flecha = flujoDerecha ? "-> " : "<- ";
        lblMovimiento.setText(mov > 0 ? flecha + mov : "");
        actualizarBorde(mov > 0 ? BORDER_MOVE : BORDER_NORMAL, mov > 0 ? 2.5 : 1);
        dibujarCard();
    }
 
    public void actualizar(Ficha.Tipo[] tipos, int roll, int mov) {
        this.tiposFichas = tipos;
        dadoPane.setValor(roll);
        String flecha = flujoDerecha ? "-> " : "<- ";
        lblMovimiento.setText(mov > 0 ? flecha + mov : "");
        actualizarBorde(BORDER_NORMAL, 1);
        dibujarCard();
    }
 
    public void setTiposFichas(Ficha.Tipo[] tipos) {
        this.tiposFichas = tipos;
        dibujarCard();
    }
 
    private void actualizarBorde(Color c, double grosor) {
        setStyle(String.format(
            "-fx-border-color: rgb(%d,%d,%d); -fx-border-width: %.1f; " +
            "-fx-border-radius: 12; -fx-background-radius: 12;",
            (int)(c.getRed() * 255), (int)(c.getGreen() * 255), (int)(c.getBlue() * 255), grosor));
    }
}