package priv.znd.util.jsonutil;

import com.sun.tools.javac.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.ValueMatcherException;
import org.skyscreamer.jsonassert.comparator.CustomComparator;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author lddsp
 * @date 2021/3/30 15:04
 */
public class CustomContComparator extends CustomComparator {
    private final Collection<Customization> customizations;
    public CustomContComparator(JSONCompareMode mode, Customization... customizations) {
        super(mode);
        this.customizations = Arrays.asList(customizations);
    }
    @Override
    public void compareValues(String prefix, Object expectedValue, Object actualValue, JSONCompareResult result)
            throws JSONException {
        Customization customization = getCustomization(prefix);
        if (customization != null) {
            try {
                if (!customization.matches(prefix, actualValue, expectedValue, result)) {
                    result.fail(prefix, expectedValue, actualValue);
                }
            }
            catch (ValueMatcherException e) {
                result.fail(prefix, e);
            }
        }else {
            if (areNumbers(expectedValue, actualValue)) {
                if (areNotSameDoubles(expectedValue, actualValue)) {
                    result.fail(prefix, expectedValue, actualValue);
                }
            } else if (expectedValue.getClass().isAssignableFrom(actualValue.getClass())) {
                if (expectedValue instanceof JSONArray) {
                    compareJSONArray(prefix, (JSONArray) expectedValue, (JSONArray) actualValue, result);
                } else if (expectedValue instanceof JSONObject) {
                    compareJSON(prefix, (JSONObject) expectedValue, (JSONObject) actualValue, result);
                } else if (!expectedValue.equals(actualValue)) {
                    if ("".equals("expectedValue"))
                    result.fail(prefix, expectedValue, actualValue);
                } else
                    result.fail(prefix, expectedValue, actualValue);
            } else {
                result.fail(prefix, expectedValue, actualValue);

            }
        }
    }

    protected boolean areNumbers(Object expectedValue, Object actualValue) {
        return expectedValue instanceof Number && actualValue instanceof Number;
    }

    protected boolean areNotSameDoubles(Object expectedValue, Object actualValue) {
        return ((Number) expectedValue).doubleValue() != ((Number) actualValue).doubleValue();
    }

    private Customization getCustomization(String path) {
        for (Customization c : customizations)
            if (c.appliesToPath(path))
                return c;
        return null;
    }
}
