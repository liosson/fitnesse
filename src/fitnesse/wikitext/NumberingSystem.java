package fitnesse.wikitext;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Numbering system for test scenario.</p>
 * @author Leonid Konyaev
 */
public class NumberingSystem {
    private final static NumberingSystem instance = new NumberingSystem();

    private List<Integer> currentNumberList = new LinkedList<Integer>();
    private static final Integer START_NUMBER = 1;
    private static final Integer INCREMENT = 1;

    private NumberingSystem() {
    }

    public static NumberingSystem getInstance() {
        return instance;
    }

    public void addNumberAtPosition(final int pos) {
        if (pos > currentNumberList.size()) {
            currentNumberList.add(START_NUMBER);
        } else {
            currentNumberList.set(pos - 1, currentNumberList.get(pos - 1) + INCREMENT);
            currentNumberList = currentNumberList.subList(0, pos);
        }
    }

    public void reset(final List<Integer> newNumberList) {
        currentNumberList = newNumberList;
    }

    @Override
    public String toString() {
        return joinList(currentNumberList, ".");
    }

    private String joinList(final Collection<Integer> collection, final String delimiter) {
        final StringBuilder resBuilder = new StringBuilder();
        for (final Integer i : collection) {
            resBuilder.append(i).append(delimiter);
        }
        if (resBuilder.length() > 0) {
            resBuilder.deleteCharAt(resBuilder.length() - 1);
        }
        return resBuilder.toString();
    }
}
