package dhcoder.support.opt;

import org.junit.Test;

import static dhcoder.test.TestUtils.assertException;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public final class OptFloatTest {

    private static final float DUMMY_VALUE = -1234.5f;

    @Test
    public void createOptionalWithNoValueWorks() {
        OptFloat floatOpt = OptFloat.withNoValue();
        assertThat(floatOpt.hasValue(), equalTo(false));
    }

    @Test
    public void createOptionalWithValueWorks() {
        OptFloat floatOpt = OptFloat.of(DUMMY_VALUE);
        assertThat(floatOpt.hasValue(), equalTo(true));
        assertThat(floatOpt.getValue(), equalTo(DUMMY_VALUE));
    }

    @Test
    public void settingOptionalValueWorks() {
        OptFloat floatOpt = OptFloat.withNoValue();
        OptFloat zeroFloatOpt = OptFloat.of(0f);
        assertThat(floatOpt.hasValue(), equalTo(false));

        floatOpt.set(DUMMY_VALUE);
        assertThat(floatOpt.hasValue(), equalTo(true));
        assertThat(floatOpt.getValue(), equalTo(DUMMY_VALUE));

        floatOpt.clear();
        assertThat(floatOpt.hasValue(), equalTo(false));

        floatOpt.setFrom(zeroFloatOpt);
        assertThat(floatOpt.hasValue(), equalTo(true));
        assertThat(floatOpt.getValue(), equalTo(0f));
    }

    @Test
    public void clearOptionalWorks() {
        OptFloat floatOpt = OptFloat.of(DUMMY_VALUE);
        assertThat(floatOpt.hasValue(), equalTo(true));

        floatOpt.clear();
        assertThat(floatOpt.hasValue(), equalTo(false));
    }

    @Test
    public void testOptionalEquality() {
        OptFloat floatOpt = OptFloat.of(DUMMY_VALUE);
        OptFloat floatOptDuplicate = OptFloat.of(DUMMY_VALUE);
        OptFloat emptyValue = OptFloat.withNoValue();

        assertThat(floatOpt.equals(floatOptDuplicate), equalTo(true));
        assertThat(floatOpt.equals(emptyValue), equalTo(false));
        assertThat(floatOpt.hashCode(), equalTo(floatOptDuplicate.hashCode()));
    }

    @Test
    public void getValueWithoutValueThrowsException() {
        final OptFloat emptyfloatOpt = OptFloat.withNoValue();
        assertException("Can't get a value from a valueless optional", IllegalStateException.class, new Runnable() {
            @Override
            public void run() {
                emptyfloatOpt.getValue();
            }
        });
    }
}