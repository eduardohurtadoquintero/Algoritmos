public class ColaCircular<T> {
    private T[] cola;
    private int inicio;
    private int fin;
    private int max;
    private int size;

    public ColaCircular() {
        this.max = 10;
        this.cola = (T[]) new Object[max];
        this.inicio = 0;
        this.fin = -1;
        this.size = 0;
    }

    public ColaCircular(int max) {
        this.max = max;
        this.cola = (T[]) new Object[max];
        this.inicio = 0;
        this.fin = -1;
        this.size = 0;
    }

    // Insertar (enqueue)
    public void insertar(T dato) {
        if (estaLlena()) {
            System.out.println("Desbordamiento");
            return;
        }

        fin = (fin + 1) % max;
        cola[fin] = dato;
        size++;
    }

    // Eliminar (dequeue)
    public T eliminar() {
        if (estaVacia()) {
            System.out.println("Subdesbordamiento");
            return null;
        }

        T dato = cola[inicio];
        inicio = (inicio + 1) % max;
        size--;

        return dato;
    }

    // Ver el primero
    public T verInicio() {
        if (estaVacia()) {
            return null;
        }
        return cola[inicio];
    }

    public boolean estaVacia() {
        return size == 0;
    }

    public boolean estaLlena() {
        return size == max;
    }

    public int getSize() {
        return size;
    }
}