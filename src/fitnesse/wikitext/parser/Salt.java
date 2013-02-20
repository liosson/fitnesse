package fitnesse.wikitext.parser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import util.Maybe;
import util.SystemTimeKeeper;

/**
 * @author Alexander Orlov &lt;AOrlov@luxoft.com&gt;
 * @version 1.0 19.08.2011
 *
 */
public class Salt extends SymbolType implements Rule, Translation {

  private GregorianCalendar calendar = new GregorianCalendar();
  private String lastFformat = null;

  private static final String Format = "Format";
  private static final String Increment = "Increment";
  private static final String SaltSize = "SaltSize";

  public Salt() {
    super("Salt");
    wikiMatcher(new Matcher().string("!salt"));
    wikiRule(this);
    htmlTranslation(this);
  }

  public Maybe<Symbol> parse(Symbol current, Parser parser) {
    // search for date format
    List<Symbol> lookAhead = parser
        .peek(new SymbolType[]{SymbolType.Whitespace, SymbolType.Text});
    if (lookAhead.size() != 0) {
      String option = lookAhead.get(1).getContent();
      if (isDateFormatOption(option)) {
        current.putProperty(Salt.Format, option);
        parser.moveNext(2);
      }
    } else {
      lookAhead = parser
          .peek(new SymbolType[] { SymbolType.Whitespace, SymbolType.OpenParenthesis });
      if (lookAhead.size() != 0) {
        parser.moveNext(2);
        Maybe<String> format = parser.parseToAsString(SymbolType.CloseParenthesis);
        if (format.isNothing())
          return Symbol.nothing;
        current.putProperty(Format, format.getValue());
      }
    }

    // search for salt size option
    lookAhead = parser.peek(new SymbolType[]{SymbolType.Whitespace, SymbolType.OpenParenthesis});
    if (lookAhead.size() != 0) {
      parser.moveNext(2);
      final Maybe<String> saltSize = parser.parseToAsString(SymbolType.CloseParenthesis);
      if (saltSize.isNothing()) {
        return Symbol.nothing;
      }
      current.putProperty(SaltSize, saltSize.getValue());
    }

    // search for increment option
    lookAhead = parser.peek(new SymbolType[] { SymbolType.Whitespace, SymbolType.Text });
    if (lookAhead.size() != 0) {
      String increment = lookAhead.get(1).getContent();
      if ((increment.startsWith("+r"))
          || ((increment.startsWith("+") || increment.startsWith("-")) && ScanString
              .isDigits(increment.substring(1)))) {
        current.putProperty(Increment, increment);
        parser.moveNext(2);
      }
    }
    return new Maybe<Symbol>(current);
  }

  private boolean isDateFormatOption(String option) {
    return option.equals("-t") || option.equals("-xml");
  }

  public String toTarget(Translator translator, Symbol symbol) {
    String result = null;

    if (symbol.getProperty(Salt.Increment).equals("+r")) {
      try {
        Thread.sleep(100);

      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      calendar.setTime(SystemTimeKeeper.now());

      symbol.putProperty(Salt.Increment, "");
    }
    result = new SimpleDateFormat(makeFormat(symbol.getProperty(Salt.Format))).format(
        calendar.getTime()
    );
    return trimSaltToSize(result, symbol.getProperty(SaltSize));
  }

    private String trimSaltToSize(final String salt, final String sizeStr) {
        final int size = parseSize(sizeStr);
        final StringBuilder resultBuilder = new StringBuilder(salt);
        if (size > 0) {
            while (resultBuilder.length() < size) {
                resultBuilder.insert(0, '0');
            }
            while (resultBuilder.length() > size) {
                resultBuilder.deleteCharAt(0);
            }
        }
        return resultBuilder.toString();
    }

    private int parseSize(final String sizeStr) {
        int res = -1;
        try {
            res = Integer.parseInt(sizeStr);
        } catch (Exception ignored) {}
        return res;
    }

    private String makeFormat(String format) {
    return format.equals("-t") ? "dd MMM, yyyy HH:mm"
        : format.equals("-xml") ? "yyyy-MM-dd'T'HH:mm:ss"
            : format.length() == 0 ? "yyyyMMddHHmmssSSS" : format;
  }

}
