import javafx.application.Application;
import javafx.stage.Stage;

public class Juego extends Application {

    private static Stage mainStage;

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        iniciar();
    }

    public static void iniciar() {
        int numEtapas = 10;
        Etapa[] etapas = new Etapa[numEtapas];

        etapas[0] = new Etapa(6, 6);
        etapas[0].setEntradaInfinita(true);

        for (int i = 1; i < numEtapas; i++) {
            etapas[i] = new Etapa(6, 6);
        }
        for (int i = 0; i < numEtapas - 1; i++) {
            etapas[i].setSiguienteEtapa(etapas[i + 1]);
        }

        int idFicha = 1;
        for (int i = 1; i < numEtapas; i++) {
            for (int j = 0; j < 4; j++) {
                etapas[i].recibirFicha(new Ficha(idFicha++, Ficha.Tipo.INICIAL));
            }
        }

        JuegoView view = new JuegoView(etapas, mainStage);
        view.mostrar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}