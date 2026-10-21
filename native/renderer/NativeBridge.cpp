#include <jni.h>
#include "NativeTypes.hpp"
#include "NativeExecutor.hpp"
#include "NrdBackend.hpp"
#include "fsr/FsrBackend.hpp"
#include "fsr/FsrFrameGeneration.hpp"
#include "ngx/NgxBackend.hpp"
#include "ngx/NgxFrameGeneration.hpp"
#include <algorithm>
#include <cstring>
#include <cstdio>
#include <memory>
#include <stdexcept>
#include <unordered_map>
#include <vector>

namespace {
using namespace ac;
std::string string(JNIEnv* env, jstring value) {
    if (!value) return {};
    const char* chars=env->GetStringUTFChars(value,nullptr);
    if (!chars) throw std::runtime_error("Cannot read native rendering path");
    std::string result(chars); env->ReleaseStringUTFChars(value,chars); return result;
}
jobjectArray strings(JNIEnv* env, const std::vector<std::string>& values) {
    auto type=env->FindClass("java/lang/String");
    auto result=env->NewObjectArray(jsize(values.size()),type,nullptr);
    for (size_t i=0;i<values.size();i++) { auto text=env->NewStringUTF(values[i].c_str()); env->SetObjectArrayElement(result,jsize(i),text); env->DeleteLocalRef(text); }
    env->DeleteLocalRef(type); return result;
}
void javaError(JNIEnv* env, const std::exception& error) {
    auto type=env->FindClass("java/lang/IllegalStateException"); env->ThrowNew(type,error.what()); env->DeleteLocalRef(type);
}
FrameData frame(JNIEnv* env, jobject buffer) {
    if (!buffer || env->GetDirectBufferCapacity(buffer)<320) throw std::runtime_error("Native reconstruction needs a direct 320-byte frame block");
    const auto* bytes=static_cast<const unsigned char*>(env->GetDirectBufferAddress(buffer));
    if (!bytes) throw std::runtime_error("Native frame block is not accessible");
    float values[72]; uint32_t flags[8]; std::memcpy(values,bytes,sizeof values); std::memcpy(flags,bytes+sizeof values,sizeof flags);
    FrameData f;
    std::copy_n(values,16,f.worldToView.begin()); std::copy_n(values+16,16,f.previousWorldToView.begin());
    std::copy_n(values+32,16,f.viewToClip.begin()); std::copy_n(values+48,16,f.previousViewToClip.begin());
    f.jitterX=values[64]; f.jitterY=values[65]; f.previousJitterX=values[66]; f.previousJitterY=values[67];
    f.deltaMs=values[68]; f.nearPlane=values[69]; f.farPlane=values[70]; f.verticalFov=values[71];
    f.renderWidth=flags[0]; f.renderHeight=flags[1]; f.outputWidth=flags[2]; f.outputHeight=flags[3];
    f.frameIndex=flags[4]; f.reset=flags[5]!=0; f.hdr=flags[6]!=0;
    if (!f.renderWidth || !f.renderHeight || !f.outputWidth || !f.outputHeight) throw std::runtime_error("Native reconstruction extent is empty");
    return f;
}
std::vector<Image> images(JNIEnv* env, jlongArray array, size_t expected) {
    if (!array || env->GetArrayLength(array)!=jsize(expected*6)) throw std::runtime_error("Native borrowed-image ABI does not match");
    std::vector<jlong> values(expected*6); env->GetLongArrayRegion(array,0,jsize(values.size()),values.data());
    std::vector<Image> result(expected);
    for (size_t i=0;i<expected;i++) {
        auto* p=values.data()+i*6;
        result[i]={reinterpret_cast<VkImage>(uintptr_t(p[0])),reinterpret_cast<VkImageView>(uintptr_t(p[1])),VkFormat(p[2]),uint32_t(p[3]),uint32_t(p[4]),VkImageLayout(p[5])};
    }
    return result;
}
struct Context {
    DeviceInfo device;
    std::unique_ptr<NgxBackend> ngx;
    std::unique_ptr<NrdBackend> nrd;
    std::unique_ptr<FsrBackend> fsr;
    std::unique_ptr<NgxFrameGeneration> fg;
    std::unique_ptr<FsrFrameGeneration> fsrFg;
    std::unordered_map<uint32_t,int> generationAlgorithms;
    std::array<std::string,6> errors;
    explicit Context(DeviceInfo d, const std::vector<std::string>& extensions) : device(std::move(d)) {
        errors.fill("Native rendering backend is unavailable");
        try { ngx=std::make_unique<NgxBackend>(device,extensions);
            errors[0]=ngx->reason(0); errors[1]=ngx->reason(1); errors[4]=ngx->reason(2);
            if (ngx->supported(2)) fg=std::make_unique<NgxFrameGeneration>(device);
        } catch (const std::exception& error) { errors[0]=errors[1]=errors[4]=error.what(); }
        if (NrdBackend::supported(device,errors[2])) try { nrd=std::make_unique<NrdBackend>(device); }
            catch (const std::exception& error) { errors[2]=error.what(); }
        FsrDevice fd{device.device,device.physicalDevice,device.queue,device.queueFamily,vkGetDeviceProcAddr,
            device.storageExtended,device.storageRead,device.storageWrite};
        if (FsrBackend::supported(fd,errors[3])) try { fsr=std::make_unique<FsrBackend>(fd); }
            catch (const std::exception& error) { errors[3]=error.what(); }
        if (FsrFrameGeneration::supported(device,errors[5])) try { fsrFg=std::make_unique<FsrFrameGeneration>(device); }
            catch (const std::exception& error) { errors[5]=error.what(); }
    }
    ~Context() {
        vkDeviceWaitIdle(device.device);
        fsrFg.reset(); fg.reset(); fsr.reset(); nrd.reset(); ngx.reset();
    }
    bool supported(int feature) const { return feature>=0 && feature<6 && errors[feature].empty(); }
    void failure(int feature, const std::string& reason) {
        errors[feature]=reason;
        std::fprintf(stderr,"AdventureCraft native rendering: %s\n",reason.c_str());
    }
    void abort() {
        vkDeviceWaitIdle(device.device);
        if (nrd) nrd->abort(); if (fsr) fsr->abort(); if (ngx) ngx->abort(); if (fg) fg->invalidate();
        if (fsrFg) fsrFg->abort();
        generationAlgorithms.clear();
    }
};
Context& context(jlong handle) { if (!handle) throw std::runtime_error("Native rendering context is closed"); return *reinterpret_cast<Context*>(uintptr_t(handle)); }
FsrImage fsrImage(const Image& image, bool output=false) {
    return {image.image,image.format,image.width,image.height,image.layout,
        VkImageUsageFlags(output ? VK_IMAGE_USAGE_STORAGE_BIT|VK_IMAGE_USAGE_SAMPLED_BIT|VK_IMAGE_USAGE_TRANSFER_SRC_BIT : VK_IMAGE_USAGE_SAMPLED_BIT)};
}
}

extern "C" {
JNIEXPORT jobjectArray JNICALL Java_dev_adventurecraft_awakening_client_render_vulkan_NativeRenderingBridge_nInstanceExtensions(JNIEnv* env,jclass,jstring root) {
    try { auto path=string(env,root); return strings(env,nativeExecutor().invoke([&] { return ngxExtensions(path); })); } catch (const std::exception& e) { javaError(env,e); return nullptr; }
}
JNIEXPORT jobjectArray JNICALL Java_dev_adventurecraft_awakening_client_render_vulkan_NativeRenderingBridge_nDeviceExtensions(JNIEnv* env,jclass,jlong instance,jlong physical,jstring root) {
    try { auto path=string(env,root); return strings(env,nativeExecutor().invoke([&] { return ngxExtensions(path,reinterpret_cast<VkInstance>(uintptr_t(instance)),reinterpret_cast<VkPhysicalDevice>(uintptr_t(physical))); })); }
    catch (const std::exception& e) { javaError(env,e); return nullptr; }
}
JNIEXPORT jlong JNICALL Java_dev_adventurecraft_awakening_client_render_vulkan_NativeRenderingBridge_nCreate(JNIEnv* env,jclass,jlong instance,jlong physical,jlong device,jlong queue,jint family,jstring root,
    jobjectArray extensions,jboolean extended,jboolean read,jboolean write,jboolean partiallyBound,jboolean sync2,jboolean dynamicRendering,jboolean maintenance4,jboolean bda,jint apiVersion) {
    try {
        DeviceInfo d; d.instance=reinterpret_cast<VkInstance>(uintptr_t(instance)); d.physicalDevice=reinterpret_cast<VkPhysicalDevice>(uintptr_t(physical));
        d.device=reinterpret_cast<VkDevice>(uintptr_t(device)); d.queue=reinterpret_cast<VkQueue>(uintptr_t(queue)); d.queueFamily=family; d.sdkRoot=string(env,root);
        d.storageExtended=extended; d.storageRead=read; d.storageWrite=write; d.partiallyBound=partiallyBound;
        d.synchronization2=sync2; d.dynamicRendering=dynamicRendering; d.maintenance4=maintenance4; d.bufferDeviceAddress=bda; d.apiVersion=uint32_t(apiVersion);
        std::vector<std::string> names;
        for (jsize i=0;extensions && i<env->GetArrayLength(extensions);i++) {
            auto name=static_cast<jstring>(env->GetObjectArrayElement(extensions,i)); names.push_back(string(env,name)); env->DeleteLocalRef(name);
        }
        d.pushDescriptor=std::find(names.begin(),names.end(),"VK_KHR_push_descriptor")!=names.end();
        return jlong(reinterpret_cast<uintptr_t>(nativeExecutor().invoke([&] { return new Context(std::move(d),names); })));
    } catch (const std::exception& e) { javaError(env,e); return 0; }
}
JNIEXPORT jboolean JNICALL Java_dev_adventurecraft_awakening_client_render_vulkan_NativeRenderingBridge_nSupported(JNIEnv* env,jclass,jlong handle,jint feature) {
    try { return context(handle).supported(feature); } catch (const std::exception& e) { javaError(env,e); return false; }
}
JNIEXPORT jstring JNICALL Java_dev_adventurecraft_awakening_client_render_vulkan_NativeRenderingBridge_nReason(JNIEnv* env,jclass,jlong handle,jint feature) {
    try { auto& c=context(handle); return env->NewStringUTF(feature>=0 && feature<6 ? c.errors[feature].c_str() : "Unknown native feature"); }
    catch (const std::exception& e) { javaError(env,e); return nullptr; }
}
JNIEXPORT jboolean JNICALL Java_dev_adventurecraft_awakening_client_render_vulkan_NativeRenderingBridge_nDenoise(JNIEnv* env,jclass,jlong handle,jlong cmd,jlongArray input,jobject data,jboolean direct,jboolean indirect) {
    try { auto& c=context(handle); if (!c.supported(2) || !c.nrd) return false;
        auto i=images(env,input,9); auto f=frame(env,data); std::string error;
        bool ok=nativeExecutor().invoke([&] { return c.nrd->denoise(reinterpret_cast<VkCommandBuffer>(uintptr_t(cmd)),f,direct,indirect,
            i[0],i[1],i[2],i[3],i[4],i[5],i[6],i[7],i[8],error); });
        if (!ok) c.failure(2,error); return ok;
    } catch (const std::exception& e) { javaError(env,e); return false; }
}
JNIEXPORT jboolean JNICALL Java_dev_adventurecraft_awakening_client_render_vulkan_NativeRenderingBridge_nReconstruct(JNIEnv* env,jclass,jlong handle,jlong cmd,jint algorithm,jlongArray input,jobject data) {
    try { auto& c=context(handle); auto i=images(env,input,9); auto f=frame(env,data); std::string error; bool ok=false;
        auto command=reinterpret_cast<VkCommandBuffer>(uintptr_t(cmd)); int feature=algorithm==3 ? 3 : algorithm==2 ? 1 : 0;
        if (!c.supported(feature)) return false;
        if (algorithm==3 && c.fsr) {
            FsrFrame ff; ff.commandBuffer=command; ff.color=fsrImage(i[0]); ff.depth=fsrImage(i[1]); ff.motion=fsrImage(i[2]);
            ff.reactive=fsrImage(i[3]); ff.output=fsrImage(i[8],true); ff.renderWidth=f.renderWidth; ff.renderHeight=f.renderHeight;
            ff.outputWidth=f.outputWidth; ff.outputHeight=f.outputHeight; ff.jitterX=f.jitterX; ff.jitterY=f.jitterY;
            ff.deltaMilliseconds=f.deltaMs; ff.cameraNear=f.nearPlane; ff.cameraFar=f.farPlane; ff.verticalFovRadians=f.verticalFov;
            ff.hdr=f.hdr; ff.reset=f.reset; ok=nativeExecutor().invoke([&] { return c.fsr->dispatch(ff,error); });
        } else if ((algorithm==1 || algorithm==2) && c.ngx) ok=nativeExecutor().invoke([&] { return c.ngx->reconstruct(command,algorithm,i,f,error); });
        if (!ok) c.failure(feature,error.empty() ? "Native reconstruction dispatch is unavailable" : error); return ok;
    } catch (const std::exception& e) { javaError(env,e); return false; }
}
JNIEXPORT jlongArray JNICALL Java_dev_adventurecraft_awakening_client_render_vulkan_NativeRenderingBridge_nGenerate(JNIEnv* env,jclass,jlong handle,jlong cmd,jint algorithm,jint slot,jlongArray input,jobject data) {
    try { auto& c=context(handle); auto empty=env->NewLongArray(0);
        const int feature=algorithm==1 ? 4 : algorithm==2 ? 5 : -1;
        c.generationAlgorithms.erase(uint32_t(slot));
        if (!c.supported(feature)) return empty;
        auto i=images(env,input,4); auto f=frame(env,data); std::string error; Image interpolated,real;
        bool ok=nativeExecutor().invoke([&] {
            auto command=reinterpret_cast<VkCommandBuffer>(uintptr_t(cmd));
            if (algorithm==1 && c.fg) {
                FrameGenerationOutputs output;
                if (!c.fg->dispatch(command,uint32_t(slot),i[0],i[1],i[2],i[3],f,output,error)) return false;
                interpolated=output.interpolated; real=output.real; return true;
            }
            if (algorithm==2 && c.fsrFg) {
                FsrFrameGenerationOutputs output;
                if (!c.fsrFg->dispatch(command,uint32_t(slot),i[0],i[1],i[2],i[3],f,output,error)) return false;
                interpolated=output.interpolated; real=output.real; return true;
            }
            error="Native frame generation dispatch is unavailable"; return false;
        });
        if (!ok) { c.failure(feature,error); return empty; }
        c.generationAlgorithms[uint32_t(slot)]=algorithm;
        jlong values[]{jlong(reinterpret_cast<uintptr_t>(interpolated.image)),jlong(reinterpret_cast<uintptr_t>(real.image))};
        auto result=env->NewLongArray(2); env->SetLongArrayRegion(result,0,2,values); return result;
    } catch (const std::exception& e) { javaError(env,e); return nullptr; }
}
JNIEXPORT jboolean JNICALL Java_dev_adventurecraft_awakening_client_render_vulkan_NativeRenderingBridge_nInterpolationAllowed(JNIEnv* env,jclass,jlong handle,jint slot) {
    try { auto& c=context(handle); auto found=c.generationAlgorithms.find(uint32_t(slot));
        if (found==c.generationAlgorithms.end()) return false;
        int algorithm=found->second; std::string error;
        bool result=nativeExecutor().invoke([&] {
            if (algorithm==1 && c.fg) return c.fg->interpolationAllowed(uint32_t(slot),&error);
            if (algorithm==2 && c.fsrFg) return c.fsrFg->interpolationAllowed(uint32_t(slot),&error);
            return false;
        });
        if (!error.empty()) c.failure(algorithm==1 ? 4 : 5,error); return result; }
    catch (const std::exception& e) { javaError(env,e); return false; }
}
JNIEXPORT void JNICALL Java_dev_adventurecraft_awakening_client_render_vulkan_NativeRenderingBridge_nAbort(JNIEnv* env,jclass,jlong handle) {
    try { nativeExecutor().invoke([&] { context(handle).abort(); }); } catch (const std::exception& e) { javaError(env,e); }
}
JNIEXPORT void JNICALL Java_dev_adventurecraft_awakening_client_render_vulkan_NativeRenderingBridge_nDestroy(JNIEnv* env,jclass,jlong handle) {
    try { nativeExecutor().invoke([&] { delete reinterpret_cast<Context*>(uintptr_t(handle)); }); } catch (const std::exception& e) { javaError(env,e); }
}
}
