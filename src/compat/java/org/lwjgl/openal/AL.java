package org.lwjgl.openal;

import org.lwjgl.*;
import org.lwjgl.system.*;

import javax.annotation.*;

import java.nio.IntBuffer;
import java.util.*;
import java.util.function.*;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.EXTThreadLocalContext.*;
import static org.lwjgl.system.APIUtil.*;
import static org.lwjgl.system.JNI.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;


public final class AL {

    @Nullable
    private static volatile ALCapabilities processCaps;

    private static final ThreadLocal<ALCapabilities> capabilitiesTLS = new ThreadLocal<>();

    private static long _contextPtr = NULL;
    private static long _devicePtr = NULL;
    private static volatile boolean _created;

    private AL() {}

    public static boolean isCreated() {
        return _created;
    }

    public static void create(String deviceArguments, int contextFrequency, int contextRefresh, boolean contextSynchronized) throws LWJGLException {
        create(deviceArguments, contextFrequency, contextRefresh, contextSynchronized, true);
    }

    public static synchronized void create(String deviceArguments, int contextFrequency, int contextRefresh, boolean contextSynchronized, boolean openDevice) throws LWJGLException {
        if (_created) {
            throw new IllegalStateException("Only one OpenAL context may be instantiated at any one time.");
        }
        try {
            init(deviceArguments, contextFrequency, contextRefresh, contextSynchronized, openDevice);
            _created = true;
        } catch (LWJGLException | RuntimeException | Error failure) {
            try {
                destroy();
            } catch (RuntimeException | Error cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    private static void init(String deviceArguments, int contextFrequency, int contextRefresh, boolean contextSynchronized, boolean openDevice) throws LWJGLException {
        if (openDevice) {
            _devicePtr = ALC10.alcOpenDevice(deviceArguments);
            if (_devicePtr == NULL) {
                throw new LWJGLException("Could not open ALC device");
            }

            ALCCapabilities deviceCaps = ALC.createCapabilities(_devicePtr);
            if (contextFrequency == -1) {
                _contextPtr = ALC10.alcCreateContext(_devicePtr, (IntBuffer)null);
            } else {
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    _contextPtr = ALC10.alcCreateContext(_devicePtr, createAttributeList(
                        contextFrequency, contextRefresh, contextSynchronized ? 1 : 0, stack
                    ));
                }
            }
            if (_contextPtr == NULL) {
                throw new LWJGLException("Could not create ALC context");
            }
            if (!ALC10.alcMakeContextCurrent(_contextPtr)) {
                throw new LWJGLException("Could not make ALC context current");
            }
            createCapabilities(deviceCaps);
        }
    }

    public static void create() throws LWJGLException {
        create(null, 44100, 60, false);
    }

    private static final IntBuffer createAttributeList(int contextFrequency, int contextRefresh, int contextSynchronized, MemoryStack stack) {
        IntBuffer buffer = stack.callocInt(7);
        buffer.put(0, 4103);
        buffer.put(1, contextFrequency);
        buffer.put(2, 4104);
        buffer.put(3, contextRefresh);
        buffer.put(4, 4105);
        buffer.put(5, contextSynchronized);
        buffer.put(6, 0);
        return buffer;
    }

    public static synchronized void destroy() {
        long context = _contextPtr;
        long device = _devicePtr;
        _contextPtr = NULL;
        _devicePtr = NULL;
        _created = false;
        al_destroy();

        try {
            if (context != NULL) {
                try {
                    ALC10.alcMakeContextCurrent(NULL);
                } finally {
                    ALC10.alcDestroyContext(context);
                }
            }
        } finally {
            if (device != NULL) {
                try {
                    ALC10.alcCloseDevice(device);
                } finally {
                    ALC.setCapabilities(null);
                }
            }
        }
    }

    static void init() {
    }

    static void al_destroy() {
        setCurrentProcess(null);
    }

    
    public static void setCurrentProcess(@Nullable ALCapabilities caps) {
        processCaps = caps;
        capabilitiesTLS.remove();
    }

    
    public static void setCurrentThread(@Nullable ALCapabilities caps) {
        if (caps == null) {
            capabilitiesTLS.remove();
        } else {
            capabilitiesTLS.set(caps);
        }
    }

    
    public static ALCapabilities getCapabilities() {
        ALCapabilities caps = capabilitiesTLS.get();
        if (caps == null) {
            caps = processCaps;
        }

        return checkCapabilities(caps);
    }

    private static ALCapabilities checkCapabilities(@Nullable ALCapabilities caps) {
        if (caps == null) {
            throw new IllegalStateException(
                "No ALCapabilities instance set for the current thread or process. Possible solutions:\n" +
                "\ta) Call AL.createCapabilities() after making a context current.\n" +
                "\tb) Call AL.setCurrentProcess() or AL.setCurrentThread() if an ALCapabilities instance already exists."
            );
        }
        return caps;
    }

    
    public static ALCapabilities createCapabilities(ALCCapabilities alcCaps) {
        return createCapabilities(alcCaps, null);
    }

    
    public static ALCapabilities createCapabilities(ALCCapabilities alcCaps, @Nullable IntFunction<PointerBuffer> bufferFactory) {
        
        
        
        long alGetProcAddress = ALC.getFunctionProvider().getFunctionAddress(NULL, "alGetProcAddress");
        if (alGetProcAddress == NULL) {
            throw new RuntimeException("A core AL function is missing. Make sure that the OpenAL library has been loaded correctly.");
        }

        FunctionProvider functionProvider = functionName -> {
            long address = invokePP(memAddress(functionName), alGetProcAddress);
            if (address == NULL && Checks.DEBUG_FUNCTIONS) {
                apiLogMissing("AL", functionName);
            }
            return address;
        };

        long GetString          = functionProvider.getFunctionAddress("alGetString");
        long GetError           = functionProvider.getFunctionAddress("alGetError");
        long IsExtensionPresent = functionProvider.getFunctionAddress("alIsExtensionPresent");
        if (GetString == NULL || GetError == NULL || IsExtensionPresent == NULL) {
            throw new IllegalStateException("Core OpenAL functions could not be found. Make sure that the OpenAL library has been loaded correctly.");
        }

        String versionString = memASCIISafe(invokeP(AL_VERSION, GetString));
        if (versionString == null || invokeI(GetError) != AL_NO_ERROR) {
            throw new IllegalStateException("There is no OpenAL context current in the current thread or process.");
        }

        APIVersion apiVersion = apiParseVersion(versionString);

        int majorVersion = apiVersion.major;
        int minorVersion = apiVersion.minor;

        int[][] AL_VERSIONS = {
            {0, 1}  
        };

        Set<String> supportedExtensions = new HashSet<>(32);

        for (int major = 1; major <= AL_VERSIONS.length; major++) {
            int[] minors = AL_VERSIONS[major - 1];
            for (int minor : minors) {
                if (major < majorVersion || (major == majorVersion && minor <= minorVersion)) {
                    supportedExtensions.add("OpenAL" + major + minor);
                }
            }
        }

        
        String extensionsString = memASCIISafe(invokeP(AL_EXTENSIONS, GetString));
        if (extensionsString != null) {
            MemoryStack stack = stackGet();

            StringTokenizer tokenizer = new StringTokenizer(extensionsString);
            while (tokenizer.hasMoreTokens()) {
                String extName = tokenizer.nextToken();
                try (MemoryStack frame = stack.push()) {
                    if (invokePZ(memAddress(frame.ASCII(extName, true)), IsExtensionPresent)) {
                        supportedExtensions.add(extName);
                    }
                }
            }
        }

        if (alcCaps.ALC_EXT_EFX) {
            supportedExtensions.add("ALC_EXT_EFX");
        }
        apiFilterExtensions(supportedExtensions, Configuration.OPENAL_EXTENSION_FILTER);

        ALCapabilities caps = new ALCapabilities(functionProvider, supportedExtensions, bufferFactory == null ? BufferUtils::createPointerBuffer : bufferFactory);

        if (alcCaps.ALC_EXT_thread_local_context && alcGetThreadContext() != NULL) {
            setCurrentThread(caps);
        } else {
            setCurrentProcess(caps);
        }

        return caps;
    }

    static ALCapabilities getICD() {
        // The upstream write-once dispatch cache retains the first context's
        // capability addresses after destroy(). Resolve the active context on
        // every call so a new legacy context cannot reuse destroyed state.
        return ALC.check(getCapabilities());
    }

}
