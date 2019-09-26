package com.android.watermark;

import android.os.Looper;
import android.util.Log;

import java.util.Arrays;

/**
 * Author : ZhongJiHao
 * Organization : Shenzhen
 * Date :  2018/11/2 10:21
 * Description :断言检测类
 */
public class Assert {
    private final static String TAG = "Assert";
    public static @interface RunsOnMainThread {}
    public static @interface DoesNotRunOnMainThread {}
    public static @interface RunsOnAnyThread {}

    private static final String TEST_THREAD_SUBSTRING = "test";

    private Assert() {

    }

    /**
     * 断定条件一定为true
     * @param condition
     */
    public static void isTrue(final boolean condition) {
        if (!condition) {
            fail("Expected condition to be true", true);
        }
    }

    /**
     * 断定条件一定为false
     * @param condition
     */
    public static void isFalse(final boolean condition) {
        if (condition) {
            fail("Expected condition to be false", false);
        }
    }

    /**
     * 断定期望值和实际值相等
     * @param expected
     * @param actual
     */
    public static void equals(final int expected, final int actual) {
        if (expected != actual) {
            fail("Expected " + expected + " but got " + actual, false);
        }
    }

    /**
     * 断定期望值和实际值相等
     * @param expected
     * @param actual
     */
    public static void equals(final long expected, final long actual) {
        if (expected != actual) {
            fail("Expected " + expected + " but got " + actual, false);
        }
    }

    /**
     * 断定期望值和实际值相等
     * @param expected
     * @param actual
     */
    public static void equals(final Object expected, final Object actual) {
        if (expected != actual
                && (expected == null || actual == null || !expected.equals(actual))) {
            fail("Expected " + expected + " but got " + actual, false);
        }
    }

    /**
     * 断定输入参数值actual在期望值列表中
     * @param actual
     * @param expected
     */
    public static void oneOf(final int actual, final int ...expected) {
        for (int value : expected) {
            if (actual == value) {
                return;
            }
        }
        fail("Expected value to be one of " + Arrays.toString(expected) + " but was " + actual);
    }

    /**
     * 断定输入参数值val在[rangeMinInclusive, rangeMaxInclusive]该区间
     * @param val
     * @param rangeMinInclusive
     * @param rangeMaxInclusive
     */
    public static void inRange(
            final int val, final int rangeMinInclusive, final int rangeMaxInclusive) {
        if (val < rangeMinInclusive || val > rangeMaxInclusive) {
            fail("Expected value in range [" + rangeMinInclusive + ", " +
                    rangeMaxInclusive + "], but was " + val, false);
        }
    }

    /**
     * 断定输入参数值val在[rangeMinInclusive, rangeMaxInclusive]该区间
     * @param val
     * @param rangeMinInclusive
     * @param rangeMaxInclusive
     */
    public static void inRange(
            final long val, final long rangeMinInclusive, final long rangeMaxInclusive) {
        if (val < rangeMinInclusive || val > rangeMaxInclusive) {
            fail("Expected value in range [" + rangeMinInclusive + ", " +
                    rangeMaxInclusive + "], but was " + val, false);
        }
    }

    /**
     * 断定当前线程是主线程
     */
    public static void isMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()
                && !Thread.currentThread().getName().contains(TEST_THREAD_SUBSTRING)) {
            fail("Expected to run on main thread", false);
        }
    }

    /**
     * 断定当前线程不是主线程
     */
    public static void isNotMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()
                && !Thread.currentThread().getName().contains(TEST_THREAD_SUBSTRING)) {
            fail("Not expected to run on main thread", true);
        }
    }

    /**
     * 断定输入值一定为NULL
     * @param obj
     */
    public static void isNull(final Object obj) {
        if (obj != null) {
            fail("Expected object to be null", true);
        }
    }

    /**
     * 断定输入值一定为NULL
     * @param obj
     * @param failureMessage
     */
    public static void isNull(final Object obj, final String failureMessage) {
        if (obj != null) {
            fail(failureMessage, false);
        }
    }

    /**
     *  断定输入对象非空
     * @param obj
     */
    public static void notNull(final Object obj) {
        if (obj == null) {
            fail("Expected value to be non-null", true);
        }
    }

    /**
     * 打印堆栈信息
     * @param message
     */
    public static void fail(final String message) {
        fail("Assert.fail() called: " + message, false);
    }

    /**
     * 打印堆栈信息
     * @param message
     * @param crashRelease
     */
    private static void fail(final String message, final boolean crashRelease) {
        Log.e(TAG, message);
        if (crashRelease) {
            throw new AssertionError(message);
        } else {
            // Find the method whose assertion failed. We're using a depth of 2, because all public
            // Assert methods delegate to this one (see javadoc on getCaller() for details).
            StackTraceElement caller = getCaller(2);
            if (caller != null) {
                // This log message can be de-obfuscated by the Proguard retrace tool, just like a
                // full stack trace from a crash.
                Log.e(TAG, "\tat " + caller.toString());
            }
        }
    }

    /**
     * Returns info about the calling method. The {@code depth} parameter controls how far back to
     * go. For example, if foo() calls bar(), and bar() calls getCaller(0), it returns info about
     * bar(). If bar() instead called getCaller(1), it would return info about foo(). And so on.
     * <p>
     * NOTE: This method retrieves the current thread's stack trace, which adds runtime overhead.
     * It should only be used in production where necessary to gather context about an error or
     * unexpected event (e.g. the {@link Assert} class uses it).
     *
     * @return stack frame information for the caller (if found); otherwise {@code null}.
     */
    private static StackTraceElement getCaller(int depth) {
        // If the signature of this method is changed, proguard.flags must be updated!
        if (depth < 0) {
            throw new IllegalArgumentException("depth cannot be negative");
        }
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace == null || trace.length < (depth + 2)) {
            return null;
        }
        // The stack trace includes some methods we don't care about (e.g. this method).
        // Walk down until we find this method, and then back up to the caller we're looking for.
        for (int i = 0; i < trace.length - 1; i++) {
            String methodName = trace[i].getMethodName();
            if ("getCaller".equals(methodName)) {
                return trace[i + depth + 1];
            }
        }
        // Never found ourself in the stack?!
        return null;
    }




}
