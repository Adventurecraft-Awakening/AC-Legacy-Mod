const AllowLinux = [
    new AllowRule("allow", "linux")
];
const AllowLinuxArm64 = [
    AllowLinux[0],
    new AllowRule("allow", "linux-arm64")
];

const AllowOsx = [
    new AllowRule("allow", "osx")
];
const AllowOsxArm64 = [
    AllowOsx[0],
    new AllowRule("allow", "osx-arm64")
];

const AllowWindows = [
    new AllowRule("allow", "windows")
];
const AllowWindowsArm64 = [
    AllowWindows[0],
    new AllowRule("allow", "windows-arm64")
];

/**
 * @type {Component}
 */
const LWJGL3 = (() => {
    const mvn = repos.maven;
    return {
        formatVersion: 1,
        libraries: [
            repos.jitpack.lib("com.github.Adventurecraft-Awakening:AC-LWJGL3-Injector:0.2.5.0"),

            mvn.lib("org.lwjgl:lwjgl:3.3.6"),
            mvn.lib("org.lwjgl:lwjgl:3.3.6:natives-linux", AllowLinux),
            mvn.lib("org.lwjgl:lwjgl:3.3.6:natives-macos-arm64", AllowOsxArm64),
            mvn.lib("org.lwjgl:lwjgl:3.3.6:natives-macos", AllowOsx),
            mvn.lib("org.lwjgl:lwjgl:3.3.6:natives-windows-x86", AllowWindows),
            mvn.lib("org.lwjgl:lwjgl:3.3.6:natives-windows", AllowWindows),

            mvn.lib("org.lwjgl:lwjgl-glfw:3.3.6"),
            mvn.lib("org.lwjgl:lwjgl-glfw:3.3.6:natives-linux", AllowLinux),
            mvn.lib("org.lwjgl:lwjgl-glfw:3.3.6:natives-macos-arm64", AllowOsxArm64),
            mvn.lib("org.lwjgl:lwjgl-glfw:3.3.6:natives-macos", AllowOsx),
            mvn.lib("org.lwjgl:lwjgl-glfw:3.3.6:natives-windows-x86", AllowWindows),
            mvn.lib("org.lwjgl:lwjgl-glfw:3.3.6:natives-windows", AllowWindows),

            mvn.lib("org.lwjgl:lwjgl-openal:3.3.6"),
            mvn.lib("org.lwjgl:lwjgl-openal:3.3.6:natives-linux", AllowLinux),
            mvn.lib("org.lwjgl:lwjgl-openal:3.3.6:natives-macos-arm64", AllowOsxArm64),
            mvn.lib("org.lwjgl:lwjgl-openal:3.3.6:natives-macos", AllowOsx),
            mvn.lib("org.lwjgl:lwjgl-openal:3.3.6:natives-windows-x86", AllowWindows),
            mvn.lib("org.lwjgl:lwjgl-openal:3.3.6:natives-windows", AllowWindows),

            mvn.lib("org.lwjgl:lwjgl-opengl:3.3.6"),
            mvn.lib("org.lwjgl:lwjgl-opengl:3.3.6:natives-linux", AllowLinux),
            mvn.lib("org.lwjgl:lwjgl-opengl:3.3.6:natives-macos-arm64", AllowOsxArm64),
            mvn.lib("org.lwjgl:lwjgl-opengl:3.3.6:natives-macos", AllowOsx),
            mvn.lib("org.lwjgl:lwjgl-opengl:3.3.6:natives-windows-x86", AllowWindows),
            mvn.lib("org.lwjgl:lwjgl-opengl:3.3.6:natives-windows", AllowWindows),

            mvn.lib("org.lwjgl:lwjgl-stb:3.3.6"),
            mvn.lib("org.lwjgl:lwjgl-stb:3.3.6:natives-linux", AllowLinux),
            mvn.lib("org.lwjgl:lwjgl-stb:3.3.6:natives-macos-arm64", AllowOsxArm64),
            mvn.lib("org.lwjgl:lwjgl-stb:3.3.6:natives-macos", AllowOsx),
            mvn.lib("org.lwjgl:lwjgl-stb:3.3.6:natives-windows-x86", AllowWindows),
            mvn.lib("org.lwjgl:lwjgl-stb:3.3.6:natives-windows", AllowWindows),
        ],
        name: "LWJGL 3",
        order: -1,
        releaseTime: "2022-11-29T14:28:08+00:00",
        type: "release",
        uid: "org.lwjgl3",
        version: new Version("3.3.6"),
        volatile: true
    };
})();
