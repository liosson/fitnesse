package fitnesse.wikitext.parser;

import util.Maybe;

/**
 * @author Leonid Konyaev &lt;LKonyaev@luxoft.com&gt;
 * @version 1.0 10.07.2011
 */
public class ValueFromXls extends SymbolType implements Rule, Translation {

    /**
     * default constructor
     */
    public ValueFromXls() {
        super("ValueFromXls");
        wikiMatcher(new Matcher().string("!value_from_xls"));
        wikiRule(this);
        htmlTranslation(this);
    }

    /**
     * @see fitnesse.wikitext.parser.Rule#parse(Symbol, Parser)
     */
    public Maybe<Symbol> parse(final Symbol current, final Parser parser) {
        if (!parser.isMoveNext(SymbolType.Whitespace)) {
            return Symbol.nothing;
        }

        final SymbolType close = parser.moveNext(1).closeType();
        if(close == SymbolType.Empty) {
            return Symbol.nothing;
        }
        final Maybe<String> parsedVal = parser.parseToAsString(close);
        if (parsedVal.isNothing()) {
            return Symbol.nothing;
        }

        String xlsValue;
        try {
            xlsValue = XlsUtils.getValueFromXls(XlsUtils.replaceDefinedVariables(parsedVal.getValue(), parser.getPage()));
        } catch (Exception e) {
            xlsValue = null;
        }

        if(xlsValue == null || xlsValue.isEmpty()) {
            return Symbol.nothing;
        }
        return new Maybe<Symbol>(current.add(xlsValue));
    }

    /**
     * @see fitnesse.wikitext.parser.Translation#toTarget(Translator, Symbol)
     */
    public String toTarget(final Translator translator, final Symbol symbol) {
        return translator.translate(symbol.childAt(0));
    }
}
