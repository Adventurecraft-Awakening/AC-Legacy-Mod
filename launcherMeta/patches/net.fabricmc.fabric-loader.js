/**
 * @type {Component}
 */
const FabricLoader = {
    formatVersion: 1,
    libraries: [
        repos.fabric.lib("net.fabricmc:fabric-loader:0.19.3"),
        repos.fabric.lib("net.fabricmc:sponge-mixin:0.15.4+mixin.0.8.7"),

        repos.maven.lib("org.ow2.asm:asm:9.8"),
        repos.maven.lib("org.ow2.asm:asm-analysis:9.8"),
        repos.maven.lib("org.ow2.asm:asm-commons:9.8"),
        repos.maven.lib("org.ow2.asm:asm-tree:9.8"),
        repos.maven.lib("org.ow2.asm:asm-util:9.8"),
    ],
    mainClass: "net.fabricmc.loader.impl.launch.knot.KnotClient",
    name: "Fabric Loader",
    requires: [new RequireAny("babric.intermediary")],
    type: "release",
    uid: "net.fabricmc.fabric-loader",
    version: new Version("0.19.3")
};