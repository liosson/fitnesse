package fitnesse.wikitext.parser;

import fitnesse.wikitext.NumberingSystem;
import util.Maybe;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>Numbering system for FitNesse scenarios.</p>
 * Usage:
 * <p>!n - reset numbering</p>
 * <p>!n * - increment first digit</p>
 * <p>!n ** - increment second digit</p>
 * <p>!n *** - increment thrid digit</p>
 * <p>etc.</p>
 * @author Leonid Konyaev
 */
public class Numbering extends SymbolType implements Rule, Translation {
    private static final String ARGUMENT = "number_template";
    private static final String NUMBER_TEMPLATE_PATTERN = "\\*+";
    private static final String START_NUMBER_PATTERN = "\\d((\\.\\d)*)";

    public Numbering() {
        super("Numbering");
        wikiMatcher(new Matcher().string("!n"));
        wikiRule(this);
        htmlTranslation(this);
    }

    @Override
    public Maybe<Symbol> parse(final Symbol current, final Parser parser) {
        List<Symbol> lookAhead = parser.peek(new SymbolType[] {SymbolType.Whitespace, SymbolType.Text});
        if (lookAhead.size() != 0 ) {
            String nextNumberTemplate = lookAhead.get(1).getContent();
            current.putProperty(ARGUMENT, nextNumberTemplate);
            parser.moveNext(2);
        }
        return new Maybe<Symbol>(current);
    }

    @Override
    public String toTarget(final Translator translator, final Symbol symbol) {
        String argument = symbol.getProperty(ARGUMENT);
        argument = argument == null ? "" : argument;
        String res = "";
        if (Pattern.compile(NUMBER_TEMPLATE_PATTERN).matcher(argument).matches()) {
            NumberingSystem.getInstance().addNumberAtPosition(argument.length());
            res = NumberingSystem.getInstance().toString();
        } else if (Pattern.compile(START_NUMBER_PATTERN).matcher(argument).matches()) {
            NumberingSystem.getInstance().reset(parseNumbersToList(argument, "\\."));
            res = NumberingSystem.getInstance().toString();
        } else {
            NumberingSystem.getInstance().reset(new LinkedList<Integer>());
        }
        return res;
    }

    private List<Integer> parseNumbersToList(final String str, final String delimiter) {
        final List<Integer> res = new LinkedList<Integer>();
        final String[] parts = str.split(delimiter);
        for (final String part : parts) {
            res.add(Integer.parseInt(part));
        }
        return res;
    }
}
