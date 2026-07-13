/**
 * @type {Component}
 */
const Minecraft = {
    assetIndex: {
        id: "pre-1.6",
        sha1: "3d8e55480977e32acd9844e545177e69a52f594b",
        size: 74091,
        totalSize: 49505710,
        url: "https://launchermeta.mojang.com/v1/packages/3d8e55480977e32acd9844e545177e69a52f594b/pre-1.6.json"
    },
    assets: "pre-1.6",
    compatibleJavaMajors: [
        24, 25
    ],
    downloads: {
        client: {
            sha1: "43db9b498cb67058d2e12d394e6507722e71bb45",
            size: 1465375,
            url: "https://launcher.mojang.com/v1/objects/43db9b498cb67058d2e12d394e6507722e71bb45/client.jar"
        }
    },
    complianceLevel: 0,
    libraries: [
        new Library("babric:log4j-config:1.0.0", "https://maven.glass-launcher.net/babric/"),
        new Library("com.mojang:brigadier:1.3.10", "https://libraries.minecraft.net"),

        repos.maven.lib("net.minecrell:terminalconsoleappender:1.3.0"),
        repos.maven.lib("org.slf4j:slf4j-api:1.8.0-beta4"),
        repos.maven.lib("org.apache.logging.log4j:log4j-slf4j18-impl:2.18.0"),
        repos.maven.lib("org.apache.logging.log4j:log4j-api:2.18.0"),
        repos.maven.lib("org.apache.logging.log4j:log4j-core:2.18.0"),

        repos.maven.lib("org.apache.commons:commons-lang3:3.17.0"),
        repos.maven.lib("com.google.guava:guava:33.4.0-jre"),

        repos.maven.lib("it.unimi.dsi:fastutil-core:8.5.15"),
        repos.maven.lib("org.mozilla:rhino:1.8.0"),
    ],
    id: "b1.7.3",
    formatVersion: 1,
    inClass: "net.minecraft.client.main.Main",
    minecraftArguments: "--username ${auth_player_name} --version ${version_name} --gameDir ${game_directory} --assetsDir ${assets_root} --assetIndex ${assets_index_name} --uuid ${auth_uuid} --accessToken ${auth_access_token} --userProperties ${user_properties} --userType ${user_type} --glDebugContext",
    name: "Minecraft with LWJGL3",
    order: -2,
    releaseTime: "2011-07-07T22:00:00+00:00",
    requires: [new RequireSuggests("org.lwjgl3", "3.3.6")],
    type: "old_beta",
    uid: "net.minecraft",
    version: new Version("b1.7.3"),
    "+traits": [
        "noapplet"
    ],
};