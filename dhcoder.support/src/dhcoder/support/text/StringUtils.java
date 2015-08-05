package dhcoder.support.text;

public final class StringUtils {

    private enum FormatState {
        CONSUME_TEXT, // Directly consume regular characters from input string
        GOT_LEFT_BRACE, // Got a left brace, start looking for format indices
        GOT_RIGHT_BRACE, // Got a right brace, see if we're closing off a format index
        PARSING_INDEX, // We're in between braces, parsing a format index
    }

    private static final String NULL_STR = "(null)";

    /**
     * Format a string using C# style formatting, i.e. using {0} instead of %0$s.
     * <p/>
     * See <a href="http://msdn.microsoft.com/en-us/library/system.string.format.aspx#Format_Brief">Microsoft's
     * String.Format documentation</a> for more information. Note that this method doesn't support numeric formatting,
     * such as {0.2f}.
     * <p/>
     * This is a relatively heavy method, which does a fair number of allocations, and it should not be called often in
     * performance critical sections.
     *
     * @param input The formatting string.
     * @param args  Various args whose string values will be used in the final string.
     */
    public static String format(String input, Object... args) {
        StringBuilder builder = new StringBuilder();

        FormatState state = FormatState.CONSUME_TEXT;
        int formatIndex = 0;

        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            switch (state) {
                case CONSUME_TEXT:
                    if (c == '{') {
                        state = FormatState.GOT_LEFT_BRACE;
                    }
                    else if (c == '}') {
                        state = FormatState.GOT_RIGHT_BRACE;
                    }
                    else {
                        builder.append(c);
                    }
                    break;
                case GOT_LEFT_BRACE:
                    if (c == '{') {
                        builder.append('{'); // Two left braces -> '{'
                        state = FormatState.CONSUME_TEXT;
                    }
                    else if (Character.isDigit(c)) {
                        state = FormatState.PARSING_INDEX;
                        formatIndex = Character.digit(c, 10);
                    }
                    else {
                        throwUnexpectedCharException(input, c);
                    }
                    break;
                case PARSING_INDEX:
                    if (Character.isDigit(c)) {
                        formatIndex *= 10;
                        formatIndex += Character.digit(c, 10);
                    }
                    else if (c == '}') {
                        if (formatIndex >= args.length) {
                            format("Format index {0} out of bounds ({1} arg(s)) in string {2}", formatIndex,
                                args.length, input);
                        }
                        else {
                            state = FormatState.CONSUME_TEXT;
                            builder.append(args[formatIndex] != null ? args[formatIndex].toString() : NULL_STR);
                        }
                    }
                    else {
                        throwUnexpectedCharException(input, c);
                    }
                    break;
                case GOT_RIGHT_BRACE:
                    if (c == '}') {
                        builder.append('}'); // Two right braces -> '}'
                        state = FormatState.CONSUME_TEXT;
                    }
                    else {
                        throwUnexpectedCharException(input, c);
                    }
                    break;
                default:
                    assert false; // Unhandled state, should be impossible to get here.
            }
        }

        if (state != FormatState.CONSUME_TEXT) {
            throw new IllegalArgumentException(format("Unexpected end of format string \"{0}\"", input));
        }

        return builder.toString();
    }

    public static boolean isWhitespace(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isWhitespace(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static void throwUnexpectedCharException(String input, char c) {
        throw new IllegalArgumentException(format("Unexpected char '{0}' parsing string \"{1}\"", c, input));
    }
}
