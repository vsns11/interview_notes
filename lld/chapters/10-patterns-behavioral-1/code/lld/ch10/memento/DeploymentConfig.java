package lld.ch10.memento;

import java.util.Map;
import java.util.TreeMap;

/** The ORIGINATOR: owns the state, and is the only class that can read a snapshot. */
public final class DeploymentConfig {

    /**
     * The NARROW interface the caretaker sees: a label, nothing else.
     * (GoF: caretaker gets a narrow interface, originator a wide one.)
     */
    public interface Snapshot {
        String label();
    }

    /** The WIDE interface: private, so only DeploymentConfig can open it. Immutable record. */
    private record State(String label, int replicas, String image, Map<String, String> env)
            implements Snapshot {
        State {
            env = Map.copyOf(env);   // DEEP enough: the snapshot owns its own copy, not the live map
        }
    }

    private int replicas;
    private String image;
    private final Map<String, String> env = new TreeMap<>();

    public DeploymentConfig(int replicas, String image, Map<String, String> env) {
        apply(replicas, image, env);
    }

    public void apply(int replicas, String image, Map<String, String> env) {
        this.replicas = replicas;
        this.image = image;
        this.env.clear();
        this.env.putAll(env);
    }

    public void setEnv(String key, String value) { env.put(key, value); }

    public Snapshot save(String label) {
        return new State(label, replicas, image, env);
    }

    /** One call restores everything: all-or-nothing. */
    public void restore(Snapshot snapshot) {
        if (!(snapshot instanceof State s)) {
            throw new IllegalArgumentException("not a snapshot produced by DeploymentConfig");
        }
        apply(s.replicas(), s.image(), s.env());
    }

    @Override
    public String toString() {
        return "replicas=" + replicas + " image=" + image + " env=" + env;
    }
}
