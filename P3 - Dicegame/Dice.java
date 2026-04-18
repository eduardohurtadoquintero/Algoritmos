public class Dice {
    private int sides;

    public Dice(int sides) {
        this.sides = sides;
    }

    public int roll() {
        return Math.max(1, (int) (Math.random() * sides) + 1);
    }

    public int getSides() { return sides; }
}