package native0.hidden;

/**
 * Compile-time stub.
 *
 * <p>The real {@code native0.hidden.Hidden0} is not shipped as a class file. It is defined into the
 * bootstrap class loader by the native library during {@code JNI_OnLoad}, so it only exists inside a
 * JVM that has loaded {@code native0/x64-windows.dll} (or the matching platform library).
 *
 * <p>This stub exists purely so that {@code gto.native0.GTOServicesInit},
 * {@code gto.native0.plugins.GTOServices} and {@code gto.native0.plugins.GTOProvider} compile. It is
 * never packaged into the runtime jar.
 */
public final class Hidden0 {

    private Hidden0() {}

    public static native void special_clinit_0_10(Class<?> type);

    public static native void special_clinit_1_50(Class<?> type);

    public static native void special_clinit_2_50(Class<?> type);

    public static native void special_clinit_3_90(Class<?> type);
}
