package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import static org.lwjgl.vulkan.VK10.*;

/** Immutable memory topology queried once, with required flags separated from preferences. */
public final class VulkanMemoryProfile {
    private final int[] flags;
    private final boolean unified;
    private final long deviceLocalBytes;
    private final long hostVisibleBytes;

    VulkanMemoryProfile(VkPhysicalDevice device) {
        try(var stack=MemoryStack.stackPush()) {
            var memory=VkPhysicalDeviceMemoryProperties.malloc(stack);
            var properties=VkPhysicalDeviceProperties.malloc(stack);
            vkGetPhysicalDeviceMemoryProperties(device,memory);
            vkGetPhysicalDeviceProperties(device,properties);
            unified=properties.deviceType()==VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU || properties.deviceType()==VK_PHYSICAL_DEVICE_TYPE_CPU;
            flags=new int[memory.memoryTypeCount()];
            boolean[] visibleHeaps=new boolean[memory.memoryHeapCount()];
            for(int i=0;i<flags.length;i++) {
                flags[i]=memory.memoryTypes(i).propertyFlags();
                if((flags[i]&VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)!=0) visibleHeaps[memory.memoryTypes(i).heapIndex()]=true;
            }
            long local=0,visible=0;
            for(int i=0;i<memory.memoryHeapCount();i++) {
                var heap=memory.memoryHeaps(i);
                if((heap.flags()&VK_MEMORY_HEAP_DEVICE_LOCAL_BIT)!=0) local+=heap.size();
                if(visibleHeaps[i]) visible+=heap.size();
            }
            deviceLocalBytes=local; hostVisibleBytes=visible;
        }
    }

    public boolean unified() { return unified; }
    public long deviceLocalBytes() { return deviceLocalBytes; }
    public long hostVisibleBytes() { return hostVisibleBytes; }
    public int memoryTypeCount() { return flags.length; }
    int flags(int memoryType) { return flags[memoryType]; }
    int select(int allowed,int required,int preferred) {
        int best=-1,bestScore=-1;
        for(int i=0;i<flags.length;i++) {
            if((allowed&(1<<i))==0 || (flags[i]&required)!=required) continue;
            int score=Integer.bitCount(flags[i]&preferred);
            if(score>bestScore) { best=i; bestScore=score; }
        }
        if(best<0) throw new IllegalStateException("No compatible Vulkan memory type for flags 0x"+Integer.toHexString(required));
        return best;
    }
}
