package fitnesse.wikitext.parser;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import util.Maybe;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author Leonid Konyaev &lt;LKonyaev@luxoft.com&gt;
 * @version 1.0 10.07.2011
 */
public class XlsUtils {

    /** locale to use for reading xls files */
    private static final Locale WORKBOOK_LOCALE = new Locale("en", "EN");

    /** regexp to match defined variable */
    private static final String DEFINED_VAR_REGEXP = "\\$\\{(\\w+)\\}";

    /**
     * find defined variables in var and replace
     *
     * @param str string
     * @param page parsing page
     * @return replaced string
     */
    public static String replaceDefinedVariables(
        final String str,
        final ParsingPage page
    ) {

        final java.util.regex.Matcher matcher = Pattern.compile(DEFINED_VAR_REGEXP).matcher(str);
        final StringBuffer resultBuffer = new StringBuffer();
        while(matcher.find()) {
            final String varName = matcher.group(1);
            final Maybe<String> var = page.findVariable(varName);
            if(var != Maybe.noString) {
                matcher.appendReplacement(resultBuffer, var.getValue().replaceAll("\\\\", "/"));
            }
        }
        matcher.appendTail(resultBuffer);
        return resultBuffer.toString();
    }

   /**
     * get value from xls doc
     * @param valueString value string
     * @return value from xls
     * @throws java.io.IOException when cannot open file
     * @throws jxl.read.biff.BiffException when cannot parse file
     */
    public static String getValueFromXls(
        final String valueString
    ) throws BiffException, IOException {

        final InputData data = InputData.fromValueString(valueString);
        if(data == null) {
            return null;
        }

        String result = null;
        Workbook workbook = null;
        try {
            workbook = openWorkbook(new File(data.getPathToXls()));
            result = getResultFromWorkbook(
                workbook,
                data.getSheet(),
                data.getRow(),
                data.getColumn()
            );

        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }

        return result;
    }

    /**
     * get result from workbook
     * @param workbook workbook
     * @param sheetNum number of xls sheet
     * @param row number of row
     * @param column number of column
     * @return value from xls cell
     */
    private static String getResultFromWorkbook(
        final Workbook workbook,
        final int sheetNum,
        final int row,
        final int column
    ) {

        String result = null;
        final Sheet[] sheets = workbook.getSheets();
        if(sheetNum >= 0 && sheetNum < sheets.length) {

            final Sheet sheet = workbook.getSheet(sheetNum);
            if(row >= 0 && row < sheet.getRows()) {

                final Cell[] cells = sheet.getRow(row);
                if(column >= 0 && column < cells.length) {
                    result = cells[column].getContents();
                }
            }
        }
        return result;
    }

    /**
     * opens workbook for given file
     * @param file file
     * @return opened workbook
     * @throws java.io.IOException when cannot open workbook
     * @throws jxl.read.biff.BiffException when cannot open workbook
     */
    private static Workbook openWorkbook(
        final File file
    ) throws BiffException, IOException {

        WorkbookSettings settings = new WorkbookSettings();
        settings.setLocale(WORKBOOK_LOCALE);
        return Workbook.getWorkbook(file, settings);
    }

    /**
     * input data for reading xls file container
     */
    private static class InputData {
        /** regexp to split value string */
        private static final String SPLIT_VALUE_STRING_REGEXP = ":";

        /** full path to xls document */
        private String pathToXls;
        /** sheet number */
        private int sheet;
        /** column number */
        private int column;
        /** row number */
        private int row;

        private InputData(
            final String pathToXls,
            final int sheet,
            final int column,
            final int row
        ) {

            this.pathToXls = pathToXls;
            this.sheet = sheet;
            this.column = column;
            this.row = row;
        }

        public String getPathToXls() {
            return pathToXls;
        }

        public int getColumn() {
            return column;
        }

        public int getRow() {
            return row;
        }

        public int getSheet() {
            return sheet;
        }

        /**
         * form input data from value string
         * @param valueString value string
         * @return input data or null if value string is incorrect
         */
        public static InputData fromValueString(
            final String valueString
        ) {

            String[] parts = valueString.split(SPLIT_VALUE_STRING_REGEXP);
            parts = workaroundWindowsPaths(parts);
            if(parts.length != 4) {
                return null;
            }

            final String path = parts[0];
            if(path.isEmpty()) {
                return null;
            }

            int sheet;
            int row;
            try {
                sheet = Integer.parseInt(parts[1]);
                row = Integer.parseInt(parts[3]) - 1;
            } catch (NumberFormatException e) {
                return null;
            }
            if(parts[2].isEmpty()) {
                return null;
            }
            int col = getColumnNumber(parts[2].toUpperCase());

            return new InputData(path, sheet, col, row);
        }

        /**
         * get column number from string name of a column
         * @param str columnn name e.g. 'G'
         * @return column number
         */
        private static int getColumnNumber(final String str) {
            int col = 0;
            final int alSize = 'Z' - 'A' + 1;
            final int len = str.length();
            for(int i = 0; i < len - 1; ++i) {
                final int ind = len - 1 - i;
                col += Math.pow(alSize, ind) * (str.charAt(i) - 'A' + 1);
            }
            return col + str.charAt(len - 1) - 'A';
        }

        /**
         * workaround splitted parts on windows
         * @param parts array of value parts
         * @return new array of parts
         */
        private static String[] workaroundWindowsPaths(final String[] parts) {
            if(parts.length == 5 && parts[0].length() == 1) {
                return new String[]{parts[0] + ":" + parts[1], parts[2], parts[3], parts[4]};
            }
            return parts;
        }
    }
}
