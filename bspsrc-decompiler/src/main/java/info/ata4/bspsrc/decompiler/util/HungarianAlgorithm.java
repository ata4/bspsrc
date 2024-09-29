package info.ata4.bspsrc.decompiler.util;

import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.IntStream;

public class HungarianAlgorithm {

    @FunctionalInterface
    public interface WeightsAccessor {
        double weight(int j, int w);
    }

    public record Result(
            int[] jobToWorker,
            double sum
    ) {}


    /**
     * Compute a maximum weight matching between two disjoints sets of vertices using the
     * <a href="https://en.wikipedia.org/wiki/Hungarian_algorithm">Kuhn-Munkres algorithm</a>
     * (also known as Hungarian algorithm).
     *
     * <p>Based on <a href="https://docs.rs/pathfinding/4.11.0/src/pathfinding/kuhn_munkres.rs.html#117-228">
     *     https://docs.rs/pathfinding/4.11.0/src/pathfinding/kuhn_munkres.rs.html#117-228</a>
     *
     * @param accessor accessor for the weights between the jobs and workers sets
     * @param jobs the size of the jobs set
     * @param workers the size of the workers set
     * @return the assignments of given as an array of indices from each row to a given column.
     */
    public static Result hungarian(WeightsAccessor accessor, int jobs, int workers) {
        var res = hungarianImpl(
                (j, w) -> {
                    if (w >= workers)
                        return 0.0;
                    else
                        return accessor.weight(j, w);
                },
                jobs,
                Math.max(workers, jobs)
        );
        for (int i = 0; i < res.jobToWorker().length; i++) {
            if (res.jobToWorker()[i] >= workers)
                res.jobToWorker()[i] = -1;
        }
        return res;
    }

    private static Result hungarianImpl(WeightsAccessor weightsAccessor, int jobs, int workers) {
        int nj = jobs;
        int nw = workers;

        if (nj > nw)
            throw new IllegalArgumentException();

        var jw = new int[nj];
        Arrays.fill(jw, -1);
        var wj = new int[nw];
        Arrays.fill(wj, -1);

        var lj = IntStream.range(0, nj)
                .mapToDouble(j -> IntStream.range(0, nw).mapToDouble(w -> weightsAccessor.weight(j, w)).max().orElseThrow())
                .toArray();
        var lw = new double[nw];

        var s = new BitSet(nj);
        var alternating = new int[nw];
        var slack = new double[nw];
        var slackj = new int[nw];

        for (int root = 0; root < nj; root++) {
            Arrays.fill(alternating, -1);

            s.clear();
            s.set(root);

            for (int w = 0; w < nw; w++) {
                slack[w] = lj[root] + lw[w] - weightsAccessor.weight(root, w);
            }
            Arrays.fill(slackj, root);

            int wOut;
            while (true) {
                var delta = Double.POSITIVE_INFINITY;
                var j = 0;
                var w = 0;

                for (int ww = 0; ww < nw; ww++) {
                    if (alternating[ww] < 0 && slack[ww] < delta) {
                        delta = slack[ww];
                        j = slackj[ww];
                        w = ww;
                    }
                }

                if (delta > 0) {
                    for (int i = s.nextSetBit(0); i >= 0; i = s.nextSetBit(i + 1)) {
                        lj[i] -= delta;
                    }
                    for (int i = 0; i < nw; i++) {
                        if (alternating[i] >= 0) {
                            lw[i] += delta;
                        } else {
                            slack[i] -= delta;
                        }
                    }
                }

                alternating[w] = j;
                if (wj[w] < 0) {
                    wOut = w;
                    break;
                }

                j = wj[w];
                s.set(j);

                for (int i = 0; i < nw; i++) {
                    if (alternating[i] < 0) {
                        var alternate_slack = lj[j] + lw[i] - weightsAccessor.weight(j, i);
                        if (slack[i] > alternate_slack) {
                            slack[i] = alternate_slack;
                            slackj[i] = j;
                        }
                    }
                }
            }

            while (wOut >= 0) {
                var j = alternating[wOut];
                var prec = jw[j];
                wj[wOut] = j;
                jw[j] = wOut;
                wOut = prec;
            }
        }

        return new Result(jw, Arrays.stream(lj).sum() + Arrays.stream(lw).sum());
    }

    public static void main(String[] args) {
        // Example usage
        double[][] costs = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        var result = hungarian((j, w) -> costs[j][w], costs.length, costs[0].length);
        System.out.println(Arrays.toString(result.jobToWorker));
    }
}
