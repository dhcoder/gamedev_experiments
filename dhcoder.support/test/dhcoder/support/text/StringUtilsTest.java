package dhcoder.support.text;

import org.junit.Test;

import static dhcoder.support.text.StringUtils.format;
import static dhcoder.test.TestUtils.assertException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public final class StringUtilsTest {

    @Test
    public void formatHandlesBasicValues() {
        assertThat(format("Hello {0}!", "World"), equalTo("Hello World!"));
        assertThat(format("Easy as {0}, {1}, {2}...", 1, 2, 3), equalTo("Easy as 1, 2, 3..."));
    }

    @Test
    public void formatWorksWithNoIndices() {
        assertThat(format("No args"), equalTo("No args"));
        assertThat(format("Hello World!", "unused"), equalTo("Hello World!"));
        assertThat(format("", 1, 2, 3), equalTo(""));
    }

    @Test
    public void formatAllowsDuplicateIndices() {
        assertThat(format("{0}+{0}={1}", 2, 4), equalTo("2+2=4"));
    }

    @Test
    public void formatAllowsSkippingIndices() {
        assertThat(format("First: {0}, Last: {2}", "Edgar", "Allen", "Poe"), equalTo("First: Edgar, Last: Poe"));
    }

    @Test
    public void formatEscapesDoubleBraces() {
        assertThat(format("Argument {{0}} -> {0}", "3.14"), equalTo("Argument {0} -> 3.14"));
        assertThat(format("{{{{{{We must go deeper}}}}}}"), equalTo("{{{We must go deeper}}}"));
    }

    @Test
    public void formatHandlesRecursion() {
        assertThat(format("Recursion! {0}{1}{0}", "{0}", "{1}"), equalTo("Recursion! {0}{1}{0}"));
    }

    @Test
    public void formatCanHandleMultipleDigitIndices() {

        Object[] args = new Object[1000];
        for (int i = 0; i < args.length; ++i) {
            args[i] = Integer.toString(i);
        }

        assertThat(format("{987}", args), equalTo("987"));
    }

    @Test
    public void formatThrowsExceptionWithUnescapedBraces() {
        assertException("Left bracket must be escaped", IllegalArgumentException.class, new Runnable() {
            @Override
            public void run() {
                format("Unescaped { throws exception");
            }
        });

        assertException("Right bracket must be escaped", IllegalArgumentException.class, new Runnable() {
            @Override
            public void run() {
                format("Unescaped } throws exception");
            }
        });

        assertException("Left bracket must be closed before the end", IllegalArgumentException.class, new Runnable() {
            @Override
            public void run() {
                format("Yeah this isn't good: {0");
            }
        });
    }

    @Test
    public void formatThrowsExceptionWithOutOfBoundsIndex() {
        assertException("Format indices must be in bounds", IllegalArgumentException.class, new Runnable() {
            @Override
            public void run() {
                format("Shopping list: {0}, {1}, and {2}", "eggs", "cheese");
            }
        });
    }

    @Test
    public void formatIndicesCanOnlyBeIntegers() {
        assertException("Format index can't be a name", IllegalArgumentException.class, new Runnable() {
            @Override
            public void run() {
                format("You can't use {named} format parameters");
            }
        });

        assertException("Format index cannot contain decimals", IllegalArgumentException.class, new Runnable() {
            @Override
            public void run() {
                format("Floats like {1.3} don't work");
            }
        });

        assertException("Format index cannot contain letters", IllegalArgumentException.class, new Runnable() {
            @Override
            public void run() {
                format("Numeric qualifiers like {0d} and {1f} aren't necessary and don't work");
            }
        });

        assertException("Format index cannot be empty", IllegalArgumentException.class, new Runnable() {
            @Override
            public void run() {
                format("Empty braces {} make format cry.");
            }
        });
    }

}