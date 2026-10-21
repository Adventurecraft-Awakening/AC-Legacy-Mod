package dev.adventurecraft.awakening.entity.ai.pathing;

import net.minecraft.world.level.pathfinder.Node;

import java.util.ArrayList;

/** AdventureCraft's path smoothing, with live clearance queries supplied by the navigator. */
public final class PathSimplifier {

    private PathSimplifier() {
    }

    public static Node[] simplify(Node[] nodes, int position, Node clearSize, Clearance clearance) {
        int first = Math.min(nodes.length, Math.max(0, position));
        var result = new ArrayList<Node>(nodes.length - first);
        Node anchor = null;
        Node firstCandidate = null;
        Node previous = null;
        boolean appendRemaining = false;

        for (int index = first; index < nodes.length; ++index) {
            Node node = nodes[index];
            if (appendRemaining) {
                result.add(node);
            }
            else if (anchor == null) {
                anchor = node;
                result.add(node);
            }
            else if (firstCandidate == null) {
                if (anchor.y != node.y) {
                    anchor = node;
                    result.add(node);
                    appendRemaining = true;
                }
                else {
                    firstCandidate = node;
                    previous = node;
                }
            }
            else if (previous.y != node.y) {
                result.add(previous);
                result.add(node);
                anchor = node;
                firstCandidate = null;
                previous = null;
                appendRemaining = true;
            }
            else {
                int deltaX = node.x - anchor.x;
                int deltaZ = node.z - anchor.z;
                float minorOffset = 0.0F;
                if (Math.abs(deltaX) < Math.abs(deltaZ)) {
                    float minorStep = (float) deltaX / (float) Math.abs(deltaZ);
                    int direction = deltaZ < 0 ? -1 : 1;
                    for (int step = 1; step < Math.abs(deltaZ); ++step) {
                        int x = anchor.x + (int) minorOffset;
                        int z = anchor.z + step * direction;
                        if (isSupported(clearance, x, anchor.y, z, clearSize) &&
                            isSupported(clearance, x + 1, anchor.y, z, clearSize) &&
                            isSupported(clearance, x - 1, anchor.y, z, clearSize)) {
                            minorOffset += minorStep;
                        }
                        else {
                            result.add(firstCandidate);
                            result.add(node);
                            appendRemaining = true;
                        }
                    }
                }
                else {
                    float minorStep = (float) deltaZ / (float) Math.abs(deltaX);
                    int direction = deltaX < 0 ? -1 : 1;
                    for (int step = 1; step < Math.abs(deltaX); ++step) {
                        int x = anchor.x + step * direction;
                        int z = anchor.z + (int) minorOffset;
                        if (isSupported(clearance, x, anchor.y, z, clearSize) &&
                            isSupported(clearance, x, anchor.y, z + 1, clearSize) &&
                            isSupported(clearance, x, anchor.y, z - 1, clearSize)) {
                            minorOffset += minorStep;
                        }
                        else {
                            anchor = firstCandidate;
                            result.add(firstCandidate);
                            result.add(node);
                            appendRemaining = true;
                        }
                    }
                }
                // Legacy smoothing keeps scanning this segment after a failure. Its live query
                // order and duplicate output nodes matter to existing paths and custom blocks.
                previous = node;
            }
        }

        if (!appendRemaining) {
            if (previous != null) {
                result.add(previous);
            }
            else if (firstCandidate != null) {
                result.add(firstCandidate);
            }
        }
        return result.toArray(new Node[result.size()]);
    }

    private static boolean isSupported(Clearance clearance, int x, int y, int z, Node clearSize) {
        return clearance.classify(x, y, z, clearSize) == 1 &&
            clearance.classify(x, y - 1, z, clearSize) != 1;
    }

    @FunctionalInterface
    public interface Clearance {

        int classify(int x, int y, int z, Node clearSize);
    }
}
