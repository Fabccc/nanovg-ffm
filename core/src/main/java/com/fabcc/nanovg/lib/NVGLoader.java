package com.fabcc.nanovg.lib;

import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class NVGLoader {

    protected static SymbolLookup SYMBOL_LOOKUP;
    protected static Arena LIBRARY_ARENA = Arena.ofAuto();

    private static File tempLib;

    /**
     * Load the correct native C code for the current platform.
     */
    public static void init() {
        if (tempLib != null || SYMBOL_LOOKUP != null) {
            return;
        }
        String operatingSystem = System.getProperty("os.name").toLowerCase();
        String larch = System.getProperty("os.arch");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        String finalArch = larch;
        if (wow64Arch != null && !wow64Arch.isEmpty()) {
            finalArch = wow64Arch.equals("32") ? "x86" : larch;
        }
        String architecture = switch (finalArch) {
            case "x86", "i386", "i486", "i586", "i686" -> "x86";
            case "amd64", "x86_64" -> "amd64";
            case "aarch64", "arm64" -> "aarch64";
            case "arm" -> "arm32";
            default -> finalArch;
        };

        String os = switch (operatingSystem) {
            case String s when s.contains("win") -> "windows";
            case String s when s.contains("mac") -> "macos";
            case String s when s.contains("nux") -> "linux";
            case String s when s.contains("sunos") -> "sunos";
            case String s when s.contains("openbsd") -> "openbsd";
            case String s when s.contains("freebsd") -> "freebsd";
            default -> "unknown";
        };
        String fullArch = os + "-" + architecture;

        String libName = switch (fullArch) {
            case "linux-amd64" -> "libnanovgffm-x86-64-linux.so";
            case "linux-aarch64" -> "libnanovgffm-aarch64-linux.so";
            case "windows-amd64" -> "libnanovgffm-x86-64-windows.dll";
            case "macos-amd64" -> "libnanovgffm-x86-64-macos.dylib";
            case "macos-aarch64" -> "libnanovgffm-aarch64-macos.dylib";
            case "freebsd-amd64" -> "libnanovgffm-x86-64-freebsd.so";
            default -> throw new UnsupportedOperationException("Unsupported platform: " + fullArch);
        };
        try (var inputStream = NVGLoader.class.getResourceAsStream("/natives/" + libName)) {
            if (inputStream == null) {
                throw new UnsatisfiedLinkError("Native library " + libName + " not found in resources");
            }
            tempLib = File.createTempFile("nanovgffm", libName.substring(libName.lastIndexOf('.')));
            Files.copy(inputStream, tempLib.toPath(), StandardCopyOption.REPLACE_EXISTING);

            SYMBOL_LOOKUP = SymbolLookup.libraryLookup(tempLib.getAbsolutePath(), LIBRARY_ARENA);
        } catch (Exception e) {
            throw new UnsatisfiedLinkError("Failed to load native library: " + e.getMessage());
        }
    }

    public static void cleanup() {
        if (tempLib != null && tempLib.exists()) {
            tempLib.delete();
            tempLib = null;
        }
    }

}
