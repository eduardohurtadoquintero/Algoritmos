import javax.swing.*;
import java.awt.*;

public class FichaView extends JPanel {

    private static final int SIZE = 16; // Reducido de 32 a 16
    private static final Color COLOR_FICHA = new Color(196, 30, 30);
    private static final Color COLOR_BORDE = new Color(120, 0, 0);
    private static final Color COLOR_PIP   = Color.WHITE;

    public FichaView() {
        setPreferredSize(new Dimension(SIZE, SIZE));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Cuadrado rojo redondeado (estilo de la imagen)
        g2.setColor(COLOR_FICHA);
        g2.fillRoundRect(0, 0, SIZE - 1, SIZE - 1, 4, 4);
        g2.setColor(COLOR_BORDE);
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(0, 0, SIZE - 1, SIZE - 1, 4, 4);

        // Pip central
        int pip = 4;
        g2.setColor(COLOR_PIP);
        g2.fillOval((SIZE - pip) / 2, (SIZE - pip) / 2, pip, pip);
    }
}