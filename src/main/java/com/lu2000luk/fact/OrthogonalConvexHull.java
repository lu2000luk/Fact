package com.lu2000luk.fact;

import com.mojang.logging.LogUtils;

import java.util.*;

public class OrthogonalConvexHull {
    public static class Point implements Comparable<Point> {
        double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Point other) {
            if (this.x != other.x) {
                return Double.compare(this.x, other.x);
            }
            return Double.compare(this.y, other.y);
        }
    }

    public static class Result {
        double[] xCoordinates;
        double[] yCoordinates;

        public Result(double[] x, double[] y) {
            this.xCoordinates = x;
            this.yCoordinates = y;
        }
    }

    public static Result findOrthogonalConvexHull(double[] x, double[] y) {
        if (x == null || y == null || x.length != y.length || x.length == 0) {
            LogUtils.getLogger().error("Fact [Math Engine] >> Invalid input arrays from findOrthogonalConvexHull!");
            return new Result(new double[0], new double[0]);
        }

        // Group points by x-coordinate and record the minimum and maximum y for each.
        TreeMap<Double, double[]> xToMinMax = new TreeMap<>();
        for (int i = 0; i < x.length; i++) {
            double xi = x[i], yi = y[i];
            if (!xToMinMax.containsKey(xi)) {
                xToMinMax.put(xi, new double[]{yi, yi});
            } else {
                double[] minMax = xToMinMax.get(xi);
                if (yi < minMax[0]) {
                    minMax[0] = yi;
                }
                if (yi > minMax[1]) {
                    minMax[1] = yi;
                }
            }
        }

        // Build the top (upper) chain: for each distinct x, use the maximum y.
        List<Point> topChain = new ArrayList<>();
        for (Map.Entry<Double, double[]> entry : xToMinMax.entrySet()) {
            double xi = entry.getKey();
            double maxY = entry.getValue()[1];
            topChain.add(new Point(xi, maxY));
        }

        // Build the bottom (lower) chain: for each distinct x in reverse order, use the minimum y.
        List<Point> bottomChain = new ArrayList<>();
        for (Double xi : xToMinMax.descendingKeySet()) {
            double minY = xToMinMax.get(xi)[0];
            bottomChain.add(new Point(xi, minY));
        }

        // Convert each chain to an "orthogonal" chain: insert extra vertices when y changes so that
        // the segments become horizontal then vertical (avoiding a direct diagonal connection).
        List<Point> topOrth = convertToOrthogonal(topChain);
        List<Point> bottomOrth = convertToOrthogonal(bottomChain);

        // Combine the two chains to form the complete orthogonal convex hull.
        // (The ordering gives a closed polygon with only horizontal/vertical edges.)
        List<Point> hull = new ArrayList<>();
        hull.addAll(topOrth);
        hull.addAll(bottomOrth);

        // Convert the list of hull vertices back to separate arrays.
        double[] hullX = new double[hull.size()];
        double[] hullY = new double[hull.size()];
        for (int i = 0; i < hull.size(); i++) {
            hullX[i] = hull.get(i).x;
            hullY[i] = hull.get(i).y;
        }
        return new Result(hullX, hullY);
    }

    // Helper method: given a chain of points (assumed sorted in one direction),
    // insert intermediate vertices so that the segments between points become horizontal and vertical.
    private static List<Point> convertToOrthogonal(List<Point> chain) {
        List<Point> orthChain = new ArrayList<>();
        if (chain.isEmpty()) {
            return orthChain;
        }
        orthChain.add(chain.get(0));
        for (int i = 1; i < chain.size(); i++) {
            Point prev = orthChain.get(orthChain.size() - 1);
            Point curr = chain.get(i);
            if (prev.y != curr.y) {
                // Insert an intermediate point: move horizontally to curr.x then vertically.
                orthChain.add(new Point(curr.x, prev.y));
            }
            orthChain.add(curr);
        }
        return orthChain;
    }
}
