package dev.adventurecraft.awakening.client.render.compat;

import dev.adventurecraft.awakening.client.render.RenderSystem;
import dev.adventurecraft.awakening.client.render.RenderScene;
import dev.adventurecraft.awakening.client.render.RenderPhase;
import dev.adventurecraft.awakening.client.render.RenderMath;
import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.resource.TextureDesc;
import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.client.render.texture.TextureStore;

import dev.adventurecraft.awakening.client.render.vulkan.*;
import dev.adventurecraft.awakening.layout.Size;
import org.lwjgl.BufferUtils;

import static dev.adventurecraft.awakening.client.render.compat.LegacyGlConstants.*;

import java.nio.*;
import java.util.*;


/**
 * Fixed-function OpenGL ABI for inherited Minecraft rendering.
 * Class-load routing targets this adapter after mixin injection. Owned mesh
 * rendering uses NativeRenderer; device and frame ownership stays in RenderSystem.
 */
public final class LegacyGlAdapter {
    private static Matrix4 atmosphereViewSource;
    private static Matrix4 atmosphereInverseView;
    private static final Map<Integer, Texture> TEXTURES = new HashMap<>();
    private static final Map<Integer, List<Runnable>> LISTS = new HashMap<>();
    private static final Map<Integer, List<VulkanRenderer.Mesh>> LIST_MESHES = new HashMap<>();
    private static final Deque<Attributes> ATTRIBUTES = new ArrayDeque<>();
    private static final Map<Integer, Deque<float[]>> MATRIX_STACKS = new HashMap<>();
    private static final Map<Integer, Integer> PIXEL_STORE = new HashMap<>();
    private static final List<AutoCloseable> RETIRED = new ArrayList<>();
    private static State state = new State();
    private static int nextId = 1;
    private static int activeTexture, clientTexture, matrixMode = MODELVIEW;
    private static final int[] boundTextures = new int[8];
    private static int primitive = -1;
    private static ByteBuffer immediate;
    private static List<Runnable> compiling;
    private static int compileMode, compileId, replayDepth;
    private static boolean executing;
    private static int error;

    private LegacyGlAdapter() { }
    public static boolean isEnabled() { return true; }

    private static void ensureFrame() { RenderSystem.ensureFrame(); }
    private static VulkanRenderer.GpuFrame finishFrame() { return RenderSystem.finishFrame(); }

    public static void retireResources() {
        for (AutoCloseable item : RETIRED) {
            try { item.close(); }
            catch (Exception ex) { throw new IllegalStateException("Cannot release Vulkan draw resource", ex); }
        }
        RETIRED.clear();
    }

    public static void releaseResources() {
        TextureStore.releaseGpuResources();
        for (var meshes : LIST_MESHES.values()) for (var mesh : meshes) mesh.close();
        retireResources();
        LISTS.clear(); LIST_MESHES.clear();
        ATTRIBUTES.clear();
        compiling = null; immediate = null; primitive = -1; replayDepth = 0; executing = false; error = 0;
    }

    private static boolean record(Runnable operation) {
        if (compiling == null || executing) return false;
        compiling.add(operation);
        return compileMode == COMPILE;
    }

    public static int genLists(int range) { int first = nextId; nextId += Math.max(range, 1); return first; }
    public static void newList(int id, int mode) {
        if (compiling != null) throw new IllegalStateException("Nested display list compilation");
        deleteLists(id,1);
        compileId = id; compileMode = mode; compiling = new ArrayList<>();
    }
    public static void endList() { LISTS.put(compileId, compiling); compiling = null; }
    public static void callList(int id) {
        if (record(() -> callList(id))) return;
        if (++replayDepth > 64) throw new IllegalStateException("Recursive display list");
        boolean old = executing;
        executing = true;
        try { for (Runnable command : LISTS.getOrDefault(id, List.of())) command.run(); }
        finally { executing = old; replayDepth--; }
    }
    public static void callLists(IntBuffer ids) { for (int i = ids.position(); i < ids.limit(); i++) callList(ids.get(i)); }
    public static void deleteLists(int first, int count) {
        for (int i = 0; i < count; i++) {
            LISTS.remove(first+i);
            var resources=LIST_MESHES.remove(first+i);
            if(resources!=null)RETIRED.addAll(resources);
        }
    }
    public static boolean isList(int id) { return LISTS.containsKey(id); }

    public static void begin(int mode) {
        if (record(() -> begin(mode))) return;
        primitive = mode;
        immediate = BufferUtils.createByteBuffer(4096);
    }
    public static void end() {
        if (record(LegacyGlAdapter::end)) return;
        if (primitive < 0) { error = INVALID_OPERATION; return; }
        immediate.flip();
        if (immediate.hasRemaining()) submit(immediate, primitive, null);
        immediate = null; primitive = -1;
    }
    public static void vertex3f(float x, float y, float z) {
        if (record(() -> vertex3f(x, y, z))) return;
        if (immediate == null) { error = INVALID_OPERATION; return; }
        if (immediate.remaining() < 32) {
            ByteBuffer larger = BufferUtils.createByteBuffer(immediate.capacity() * 2);
            immediate.flip(); larger.put(immediate); immediate = larger;
        }
        vertex(immediate, x, y, z, state.uv[0], state.uv[1], state.color, state.normal);
    }
    public static void vertex2f(float x, float y) { vertex3f(x, y, 0); }
    public static void vertex3d(double x, double y, double z) { vertex3f((float)x, (float)y, (float)z); }
    public static void vertex2d(double x, double y) { vertex3f((float)x, (float)y, 0); }
    public static void texCoord2f(float u, float v) {
        if (record(() -> texCoord2f(u, v))) return;
        state.uv = new float[]{u, v};
    }
    public static void texCoord2d(double u, double v) { texCoord2f((float)u, (float)v); }
    public static void normal3f(float x, float y, float z) {
        if (record(() -> normal3f(x, y, z))) return;
        state.normal = new float[]{x, y, z};
    }
    public static void color4f(float r, float g, float b, float a) {
        if (record(() -> color4f(r, g, b, a))) return;
        state.color = new float[]{r, g, b, a};
    }
    public static void color3f(float r, float g, float b) { color4f(r, g, b, 1); }
    public static void color3d(double r, double g, double b) { color4f((float)r, (float)g, (float)b, 1); }
    public static void color4d(double r, double g, double b, double a) { color4f((float)r, (float)g, (float)b, (float)a); }
    public static void color4ub(byte r, byte g, byte b, byte a) { color4f((r&255)/255f,(g&255)/255f,(b&255)/255f,(a&255)/255f); }

    public static void enable(int cap) { if (!record(() -> enable(cap))) state.enabled.add(cap); }
    public static void disable(int cap) { if (!record(() -> disable(cap))) state.enabled.remove(cap); }
    public static boolean isEnabled(int cap) { return state.enabled.contains(cap); }
    public static void alphaFunc(int function, float ref) {
        if (record(() -> alphaFunc(function, ref))) return;
        state.alphaFunc = function; state.alphaRef = clamp(ref);
    }
    public static void blendFunc(int source, int destination) {
        if (record(() -> blendFunc(source, destination))) return;
        state.blendSrc = source; state.blendDst = destination;
    }
    public static void blendColor(float r,float g,float b,float a) {
        if(!record(()->blendColor(r,g,b,a)))state.blendColor = new float[]{clamp(r),clamp(g),clamp(b),clamp(a)};
    }
    public static void depthFunc(int function) { if (!record(() -> depthFunc(function))) state.depthFunc = function; }
    public static void depthMask(boolean write) { if (!record(() -> depthMask(write))) state.depthWrite = write; }
    public static void depthRange(double min, double max) {
        if (record(() -> depthRange(min,max))) return;
        state.minDepth = clamp((float)min); state.maxDepth = clamp((float)max);
    }
    public static void colorMask(boolean r,boolean g,boolean b,boolean a) {
        if (record(() -> colorMask(r,g,b,a))) return;
        state.colorMask = (r?1:0)|(g?2:0)|(b?4:0)|(a?8:0);
    }
    public static void cullFace(int face) { if (!record(() -> cullFace(face))) state.cullFace = face; }
    public static void frontFace(int winding) { if (!record(() -> frontFace(winding))) state.frontFace = winding; }
    public static void lineWidth(float value) { if (!record(() -> lineWidth(value))) state.lineWidth = value; }
    public static void polygonOffset(float factor,float units) {
        if (record(() -> polygonOffset(factor,units))) return;
        state.offsetFactor = factor; state.offsetUnits = units;
    }
    public static void viewport(int x,int y,int w,int h) { if (!record(() -> viewport(x,y,w,h))) state.viewport = new int[]{x,y,w,h}; }
    public static void scissor(int x,int y,int w,int h) { if (!record(() -> scissor(x,y,w,h))) state.scissor = new int[]{x,y,w,h}; }
    public static void shadeModel(int mode) { if(!record(()->shadeModel(mode)))state.shadeModel = mode; }
    public static void colorMaterial(int face,int parameter) { if(!record(()->colorMaterial(face,parameter)))state.colorMaterial = parameter; }
    public static void hint(int target,int mode) { /* Vulkan pipeline compilation has no matching GL quality hint. */ }

    public static void clearColor(float r,float g,float b,float a) {
        if (record(() -> clearColor(r,g,b,a))) return;
        state.clearColor = new float[]{r,g,b,a};
    }
    public static void clearDepth(double value) { if(!record(()->clearDepth(value)))state.clearDepth = clamp((float)value); }
    public static void clear(int mask) {
        if (record(() -> clear(mask))) return;
        ensureFrame();
        RenderSystem.flushDraws();
        RenderSystem.renderer().clear(LegacyStateCodec.clear(mask, state.clearColor.clone(), state.clearDepth, state.depthWrite, state.colorMask,
            isEnabled(SCISSOR_TEST) ? state.scissor.clone() : null, RenderSystem.height()));
    }
    public static void finish() {
        if (RenderSystem.frameOpen()) finishFrame();
        RenderSystem.waitIdle();
    }
    public static void flush() { if (RenderSystem.frameOpen()) finishFrame(); }

    public static void pushAttrib(int mask) {
        if(record(()->pushAttrib(mask)))return;
        Map<Integer,Map<Integer,Float>> parameters=new HashMap<>();
        if((mask&0x40000)!=0)for(int id:boundTextures) {
            Texture texture=TEXTURES.get(id);
            if(texture!=null)parameters.put(id,parameterSnapshot(texture));
        }
        ATTRIBUTES.push(new Attributes(mask,state.copy(),boundTextures.clone(),activeTexture,matrixMode,parameters));
    }
    public static void popAttrib() {
        if(record(LegacyGlAdapter::popAttrib)||ATTRIBUTES.isEmpty())return;
        Attributes saved=ATTRIBUTES.pop();
        State old=saved.state;
        int mask=saved.mask;
        if((mask&1)!=0){state.color=old.color;state.normal=old.normal;state.uv=old.uv;}
        if((mask&4)!=0)state.lineWidth=old.lineWidth;
        if((mask&8)!=0){state.cullFace=old.cullFace;state.frontFace=old.frontFace;state.offsetFactor=old.offsetFactor;state.offsetUnits=old.offsetUnits;}
        if((mask&64)!=0){state.colorMaterial=old.colorMaterial;state.shadeModel=old.shadeModel;state.lightPosition=old.lightPosition;state.lightDiffuse=old.lightDiffuse;state.lightAmbient=old.lightAmbient;}
        if((mask&128)!=0){state.fogColor=old.fogColor;state.fogMode=old.fogMode;state.fogStart=old.fogStart;state.fogEnd=old.fogEnd;state.fogDensity=old.fogDensity;state.fogDistanceMode=old.fogDistanceMode;}
        if((mask&256)!=0){state.depthFunc=old.depthFunc;state.depthWrite=old.depthWrite;state.clearDepth=old.clearDepth;}
        if((mask&2048)!=0){state.viewport=old.viewport;state.minDepth=old.minDepth;state.maxDepth=old.maxDepth;}
        if((mask&4096)!=0)matrixMode=saved.matrixMode;
        if((mask&16384)!=0){state.clearColor=old.clearColor;state.colorMask=old.colorMask;state.alphaFunc=old.alphaFunc;state.alphaRef=old.alphaRef;state.blendSrc=old.blendSrc;state.blendDst=old.blendDst;state.blendColor=old.blendColor;}
        if((mask&0x40000)!=0) {
            activeTexture=saved.activeTexture;
            System.arraycopy(saved.boundTextures,0,boundTextures,0,boundTextures.length);
            saved.textureParameters.forEach((id,parameters)->{
                Texture texture=TEXTURES.get(id);
                if (texture != null) {
                    texture.parameters.clear();
                    parameters.forEach((name, value) -> setTextureParameter(id, name, value));
                }
            });
        }
        if((mask&0x80000)!=0)state.scissor=old.scissor;
        if((mask&8192)!=0)state.enabled=new HashSet<>(old.enabled);
        else {
            restoreEnabled(old,mask,8,CULL_FACE,POLYGON_OFFSET_FILL);
            restoreEnabled(old,mask,64,LIGHTING,LIGHT0,LIGHT1,COLOR_MATERIAL);
            restoreEnabled(old,mask,128,FOG);
            restoreEnabled(old,mask,256,DEPTH_TEST);
            restoreEnabled(old,mask,4096,NORMALIZE,32826);
            restoreEnabled(old,mask,16384,ALPHA_TEST,BLEND);
            restoreEnabled(old,mask,0x40000,TEXTURE_2D);
            restoreEnabled(old,mask,0x80000,SCISSOR_TEST);
        }
    }
    private static void restoreEnabled(State saved,int mask,int group,int... capabilities) {
        if((mask&group)==0)return;
        for(int capability:capabilities) {
            if(saved.enabled.contains(capability))state.enabled.add(capability);
            else state.enabled.remove(capability);
        }
    }
    private record Attributes(int mask,State state,int[] boundTextures,int activeTexture,int matrixMode,
                              Map<Integer,Map<Integer,Float>> textureParameters) { }
    private static int matrixKey() { return matrixMode == TEXTURE ? TEXTURE + activeTexture : matrixMode; }
    private static Deque<float[]> matrices() {
        return MATRIX_STACKS.computeIfAbsent(matrixKey(), key -> { var stack = new ArrayDeque<float[]>(); stack.push(identity()); return stack; });
    }
    private static float[] matrix() { return matrices().peek(); }
    private static float[] matrix(int key) {
        Deque<float[]> stack = MATRIX_STACKS.get(key);
        return stack == null ? identity() : stack.peek().clone();
    }
    public static void matrixMode(int mode) { if (!record(() -> matrixMode(mode))) matrixMode = mode; }
    public static void pushMatrix() { if (!record(LegacyGlAdapter::pushMatrix)) matrices().push(matrix().clone()); }
    public static void popMatrix() { if (!record(LegacyGlAdapter::popMatrix) && matrices().size() > 1) matrices().pop(); }
    public static void loadIdentity() { if (!record(LegacyGlAdapter::loadIdentity)) { matrices().pop(); matrices().push(identity()); } }
    public static void loadMatrixf(FloatBuffer values) { load(values, false); }
    public static void loadMatrix(FloatBuffer values) { loadMatrixf(values); }
    public static void multMatrixf(FloatBuffer values) { load(values, true); }
    public static void multMatrix(FloatBuffer values) { multMatrixf(values); }
    private static void load(FloatBuffer values,boolean multiply) {
        float[] copy = new float[16]; values.duplicate().get(copy);
        if (record(() -> load(FloatBuffer.wrap(copy),multiply))) return;
        float[] result = multiply ? multiply(matrix(),copy) : copy;
        matrices().pop(); matrices().push(result);
    }
    private static void mult(float[] transform) { float[] result = multiply(matrix(),transform); matrices().pop(); matrices().push(result); }
    public static void translatef(float x,float y,float z) {
        if (record(() -> translatef(x,y,z))) return;
        float[] transform = identity(); transform[12]=x; transform[13]=y; transform[14]=z; mult(transform);
    }
    public static void translated(double x,double y,double z) { translatef((float)x,(float)y,(float)z); }
    public static void scalef(float x,float y,float z) {
        if (record(() -> scalef(x,y,z))) return;
        float[] transform = identity(); transform[0]=x; transform[5]=y; transform[10]=z; mult(transform);
    }
    public static void scaled(double x,double y,double z) { scalef((float)x,(float)y,(float)z); }
    public static void rotatef(float angle,float x,float y,float z) {
        if (record(() -> rotatef(angle,x,y,z))) return;
        double length = Math.sqrt(x*x+y*y+z*z); if (length == 0) return;
        float nx=(float)(x/length),ny=(float)(y/length),nz=(float)(z/length);
        float c=(float)Math.cos(Math.toRadians(angle)), s=(float)Math.sin(Math.toRadians(angle)), t=1-c;
        mult(new float[]{t*nx*nx+c,t*nx*ny+s*nz,t*nx*nz-s*ny,0,t*nx*ny-s*nz,t*ny*ny+c,t*ny*nz+s*nx,0,
            t*nx*nz+s*ny,t*ny*nz-s*nx,t*nz*nz+c,0,0,0,0,1});
    }
    public static void rotated(double angle,double x,double y,double z) { rotatef((float)angle,(float)x,(float)y,(float)z); }
    public static void ortho(double left,double right,double bottom,double top,double near,double far) {
        if (record(() -> ortho(left,right,bottom,top,near,far))) return;
        float[] m=identity(); m[0]=(float)(2/(right-left)); m[5]=(float)(2/(top-bottom)); m[10]=(float)(-2/(far-near));
        m[12]=(float)(-(right+left)/(right-left)); m[13]=(float)(-(top+bottom)/(top-bottom)); m[14]=(float)(-(far+near)/(far-near)); mult(m);
    }
    public static void frustum(double left,double right,double bottom,double top,double near,double far) {
        if (record(() -> frustum(left,right,bottom,top,near,far))) return;
        float[] m=new float[16]; m[0]=(float)(2*near/(right-left)); m[5]=(float)(2*near/(top-bottom));
        m[8]=(float)((right+left)/(right-left)); m[9]=(float)((top+bottom)/(top-bottom));
        m[10]=(float)(-(far+near)/(far-near)); m[11]=-1; m[14]=(float)(-2*far*near/(far-near)); mult(m);
    }

    public static void fog(int name,FloatBuffer values) { fogfv(name, values); }
    public static void fogfv(int name,FloatBuffer values) {
        float[] copy = floats(values);
        if (record(() -> fogfv(name,FloatBuffer.wrap(copy)))) return;
        if (name == FOG_COLOR) state.fogColor = Arrays.copyOf(copy,4);
        else if (copy.length > 0) fogf(name,copy[0]);
    }
    public static void fogf(int name,float value) {
        if (record(() -> fogf(name,value))) return;
        switch(name) { case FOG_MODE -> state.fogMode=(int)value; case FOG_START -> state.fogStart=value;
            case FOG_END -> state.fogEnd=value; case FOG_DENSITY -> state.fogDensity=value; default -> { } }
    }
    public static void fogi(int name,int value) {
        if(name==34138){if(!record(()->fogi(name,value)))state.fogDistanceMode=value;}else fogf(name,value);
    }
    public static void light(int light,int name,FloatBuffer values) { lightfv(light,name,values); }
    public static void lightfv(int light,int name,FloatBuffer values) {
        float[] copy=Arrays.copyOf(floats(values),4);
        if (record(() -> lightfv(light,name,FloatBuffer.wrap(copy)))) return;
        int index=light-LIGHT0; if (index < 0 || index > 1) return;
        if (name == POSITION) state.lightPosition[index]=transform(matrix(MODELVIEW),copy);
        if (name == DIFFUSE) state.lightDiffuse[index]=copy;
    }
    public static void lightModel(int name,FloatBuffer values) { lightModelfv(name,values); }
    public static void lightModelfv(int name,FloatBuffer values) {
        float[] copy=Arrays.copyOf(floats(values),4);
        if (record(() -> lightModelfv(name,FloatBuffer.wrap(copy)))) return;
        if (name == LIGHT_MODEL_AMBIENT) state.lightAmbient=copy;
    }
    public static void material(int face,int name,FloatBuffer values) { /* Vertex color supplies Minecraft's diffuse material. */ }

    public static void activeTexture(int unit) { if(!record(()->activeTexture(unit)))activeTexture=Math.clamp(unit-TEXTURE0,0,boundTextures.length-1); }
    public static void clientActiveTexture(int unit) { clientTexture=Math.clamp(unit-TEXTURE0,0,7); }
    public static void multiTexCoord2f(int unit,float u,float v) { if (unit == TEXTURE0) texCoord2f(u,v); }
    public static void multiTexCoord2fARB(int unit,float u,float v) { multiTexCoord2f(unit,u,v); }
    public static void activeTextureARB(int unit) { activeTexture(unit); }
    public static void clientActiveTextureARB(int unit) { clientActiveTexture(unit); }
    public static int genTextures() { return nextId++; }
    public static void genTextures(IntBuffer names) { for(int i=names.position();i<names.limit();i++) names.put(i,genTextures()); }
    public static void bindTexture(int target,int id) {
        if (record(() -> bindTexture(target,id))) return;
        boundTextures[activeTexture]=id;
        if(id!=0) texture(id);
    }
    public static void deleteTextures(int id) {
        Texture texture = TEXTURES.remove(id);
        if (texture != null) texture.resource.close();
        for(int i=0;i<boundTextures.length;i++) if(boundTextures[i]==id) boundTextures[i]=0;
    }
    public static void deleteTextures(IntBuffer ids) { for(int i=ids.position();i<ids.limit();i++) deleteTextures(ids.get(i)); }
    public static boolean isTexture(int id) { return TEXTURES.containsKey(id); }
    private static Texture texture(int id) { return TEXTURES.computeIfAbsent(id, key -> new Texture()); }
    private static Texture boundTexture() { return texture(boundTextures[activeTexture]); }

    /** Bridge an inherited texture name once at the asset or render-entry boundary. */
    public static RenderTexture textureForId(int id) {
        if (id == 0) return null;
        Texture entry = texture(id);
        if (entry.resource.isClosed()) { TEXTURES.remove(id); return null; }
        return entry.resource.borrow();
    }

    /** A legacy name survives image replacement, just as a catalog asset does. */
    public static RenderTexture textureReference(java.util.function.IntSupplier name) {
        java.util.Objects.requireNonNull(name);
        return RenderTexture.assetReference(() -> {
            int id = name.getAsInt();
            Texture entry = TEXTURES.get(id);
            if (entry == null) throw new IllegalStateException("Legacy texture name is not allocated: " + id);
            return entry.resource;
        });
    }
    /** Install a borrowed native reference only for entering an inherited renderer callback. */
    public static int idForTexture(RenderTexture resource) {
        if (resource == null) return 0;
        RenderTexture owner = resource.storageOwner();
        var entries = TEXTURES.entrySet().iterator();
        while (entries.hasNext()) {
            var entry = entries.next();
            if (entry.getValue().resource.isClosed()) { entries.remove(); continue; }
            if (entry.getValue().resource.storageOwner() == owner
                && entry.getValue().resource.sampler().equals(resource.sampler())
                && entry.getValue().resource.baseLevel() == resource.baseLevel()
                && entry.getValue().resource.maxLevel() == resource.maxLevel()) return entry.getKey();
        }
        int id = genTextures();
        TEXTURES.put(id, new Texture(resource.borrow()));
        return id;
    }
    /** Attach a catalog-owned image to an existing vanilla name without taking its ownership. */
    public static void referenceTexture(int id, RenderTexture resource) {
        replaceTexture(id, resource.borrow());
    }
    /** Vanilla-generated images transfer ownership to their integer-name bridge. */
    public static void ownTexture(int id, RenderTexture resource) {
        TextureStore.adoptLegacyAllocation(resource);
        replaceTexture(id, resource);
    }
    private static void replaceTexture(int id, RenderTexture resource) {
        if (id <= 0) throw new IllegalArgumentException("Texture name must be positive");
        Texture previous = TEXTURES.put(id, new Texture(resource));
        if (previous != null) previous.resource.close();
    }
    public static RenderTexture currentTexture() { return textureForId(currentTextureId()); }
    public static Size textureSize(int id, int level) { return texture(id).resource.size(level); }
    public static long textureSizeInBytes(int id) { return texture(id).resource.sizeInBytes(); }
    public static float textureParameter(int id, int name) {
        Texture texture = texture(id);
        SamplerState sampler = texture.resource.sampler();
        return switch (name) {
            case TEXTURE_MIN_FILTER -> encodeMinFilter(sampler);
            case TEXTURE_MAG_FILTER -> encodeFilter(sampler.mag());
            case TEXTURE_WRAP_S -> encodeAddress(sampler.wrapU());
            case TEXTURE_WRAP_T -> encodeAddress(sampler.wrapV());
            case TEXTURE_BASE_LEVEL -> texture.resource.baseLevel();
            case TEXTURE_MAX_LEVEL -> texture.resource.maxLevel();
            default -> texture.parameters.getOrDefault(name, 0f);
        };
    }
    public static void setTextureParameter(int id, int name, float value) {
        Texture texture = texture(id);
        texture.parameters.put(name, value);
        RenderTexture resource = texture.resource;
        SamplerState old = resource.sampler();
        int symbol = Math.round(value);
        switch (name) {
            case TEXTURE_MIN_FILTER -> resource.setSampler(new SamplerState(decodeFilter(symbol), old.mag(), decodeMipmap(symbol), old.wrapU(), old.wrapV()));
            case TEXTURE_MAG_FILTER -> resource.setSampler(new SamplerState(old.min(), decodeFilter(symbol), old.mipmap(), old.wrapU(), old.wrapV()));
            case TEXTURE_WRAP_S -> resource.setSampler(new SamplerState(old.min(), old.mag(), old.mipmap(), decodeAddress(symbol), old.wrapV()));
            case TEXTURE_WRAP_T -> resource.setSampler(new SamplerState(old.min(), old.mag(), old.mipmap(), old.wrapU(), decodeAddress(symbol)));
            case TEXTURE_BASE_LEVEL -> resource.setMipRange(symbol, resource.maxLevel());
            case TEXTURE_MAX_LEVEL -> resource.setMipRange(resource.baseLevel(), symbol);
            default -> { }
        }
    }
    private static SamplerState.Filter decodeFilter(int symbol) {
        return symbol == LINEAR || symbol == LINEAR_MIPMAP_NEAREST || symbol == LINEAR_MIPMAP_LINEAR
            ? SamplerState.Filter.LINEAR : SamplerState.Filter.NEAREST;
    }
    private static SamplerState.MipmapMode decodeMipmap(int symbol) {
        return switch (symbol) {
            case NEAREST_MIPMAP_NEAREST, LINEAR_MIPMAP_NEAREST -> SamplerState.MipmapMode.NEAREST;
            case NEAREST_MIPMAP_LINEAR, LINEAR_MIPMAP_LINEAR -> SamplerState.MipmapMode.LINEAR;
            default -> SamplerState.MipmapMode.NONE;
        };
    }
    private static int encodeFilter(SamplerState.Filter filter) { return filter == SamplerState.Filter.LINEAR ? LINEAR : NEAREST; }
    private static int encodeMinFilter(SamplerState sampler) {
        return switch (sampler.mipmap()) {
            case NONE -> encodeFilter(sampler.min());
            case NEAREST -> sampler.min() == SamplerState.Filter.LINEAR ? LINEAR_MIPMAP_NEAREST : NEAREST_MIPMAP_NEAREST;
            case LINEAR -> sampler.min() == SamplerState.Filter.LINEAR ? LINEAR_MIPMAP_LINEAR : NEAREST_MIPMAP_LINEAR;
        };
    }
    private static SamplerState.AddressMode decodeAddress(int symbol) {
        return switch (symbol) {
            case 33648 -> SamplerState.AddressMode.MIRRORED_REPEAT;
            case 33069 -> SamplerState.AddressMode.CLAMP_TO_BORDER;
            case CLAMP_TO_EDGE, 10496 -> SamplerState.AddressMode.CLAMP_TO_EDGE;
            default -> SamplerState.AddressMode.REPEAT;
        };
    }
    private static int encodeAddress(SamplerState.AddressMode mode) {
        return switch (mode) {
            case REPEAT -> REPEAT;
            case MIRRORED_REPEAT -> 33648;
            case CLAMP_TO_EDGE -> CLAMP_TO_EDGE;
            case CLAMP_TO_BORDER -> 33069;
        };
    }
    private static Map<Integer, Float> parameterSnapshot(Texture texture) {
        Map<Integer, Float> parameters = new HashMap<>(texture.parameters);
        SamplerState sampler = texture.resource.sampler();
        parameters.put(TEXTURE_MIN_FILTER, (float) encodeMinFilter(sampler));
        parameters.put(TEXTURE_MAG_FILTER, (float) encodeFilter(sampler.mag()));
        parameters.put(TEXTURE_WRAP_S, (float) encodeAddress(sampler.wrapU()));
        parameters.put(TEXTURE_WRAP_T, (float) encodeAddress(sampler.wrapV()));
        parameters.put(TEXTURE_BASE_LEVEL, (float) texture.resource.baseLevel());
        parameters.put(TEXTURE_MAX_LEVEL, (float) texture.resource.maxLevel());
        return parameters;
    }
    public static void allocateTexture2D(int id, int width, int height, int mipLevels) { TextureStore.allocateLegacy(texture(id).resource, new TextureDesc(width, height, mipLevels)); }
    public static void uploadTextureRegion(int id, int level, int x, int y, int width, int height, ByteBuffer rgba) {
        texture(id).resource.upload(level, x, y, width, height, rgba);
    }
    public static void uploadTextureRegion(int id, int level, int x, int y, int width, int height, IntBuffer rgba) {
        texture(id).resource.upload(level, x, y, width, height, rgba);
    }
    public static void texParameteri(int target,int name,int value) { texParameterf(target,name,value); }
    public static void texParameterf(int target,int name,float value) {
        if(record(()->texParameterf(target,name,value))) return;
        setTextureParameter(boundTextures[activeTexture], name, value);
    }
    public static int getTexParameteri(int target,int name) { return Math.round(getTexParameterf(target, name)); }
    public static float getTexParameterf(int target,int name) { return textureParameter(boundTextures[activeTexture], name); }
    public static int getTexLevelParameteri(int target,int level,int name) {
        Size size = boundTexture().resource.size(level);
        return size.w == 0 || size.h == 0 ? 0 : switch(name) {
            case TEXTURE_WIDTH -> size.w;
            case TEXTURE_HEIGHT -> size.h;
            default -> RGBA8;
        };
    }
    public static void texImage2D(int target,int level,int internal,int w,int h,int border,int format,int type,ByteBuffer pixels) {
        textureImage(level,w,h,format,type,pixels==null?null:bytes(pixels));
    }
    public static void texImage2D(int target,int level,int internal,int w,int h,int border,int format,int type,IntBuffer pixels) {
        textureImage(level,w,h,format,type,pixels==null?null:bytes(pixels));
    }
    private static void textureImage(int level,int w,int h,int format,int type,ByteBuffer pixels) {
        if(w<0||h<0)throw new IllegalArgumentException("Negative texture size");
        ByteBuffer rgba=decodePixels(w,h,format,type,pixels);
        if(compiling!=null&&!executing) {
            ByteBuffer captured=bytes(rgba);
            if(record(()->storeTextureImage(level,w,h,bytes(captured))))return;
        }
        storeTextureImage(level,w,h,rgba);
    }
    private static void storeTextureImage(int level,int width,int height,ByteBuffer rgba) {
        TextureStore.defineLegacyLevel(boundTexture().resource, level, width, height, rgba);
    }
    public static void texSubImage2D(int target,int level,int x,int y,int w,int h,int format,int type,ByteBuffer pixels) {
        textureSubImage(level,x,y,w,h,format,type,bytes(pixels));
    }
    public static void texSubImage2D(int target,int level,int x,int y,int w,int h,int format,int type,IntBuffer pixels) {
        textureSubImage(level,x,y,w,h,format,type,bytes(pixels));
    }
    private static void textureSubImage(int level,int x,int y,int w,int h,int format,int type,ByteBuffer pixels) {
        ByteBuffer rgba=decodePixels(w,h,format,type,pixels);
        if(record(()->storeTextureSubImage(level,x,y,w,h,rgba)))return;
        storeTextureSubImage(level,x,y,w,h,rgba);
    }
    private static void storeTextureSubImage(int level,int x,int y,int width,int height,ByteBuffer rgba) {
        boundTexture().resource.upload(level, x, y, width, height, rgba);
    }
    public static void texStorage2D(int target,int levels,int format,int w,int h) {
        for(int i=0;i<levels;i++) textureImage(i,Math.max(1,w>>i),Math.max(1,h>>i),RGBA,UNSIGNED_BYTE,null);
    }
    private static ByteBuffer decodePixels(int w,int h,int format,int type,ByteBuffer input) {
        ByteBuffer rgba=BufferUtils.createByteBuffer(Math.multiplyExact(Math.multiplyExact(w,h),4));
        if(input==null) return rgba;
        int components=switch(format){case RGB,0x80E0->3;case LUMINANCE,ALPHA,DEPTH_COMPONENT->1;case LUMINANCE_ALPHA->2;default->4;};
        int rowLength=PIXEL_STORE.getOrDefault(UNPACK_ROW_LENGTH,0); if(rowLength==0)rowLength=w;
        int alignment=PIXEL_STORE.getOrDefault(UNPACK_ALIGNMENT,4);
        int rowBytes=Math.multiplyExact(rowLength,components);
        int stride=(rowBytes+alignment-1)/alignment*alignment;
        int skipRows=PIXEL_STORE.getOrDefault(UNPACK_SKIP_ROWS,0),skipPixels=PIXEL_STORE.getOrDefault(UNPACK_SKIP_PIXELS,0);
        if(type!=UNSIGNED_BYTE)throw new UnsupportedOperationException("Vulkan texture pixel type: "+type);
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){
            int p=input.position()+(y+skipRows)*stride+(x+skipPixels)*components;
            int r=input.get(p)&255,g=components>1?input.get(p+1)&255:r,b=components>2?input.get(p+2)&255:r,a=components==4?input.get(p+3)&255:255;
            if(format==ALPHA){a=r;r=g=b=255;}else if(format==LUMINANCE_ALPHA){a=g;g=b=r;}
            else if(format==0x80E1||format==0x80E0){int t=r;r=b;b=t;}
            rgba.put((byte)r).put((byte)g).put((byte)b).put((byte)a);
        }
        return rgba.flip();
    }
    public static void pixelStorei(int name,int value) { PIXEL_STORE.put(name,value); }

    /** Vanilla's constructor still reserves unused VBO names; tessellation itself is native. */
    public static int genBuffers() { return nextId++; }
    public static void genBuffers(IntBuffer ids) { for (int i = ids.position(); i < ids.limit(); i++) ids.put(i, genBuffers()); }
    public static void genBuffersARB(IntBuffer ids) { genBuffers(ids); }

    private static void submit(ByteBuffer vertices,int mode,VulkanRenderer.Mesh retained){
        submit(vertices,mode,retained,false);
    }
    private static void submit(ByteBuffer vertices,int mode,VulkanRenderer.Mesh retained,boolean currentColor){
        ensureFrame();
        RenderSystem.flushDraws();
        var prepared = TextureStore.prepare(currentTexture());
        var texture=prepared.nativeTexture();
        var mesh=retained==null?LegacyStateCodec.stream(RenderSystem.renderer(),vertices,mode):retained;
        try { RenderSystem.renderer().draw(mesh,texture,drawState(currentColor)); }
        finally { prepared.close(); }
    }

    /** Vanilla tessellation boundary. Writable scratch records receive missing attribute defaults in place. */
    public static void submitVertices(ByteBuffer vertices, int mode, boolean hasColor, boolean hasTexture, boolean hasNormal) {
        if (!vertices.hasRemaining()) return;
        if (vertices.remaining() % 32 != 0) throw new IllegalArgumentException("Incomplete native vertex records");
        if (compiling != null && !executing) {
            var captured = new CapturedVertices(bytes(vertices), mode, hasColor, hasTexture, hasNormal, compileId);
            if (record(captured::draw)) return;
            captured.draw();
        } else submitVertexData(vertices, mode, hasColor, hasTexture, hasNormal);
    }

    private static void submitVertexData(ByteBuffer vertices, int mode, boolean hasColor, boolean hasTexture, boolean hasNormal) {
        submit(nativeVertexData(vertices, hasColor, hasTexture, hasNormal), mode, null, !hasColor);
    }

    private static ByteBuffer nativeVertexData(ByteBuffer vertices, boolean hasColor, boolean hasTexture, boolean hasNormal) {
        ByteBuffer nativeVertices = vertices;
        if (!hasColor || !hasTexture || !hasNormal) {
            nativeVertices = (vertices.isReadOnly() ? bytes(vertices) : vertices.slice()).order(ByteOrder.nativeOrder());
            int normal = packedCurrentNormal();
            for (int offset = 0; offset < nativeVertices.remaining(); offset += 32) {
                if (!hasColor) nativeVertices.putInt(offset + 20, -1);
                if (!hasTexture) {
                    nativeVertices.putFloat(offset + 12, state.uv[0]);
                    nativeVertices.putFloat(offset + 16, state.uv[1]);
                }
                if (!hasNormal) {
                    nativeVertices.put(offset + 24, (byte) normal);
                    nativeVertices.put(offset + 25, (byte) (normal >>> 8));
                    nativeVertices.put(offset + 26, (byte) (normal >>> 16));
                }
            }
        }
        return nativeVertices;
    }

    private static int packedCurrentNormal() {
        int result = 0;
        for (int component = 0; component < 3; component++) {
            result |= (Math.round(Math.clamp(state.normal[component], -1, 1) * 127) & 255) << (component * 8);
        }
        return result;
    }

    /** A display-list draw keeps its native allocation until missing attribute defaults change. */
    private static final class CapturedVertices {
        private final ByteBuffer vertices;
        private final int mode;
        private final boolean hasColor, hasTexture, hasNormal;
        private final int list;
        private VulkanRenderer.Mesh mesh;
        private VertexDefaults defaults;

        private CapturedVertices(ByteBuffer vertices, int mode, boolean hasColor, boolean hasTexture, boolean hasNormal, int list) {
            this.vertices = vertices;
            this.mode = mode;
            this.hasColor = hasColor;
            this.hasTexture = hasTexture;
            this.hasNormal = hasNormal;
            this.list = list;
        }

        private void draw() {
            ensureFrame();
            var current = new VertexDefaults(
                this.hasTexture ? 0 : Float.floatToRawIntBits(state.uv[0]),
                this.hasTexture ? 0 : Float.floatToRawIntBits(state.uv[1]),
                this.hasNormal ? 0 : packedCurrentNormal());
            if (this.mesh == null || !current.equals(this.defaults)) {
                var replacement = LegacyStateCodec.upload(RenderSystem.renderer(),
                    nativeVertexData(this.vertices, this.hasColor, this.hasTexture, this.hasNormal), this.mode);
                var resources = LIST_MESHES.computeIfAbsent(this.list, ignored -> new ArrayList<>());
                if (this.mesh != null) {
                    resources.remove(this.mesh);
                    RETIRED.add(this.mesh);
                }
                this.mesh = replacement;
                this.defaults = current;
                resources.add(replacement);
            }
            submit(null, this.mode, this.mesh, !this.hasColor);
        }
    }

    private record VertexDefaults(int u, int v, int normal) { }
    public static float[] modelView() { return matrix(MODELVIEW); }
    public static float[] currentTint() { return state.color.clone(); }
    public static int currentTextureId() { return isEnabled(TEXTURE_2D) ? boundTextures[0] : 0; }
    public static DrawParameters captureDrawState(boolean currentColor) { return drawState(currentColor); }
    private static DrawParameters drawState(boolean currentColor){
        return captureDrawState(matrix(MODELVIEW), currentColor?state.color.clone():new float[]{1,1,1,1});
    }
    public static DrawParameters captureDrawState(float[] model, float[] tint){
        var captured = LegacyStateCodec.fromLegacy(matrix(PROJECTION),model.clone(),tint.clone(),state.fogColor.clone(),
            isEnabled(FOG)?state.fogMode:0,state.fogStart,state.fogEnd,state.fogDensity,
            isEnabled(ALPHA_TEST),state.alphaFunc,state.alphaRef,isEnabled(DEPTH_TEST),state.depthWrite,state.depthFunc,
            isEnabled(BLEND),state.blendSrc,state.blendDst,isEnabled(CULL_FACE),state.cullFace,state.frontFace,state.colorMask,
            state.viewport.clone(),isEnabled(SCISSOR_TEST)?state.scissor.clone():null,state.minDepth,state.maxDepth,state.lineWidth,
            isEnabled(POLYGON_OFFSET_FILL)?state.offsetFactor:0,isEnabled(POLYGON_OFFSET_FILL)?state.offsetUnits:0,
            isEnabled(LIGHTING),matrix(TEXTURE),state.lightPosition[0].clone(),state.lightPosition[1].clone(),
            state.lightDiffuse[0].clone(),state.lightDiffuse[1].clone(),state.lightAmbient.clone(),isEnabled(COLOR_MATERIAL),
            (isEnabled(LIGHT0)?1:0)|(isEnabled(LIGHT1)?2:0),state.blendColor.clone(),state.colorMaterial,state.fogDistanceMode,RenderSystem.height());
        if (!RenderScene.hasWorld() || RenderSystem.phase() == RenderPhase.UI || RenderSystem.phase() == RenderPhase.HANDS) return captured;
        var world = RenderScene.world();
        if (world.environment().fog().x() == 0) return captured;
        if (atmosphereViewSource != world.view()) {
            double[] inverse = RenderMath.inverse(world.view());
            float[] values = new float[16];
            for (int i = 0; i < values.length; i++) values[i] = (float) inverse[i];
            atmosphereInverseView = new Matrix4(values);
            atmosphereViewSource = world.view();
        }
        // Legacy model-view includes the camera. Factor it out so atmosphere uses real
        // camera-relative positions while the clip-space transform remains identical.
        var pose = atmosphereInverseView.multiply(captured.view().view().multiply(captured.object().model()));
        return captured.withView(captured.view().withView(world.view()).withEnvironment(world.environment()))
            .withObject(captured.object().withModel(pose));
    }

    /** Publish the authoritative native view for inherited rendering only. */
    public static void installView(FrameView view) {
        float[] projection = view.projection().copyArray();
        for (int column = 0; column < 4; column++) {
            int at = column * 4;
            projection[at + 1] = -projection[at + 1];
            projection[at + 2] = 2 * projection[at + 2] - projection[at + 3];
        }
        setMatrix(PROJECTION, projection);
        setMatrix(MODELVIEW, view.view().copyArray());
        matrixMode = MODELVIEW;
    }
    public static void installFog(FogState fog) {
        state.fogMode = switch (fog.mode()) { case LINEAR -> LINEAR; case EXPONENTIAL -> EXP; case EXPONENTIAL_SQUARED -> 2049; default -> 0; };
        state.fogDistanceMode = switch(fog.distance()) { case RADIAL_VERTEX -> -1; case RADIAL_FRAGMENT -> 34139; default -> 34140; };
        state.fogColor = fog.color().copyArray(); state.fogStart = fog.start(); state.fogEnd = fog.end(); state.fogDensity = fog.density();
        state.normal = new float[]{0, -1, 0}; state.color = new float[]{1, 1, 1, 1};
        state.colorMaterial = AMBIENT; capability(COLOR_MATERIAL, true);
    }

    /** Establish the native UI baseline for inherited screen and widget drawing. */
    public static void installUi(DrawParameters parameters) {
        // Inherited screens expect depth writes and an alpha cutoff for transparent blits.
        // Native UI draws retain their explicit depth-free raster state.
        installDrawParameters(parameters.withMaterial(MaterialRecipe.UI_CUTOUT.material())
            .withRaster(parameters.raster().withDepth(true, true)));
        capability(TEXTURE_2D, true);
    }

    private static void installDrawParameters(DrawParameters parameters) {
        var view = parameters.view(); var object = parameters.object();
        var material = parameters.material(); var raster = parameters.raster();
        float[] projection = view.projection().copyArray();
        for (int column = 0; column < 4; column++) {
            int at = column * 4;
            projection[at + 1] = -projection[at + 1];
            projection[at + 2] = 2 * projection[at + 2] - projection[at + 3];
        }
        setMatrix(PROJECTION, projection);
        setMatrix(MODELVIEW, view.view().multiply(object.model()).copyArray());
        activeTexture = 0;
        setMatrix(TEXTURE, object.textureTransform().copyArray());
        matrixMode = MODELVIEW;
        state.color = object.tint().copyArray();
        state.alphaFunc = 512 + material.alphaCompare().ordinal(); state.alphaRef = material.alphaReference();
        capability(ALPHA_TEST, material.alphaCompare() != CompareOp.ALWAYS);
        capability(LIGHTING, material.lighting());
        capability(COLOR_MATERIAL, material.colorMaterial() != ColorMaterial.NONE);
        state.colorMaterial = switch (material.colorMaterial()) {
            case AMBIENT -> AMBIENT; case DIFFUSE -> DIFFUSE; case EMISSION -> 5632;
            default -> AMBIENT_AND_DIFFUSE;
        };
        var lights = view.lights();
        capability(LIGHT0, lights.first().enabled()); capability(LIGHT1, lights.second().enabled());
        state.lightPosition = new float[][]{vectorArray(lights.first().position()), vectorArray(lights.second().position())};
        state.lightDiffuse = new float[][]{lights.first().diffuse().copyArray(), lights.second().diffuse().copyArray()};
        state.lightAmbient = lights.ambient().copyArray();
        var fog = view.fog();
        capability(FOG, fog.mode() != FogMode.NONE);
        state.fogMode = switch (fog.mode()) { case LINEAR -> LINEAR; case EXPONENTIAL -> EXP; case EXPONENTIAL_SQUARED -> 2049; default -> 0; };
        state.fogDistanceMode = switch(fog.distance()) { case RADIAL_VERTEX -> -1; case RADIAL_FRAGMENT -> 34139; default -> 34140; };
        state.fogColor = fog.color().copyArray(); state.fogStart = fog.start(); state.fogEnd = fog.end(); state.fogDensity = fog.density();
        capability(DEPTH_TEST, raster.depthTest()); state.depthWrite = raster.depthWrite(); state.depthFunc = 512 + raster.depthCompare().ordinal();
        capability(BLEND, raster.blend().enabled());
        state.blendSrc = legacyBlend(raster.blend().source()); state.blendDst = legacyBlend(raster.blend().destination());
        state.blendColor = raster.blend().constants().copyArray();
        capability(CULL_FACE, raster.cull() != CullMode.NONE);
        state.cullFace = switch(raster.cull()) { case FRONT -> FRONT; case FRONT_AND_BACK -> 1032; default -> BACK; };
        state.frontFace = raster.winding() == Winding.CLOCKWISE ? CW : CCW;
        state.colorMask = raster.colorMask().bits();
        if (raster.viewport() != null) state.viewport = legacyRect(raster.viewport());
        capability(SCISSOR_TEST, raster.scissor() != null);
        if (raster.scissor() != null) state.scissor = legacyRect(raster.scissor());
        state.minDepth = raster.minDepth(); state.maxDepth = raster.maxDepth(); state.lineWidth = raster.lineWidth();
        capability(POLYGON_OFFSET_FILL, raster.depthBias().enabled());
        state.offsetFactor = raster.depthBias().factor(); state.offsetUnits = raster.depthBias().units();
    }

    /** Installs an explicit draw only while invoking inherited Minecraft code. */
    public static void withDrawParameters(DrawParameters parameters, RenderTexture texture, Runnable callback) {
        State previous = state;
        var previousAttributes = new ArrayDeque<>(ATTRIBUTES);
        int previousMode = matrixMode, previousActive = activeTexture;
        int[] previousTextures = boundTextures.clone();
        var previousMatrices = new HashMap<Integer, Deque<float[]>>();
        for (var entry : MATRIX_STACKS.entrySet()) previousMatrices.put(entry.getKey(), new ArrayDeque<>(entry.getValue()));
        state = state.copy();
        try {
            installDrawParameters(parameters);
            capability(TEXTURE_2D, texture != null);
            boundTextures[0] = idForTexture(texture);
            callback.run();
        } finally {
            state = previous; matrixMode = previousMode; activeTexture = previousActive;
            ATTRIBUTES.clear(); ATTRIBUTES.addAll(previousAttributes);
            System.arraycopy(previousTextures, 0, boundTextures, 0, boundTextures.length);
            MATRIX_STACKS.clear(); MATRIX_STACKS.putAll(previousMatrices);
        }
    }

    private static void capability(int capability, boolean enabled) {
        if (enabled) state.enabled.add(capability); else state.enabled.remove(capability);
    }
    private static void setMatrix(int key, float[] matrix) {
        var stack = new ArrayDeque<float[]>(); stack.push(matrix); MATRIX_STACKS.put(key, stack);
    }
    private static float[] vectorArray(Vector4 v) { return new float[]{v.x(), v.y(), v.z(), v.w()}; }
    private static int[] legacyRect(Rect rectangle) {
        return new int[]{rectangle.x(), RenderSystem.height() - rectangle.y() - rectangle.height(), rectangle.width(), rectangle.height()};
    }
    private static int legacyBlend(BlendFactor factor) {
        return switch(factor) {
            case ZERO -> ZERO; case ONE -> ONE; case SRC_COLOR -> 768; case ONE_MINUS_SRC_COLOR -> ONE_MINUS_SRC_COLOR;
            case DST_COLOR -> 774; case ONE_MINUS_DST_COLOR -> ONE_MINUS_DST_COLOR; case SRC_ALPHA -> SRC_ALPHA;
            case ONE_MINUS_SRC_ALPHA -> ONE_MINUS_SRC_ALPHA; case DST_ALPHA -> 772; case ONE_MINUS_DST_ALPHA -> 773;
            case CONSTANT_COLOR -> 32769; case ONE_MINUS_CONSTANT_COLOR -> 32770; case CONSTANT_ALPHA -> 32771;
            case ONE_MINUS_CONSTANT_ALPHA -> 32772; case SRC_ALPHA_SATURATE -> 776;
        };
    }

    public static int getError(){int previous=error;error=NO_ERROR;return previous;}
    public static String getString(int name){return switch(name){case VENDOR->"AdventureCraft Vulkan";case RENDERER->RenderSystem.deviceName();case VERSION->"Vulkan legacy state translator";case EXTENSIONS->"";default->"";};}
    public static int getInteger(int name){return switch(name){
        case MATRIX_MODE->matrixMode;case TEXTURE_BINDING_2D->boundTextures[activeTexture];case ARRAY_BUFFER_BINDING->0;
        case ELEMENT_ARRAY_BUFFER_BINDING->0;case CURRENT_PROGRAM->0;case ACTIVE_TEXTURE->TEXTURE0+activeTexture;
        case CLIENT_ACTIVE_TEXTURE->TEXTURE0+clientTexture;case MAX_TEXTURE_SIZE->16384;case MAX_TEXTURE_UNITS->8;
        case DEPTH_FUNC->state.depthFunc;case ALPHA_TEST_FUNC->state.alphaFunc;case FOG_MODE->state.fogMode;
        case CULL_FACE_MODE->state.cullFace;case FRONT_FACE->state.frontFace;
        default->PIXEL_STORE.getOrDefault(name,0);
    };}
    public static void getInteger(int name,IntBuffer out){getIntegerv(name,out);}
    public static void getIntegerv(int name,IntBuffer out){int[]values=name==VIEWPORT?state.viewport:name==SCISSOR_BOX?state.scissor:new int[]{getInteger(name)};for(int i=0;i<values.length&&i<out.remaining();i++)out.put(out.position()+i,values[i]);}
    public static void getIntegerv(int name,int[]out){var temp=IntBuffer.wrap(out);getIntegerv(name,temp);}
    public static void getFloat(int name,FloatBuffer out){getFloatv(name,out);}
    public static void getFloatv(int name,FloatBuffer out){float[]values=switch(name){case MODELVIEW_MATRIX->matrix(MODELVIEW);case PROJECTION_MATRIX->matrix(PROJECTION);case TEXTURE_MATRIX->matrix(TEXTURE+activeTexture);case CURRENT_COLOR->state.color;case FOG_COLOR->state.fogColor;case DEPTH_RANGE->new float[]{state.minDepth,state.maxDepth};default->new float[]{getFloat(name)};};for(int i=0;i<values.length&&i<out.remaining();i++)out.put(out.position()+i,values[i]);}
    public static void getFloatv(int name,float[]out){getFloatv(name,FloatBuffer.wrap(out));}
    public static float getFloat(int name){return switch(name){case FOG_START->state.fogStart;case FOG_END->state.fogEnd;case FOG_DENSITY->state.fogDensity;case ALPHA_TEST_REF->state.alphaRef;case LINE_WIDTH->state.lineWidth;case 0x84FF->1;default->getInteger(name);};}
    public static boolean getBoolean(int name){return name==DEPTH_WRITEMASK?state.depthWrite:isEnabled(name);}
    public static void getBooleanv(int name,ByteBuffer out){if(name==COLOR_WRITEMASK){for(int i=0;i<4;i++)out.put(out.position()+i,(byte)((state.colorMask>>i)&1));}else out.put(out.position(),(byte)(getBoolean(name)?1:0));}
    public static void readBuffer(int buffer){ }
    public static void drawBuffer(int buffer){ }
    public static void readPixels(int x,int y,int w,int h,int format,int type,ByteBuffer output) {
        if(w==0||h==0)return;
        if(w<0||h<0||x<0||y<0||x+w>RenderSystem.width()||y+h>RenderSystem.height())
            throw new IllegalArgumentException("Readback rectangle exceeds the framebuffer");
        if(type!=UNSIGNED_BYTE&&type!=FLOAT)
            throw new UnsupportedOperationException("Vulkan readback pixel type: "+type);
        finishFrame();
        var image=RenderSystem.renderer().readbackFrame();
        int components=pixelComponents(format);
        int componentBytes=type==FLOAT?4:1;
        int alignment=PIXEL_STORE.getOrDefault(PACK_ALIGNMENT,4);
        int rowLength=PIXEL_STORE.getOrDefault(PACK_ROW_LENGTH,0);
        if(rowLength==0)rowLength=w;
        int rowBytes=Math.multiplyExact(rowLength,components*componentBytes);
        int stride=Math.ceilDiv(rowBytes,alignment)*alignment;
        int skipRows=PIXEL_STORE.getOrDefault(PACK_SKIP_ROWS,0);
        int skipPixels=PIXEL_STORE.getOrDefault(PACK_SKIP_PIXELS,0);
        ByteBuffer target=output.duplicate().order(ByteOrder.nativeOrder());
        for(int row=0;row<h;row++)for(int col=0;col<w;col++) {
            int src=((image.height()-1-(y+row))*image.width()+x+col)*4;
            int dst=target.position()+(row+skipRows)*stride+(col+skipPixels)*components*componentBytes;
            for(int c=0;c<components;c++) {
                int channel=format==0x80E1?(c==0?2:c==2?0:c):format==ALPHA?3:c;
                float value=format==DEPTH_COMPONENT
                    ?image.depth().order(ByteOrder.nativeOrder()).getFloat(src)
                    :(image.rgba().get(src+channel)&255)/255f;
                if(type==FLOAT)target.putFloat(dst+c*4,value);
                else target.put(dst+c,(byte)Math.round(clamp(value)*255));
            }
        }
    }
    private static int pixelComponents(int format) {
        return switch(format){case RGB,0x80E0->3;case DEPTH_COMPONENT,ALPHA,LUMINANCE->1;case LUMINANCE_ALPHA->2;default->4;};
    }
    public static void readPixels(int x,int y,int w,int h,int format,int type,FloatBuffer output) {
        if(type!=FLOAT)throw new IllegalArgumentException("Float readback requires FLOAT pixels");
        int components=pixelComponents(format);
        int alignment=PIXEL_STORE.getOrDefault(PACK_ALIGNMENT,4);
        int rowLength=PIXEL_STORE.getOrDefault(PACK_ROW_LENGTH,0);
        if(rowLength==0)rowLength=w;
        int stride=Math.ceilDiv(Math.multiplyExact(rowLength,components*4),alignment)*alignment;
        int bytes=Math.addExact(Math.multiplyExact(h+PIXEL_STORE.getOrDefault(PACK_SKIP_ROWS,0),stride),
            Math.multiplyExact(PIXEL_STORE.getOrDefault(PACK_SKIP_PIXELS,0),components*4));
        var pixels=BufferUtils.createByteBuffer(bytes);
        readPixels(x,y,w,h,format,type,pixels);
        var values=pixels.asFloatBuffer();
        if(values.remaining()>output.remaining())values.limit(output.remaining());
        output.duplicate().put(values);
    }
    public static void getTexImage(int target,int level,int format,int type,ByteBuffer output) {
        output.duplicate().put(boundTexture().resource.readLevel(level));
    }
    public static void genQueriesARB(IntBuffer ids){for(int i=ids.position();i<ids.limit();i++)ids.put(i,nextId++);}
    public static void getQueryObjectuivARB(int id,int name,IntBuffer result){result.put(result.position(),1);}
    public static void getQueryObjectuARB(int id,int name,IntBuffer result){getQueryObjectuivARB(id,name,result);}

    private static float clamp(float value){return Math.clamp(value,0,1);}
    private static float[] floats(FloatBuffer buffer){float[]data=new float[buffer.remaining()];buffer.duplicate().get(data);return data;}
    private static ByteBuffer bytes(Buffer source){
        int unit=source instanceof ByteBuffer?1:source instanceof ShortBuffer?2:source instanceof DoubleBuffer||source instanceof LongBuffer?8:4;
        ByteBuffer out=BufferUtils.createByteBuffer(Math.multiplyExact(source.remaining(),unit));
        if(source instanceof ByteBuffer b)out.put(b.duplicate());else if(source instanceof IntBuffer b)out.asIntBuffer().put(b.duplicate());
        else if(source instanceof FloatBuffer b)out.asFloatBuffer().put(b.duplicate());else if(source instanceof ShortBuffer b)out.asShortBuffer().put(b.duplicate());
        else if(source instanceof DoubleBuffer b)out.asDoubleBuffer().put(b.duplicate());else throw new IllegalArgumentException("Unsupported data buffer "+source);
        out.position(0);return out;
    }
    private static void vertex(ByteBuffer out,float x,float y,float z,float u,float v,float[]color,float[]normal){
        out.putFloat(x).putFloat(y).putFloat(z).putFloat(u).putFloat(v);
        for(int i=0;i<4;i++)out.put((byte)Math.round(clamp(color[i])*255));
        for(int i=0;i<3;i++)out.put((byte)Math.round(Math.clamp(normal[i],-1,1)*127));
        out.put((byte)0).putInt(-1);
    }
    private static float[] identity(){float[]m=new float[16];m[0]=m[5]=m[10]=m[15]=1;return m;}
    private static float[] multiply(float[]a,float[]b){float[]r=new float[16];for(int c=0;c<4;c++)for(int row=0;row<4;row++)for(int k=0;k<4;k++)r[c*4+row]+=a[k*4+row]*b[c*4+k];return r;}
    private static float[] transform(float[]matrix,float[]v){float[]r=new float[4];for(int row=0;row<4;row++)for(int k=0;k<4;k++)r[row]+=matrix[k*4+row]*v[k];return r;}

    /** The ABI owns only integer names and parameter encodings, never image storage. */
    private static final class Texture {
        final RenderTexture resource;
        final Map<Integer, Float> parameters = new HashMap<>();
        Texture() { this.resource = TextureStore.createLegacyTexture(); this.resource.setMipRange(0, 1000); }
        Texture(RenderTexture resource) { this.resource = resource; }
    }
    private static final class State {
        int fogDistanceMode = 34139;
        Set<Integer>enabled=new HashSet<>();float[]color={1,1,1,1},normal={0,0,1},uv={0,0},clearColor={0,0,0,0},fogColor={0,0,0,0},blendColor={0,0,0,0};
        int alphaFunc=ALWAYS,blendSrc=ONE,blendDst=ZERO,depthFunc=LESS,cullFace=BACK,frontFace=CCW,colorMask=15,fogMode=EXP,shadeModel=SMOOTH,colorMaterial=AMBIENT_AND_DIFFUSE;
        boolean depthWrite=true;float alphaRef,clearDepth=1,minDepth=0,maxDepth=1,lineWidth=1,fogStart,fogEnd=1,fogDensity=1,offsetFactor,offsetUnits;
        int[]viewport={0,0,1,1},scissor={0,0,1,1};
        float[][]lightPosition={{0,0,1,0},{0,0,1,0}},lightDiffuse={{1,1,1,1},{0,0,0,1}};float[]lightAmbient={.2f,.2f,.2f,1};
        State copy(){State s=new State();s.fogDistanceMode=fogDistanceMode;s.enabled=new HashSet<>(enabled);s.color=color.clone();s.normal=normal.clone();s.uv=uv.clone();s.clearColor=clearColor.clone();s.fogColor=fogColor.clone();s.blendColor=blendColor.clone();s.alphaFunc=alphaFunc;s.blendSrc=blendSrc;s.blendDst=blendDst;s.depthFunc=depthFunc;s.cullFace=cullFace;s.frontFace=frontFace;s.colorMask=colorMask;s.fogMode=fogMode;s.shadeModel=shadeModel;s.colorMaterial=colorMaterial;s.depthWrite=depthWrite;s.alphaRef=alphaRef;s.clearDepth=clearDepth;s.minDepth=minDepth;s.maxDepth=maxDepth;s.lineWidth=lineWidth;s.fogStart=fogStart;s.fogEnd=fogEnd;s.fogDensity=fogDensity;s.offsetFactor=offsetFactor;s.offsetUnits=offsetUnits;s.viewport=viewport.clone();s.scissor=scissor.clone();s.lightPosition=new float[][]{lightPosition[0].clone(),lightPosition[1].clone()};s.lightDiffuse=new float[][]{lightDiffuse[0].clone(),lightDiffuse[1].clone()};s.lightAmbient=lightAmbient.clone();return s;}
    }
}
