package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;

/**
 * @author Leonid Konyaev
 * @version 1.0 05.10.2011
 */
public class DefineFromXls extends SymbolType implements Rule, Translation {

    public DefineFromXls() {
        super("DefineFromXls");
        wikiMatcher(new Matcher().string("!define_from_xls"));
        wikiRule(this);
        htmlTranslation(this);
    }

    /**
     * @see fitnesse.wikitext.parser.Rule#parse(Symbol, Parser)
     */
    public Maybe<Symbol> parse(final Symbol current, final Parser parser) {
        if (!parser.isMoveNext(SymbolType.Whitespace)) return Symbol.nothing;

        final Maybe<String> name = parser.parseToAsString(SymbolType.Whitespace);
        if (name.isNothing()) {
            return Symbol.nothing;
        }
        final String variableName = name.getValue();
        if (!ScanString.isVariableName(variableName)) {
            return Symbol.nothing;
        }

        final Symbol next = parser.moveNext(1);
        final SymbolType close = next.closeType();
        if (close == SymbolType.Empty) {
            return Symbol.nothing;
        }

        Maybe<String> valueString = parser.parseToAsString(close);
        if (valueString.isNothing()) {
            return Symbol.nothing;
        }

        String xlsValue;
        try {
            xlsValue = XlsUtils.getValueFromXls(XlsUtils.replaceDefinedVariables(valueString.getValue(), parser.getPage()));
        } catch (Exception e) {
            xlsValue = null;
        }

        if(xlsValue == null || xlsValue.isEmpty()) {
            return Symbol.nothing;
        }
        parser.getPage().putVariable(variableName, xlsValue);

        return new Maybe<Symbol>(current.add(variableName).add(xlsValue));
    }

    /**
     * @see fitnesse.wikitext.parser.Translation#toTarget(Translator, Symbol)
     */
    public String toTarget(final Translator translator, final Symbol symbol) {
        HtmlTag result = new HtmlTag("span", "variable defined from xls: "
                + translator.translate(symbol.childAt(0))
                + "="
                + translator.translate(symbol.childAt(1)));
        result.addAttribute("class", "meta");
        return result.html();
    }
}
