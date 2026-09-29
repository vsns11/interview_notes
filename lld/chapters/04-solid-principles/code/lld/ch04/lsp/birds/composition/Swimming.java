package lld.ch04.lsp.birds.composition;

public class Swimming implements MovementStyle {
    @Override
    public void move(String birdName) {
        System.out.println(birdName + " is swimming through the water");
    }
}
