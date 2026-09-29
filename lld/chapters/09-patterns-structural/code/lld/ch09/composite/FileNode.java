package lld.ch09.composite;

/** LEAF: no children, answers directly. */
record FileNode(String name, long size) implements FileSystemNode {
    @Override
    public void print(String indent) {
        System.out.println(indent + "- " + name + " (" + size + " B)");
    }
}
