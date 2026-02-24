import java.util.regex.Pattern

class GitState {
    static final TagPattern = Pattern.compile("(.*)-([0-9]+)-g.?[0-9a-fA-F]{3,}")

    private String desc
    private String status
    private String hash
    private String branch
    private String originUrl

    GitState(File dir) {
        desc = new Proc(dir, "git", "describe", "--tags", "--always", "--first-parent", "--abbrev=7", "--match=*", "HEAD").text
        status = new Proc(dir, "git", "status", "--porcelain").text
        hash = new Proc(dir, "git", "rev-parse", "HEAD").text
        branch = new Proc(dir, "git", "branch", "--show-current").text
        originUrl = new Proc(dir, "git", "config", "remote.origin.url").text
    }

    private boolean describeIsPlainTag() {
        !Pattern.matches(".*g.?[0-9a-fA-F]{3,}", getDescribe())
    }

    String getVersion() {
        if (getDescribe() == null) {
            return "unspecified"
        }
        return getDescribe() + (getIsCleanTag() ? "" : ".dirty")
    }

    String getDescribe() {
        desc
    }

    boolean getIsCleanTag() {
        status.isEmpty() && describeIsPlainTag()
    }

    int getCommitDistance() {
        if (describeIsPlainTag()) {
            return 0
        }
        def match = TagPattern.matcher(getDescribe())
        return match.matches() ? Integer.parseInt(match.group(2)) : 0
    }

    String getLastTag() {
        if (describeIsPlainTag()) {
            return getDescribe()
        }
        def match = TagPattern.matcher(getDescribe())
        return match.matches() ? match.group(1) : null
    }

    String getHash() {
        hash
    }

    String getShortHash() {
        hash.substring(0, 10)
    }

    String getBranch() {
        branch
    }

    String getOriginUrl() {
        originUrl
    }
}