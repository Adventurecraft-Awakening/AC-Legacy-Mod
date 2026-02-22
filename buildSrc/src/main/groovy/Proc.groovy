class Proc {
    private Process proc

    Proc(File dir, String... cmdArray) {
        proc = new ProcessBuilder(cmdArray).directory(dir).start()
    }

    String getText() {
        String text = new InputStreamReader(proc.getInputStream()).readAllAsString()
        closeStreams(proc)
        return text.stripTrailing()
    }

    static void closeStreams(Process self) {
        try {
            self.getErrorStream().close()
        } catch (IOException ignored) {
        }
        try {
            self.getInputStream().close()
        } catch (IOException ignored) {
        }
        try {
            self.getOutputStream().close()
        } catch (IOException ignored) {
        }
    }
}