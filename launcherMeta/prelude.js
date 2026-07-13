/**
 * @param {string | URL} url
 * @constructor
 */
function URL(url) {
    if (url instanceof URL) {
        return url;
    }
    this.full = url.toString();

    this.toString = () => this.full;
    this.toJSON = () => this.full;
}

/**
 *
 * @param {string | URL} path
 * @constructor
 */
function File(path) {
    this.path = new URL(path);

    this.toString = () => this.path.toString();
    this.toJSON = () => this.path.toString();
}

/**
 *
 * @constructor
 */
function DataSource() {
}

/**
 *
 * @param {File} file
 * @constructor
 */
function FileSource(file) {
    DataSource.call(this);
    this.file = file;
}

/**
 *
 * @param {any} value
 * @constructor
 */
function JsonSource(value) {
    DataSource.call(this);
    this.value = value;

    this.toString = () => this.value.toString();
    this.toJSON = () => this.value;
}

/**
 * @param {"allow"} action
 * @param {string | {name: string}} os
 * @property {{name: string}} os
 * @constructor
 */
function AllowRule(action, os) {
    this.action = action;
    if (typeof os == "string") {
        this.os = {name: os};
    } else {
        this.os = os;
    }
}

/**
 *
 * @param {string | number | Version} value
 * @constructor
 */
function Version(value) {
    if (value instanceof Version) {
        return value;
    }
    this.value = value.toString();

    this.toString = () => this.value;
    this.toJSON = () => this.value;
}

/**
 *
 * @param {string} uid
 * @param {string | Version} suggests
 * @constructor
 */
function RequireSuggests(uid, suggests) {
    this.uid = uid;
    this.suggests = new Version(suggests);
}

/**
 *
 * @param {string} uid
 * @param {string | Version} equals
 * @constructor
 */
function RequireEquals(uid, equals) {
    this.uid = uid;
    this.equals = new Version(equals);
}

/**
 *
 * @param {string} uid
 * @constructor
 */
function RequireAny(uid) {
    this.uid = uid;
}

/**
 * @typedef {RequireEquals | RequireSuggests | RequireAny} Require
 */

/**
 *
 * @param {string} name
 * @param {string | URL} url
 * @param {[AllowRule]} [rules]
 * @constructor
 */
function Library(name, url, rules) {
    this.name = name;
    this.url = new URL(url);
    if (rules) {
        this.rules = rules;
    }
}

/**
 * @typedef Component
 * @property {string} uid
 * @property {string} name
 * @property {Version} version
 *
 * @property {[Library]} libraries
 * @property {[Require]} [requires]
 * @property {boolean} [volatile]
 */

/**
 * @param {string | URL} url
 * @property {URL} url
 * @constructor
 */
function Repository(url) {
    this.url = new URL(url);

    /**
     * @param {string} name
     * @param {[AllowRule]} [rules]
     * @returns {Library}
     */
    this.lib = function (name, rules) {
        return new Library(name, this.url, rules);
    }
}

/**
 *
 * @param {Component} component
 * @param {{dependencyOnly?: boolean, important?: boolean}} [markers]
 * @constructor
 */
function CachedComponent(component, markers) {
    this.uid = component.uid;
    this.version = component.version;
    Object.assign(this, markers);

    this.cachedName = component.name;
    this.cachedVersion = component.version;
    this.cachedVolatile = component.volatile;
    this.cachedRequires = component.requires;
}

const repos = {
    fabric: new Repository("https://maven.fabricmc.net/"),
    jitpack: new Repository("https://jitpack.io/"),
    maven: new Repository("https://repo1.maven.org/maven2/")
};

/**
 *
 * @type {Map<string | URL, DataSource>}
 */
let files = new Map();

/**
 @param {Object} descriptor
 */
function exportPack(descriptor) {
    throw ""
}