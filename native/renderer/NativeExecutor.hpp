#pragma once
#include <condition_variable>
#include <functional>
#include <future>
#include <memory>
#include <mutex>
#include <queue>
#include <stdexcept>
#ifdef _WIN32
#ifndef NOMINMAX
#define NOMINMAX
#endif
#include <windows.h>
#else
#include <pthread.h>
#endif

namespace ac {
// Some vendor discovery routines exceed a JVM thread's native stack budget.
// Keep all SDK calls on one persistent, explicitly sized native thread. JNI
// objects stay on the calling thread and only copied C++ values cross this API.
class NativeExecutor {
    std::mutex mutex_;
    std::condition_variable ready_;
    std::queue<std::function<void()>> tasks_;
    bool stopping_ = false;
    inline static thread_local bool executing_ = false;
#ifdef _WIN32
    HANDLE thread_ = nullptr;
    static DWORD WINAPI entry(void* owner) { static_cast<NativeExecutor*>(owner)->work(); return 0; }
#else
    pthread_t thread_{};
    static void* entry(void* owner) { static_cast<NativeExecutor*>(owner)->work(); return nullptr; }
#endif
    void work() {
        executing_ = true;
        for (;;) {
            std::function<void()> task;
            {
                std::unique_lock<std::mutex> lock(mutex_);
                ready_.wait(lock,[&] { return stopping_ || !tasks_.empty(); });
                if (tasks_.empty() && stopping_) return;
                task = std::move(tasks_.front()); tasks_.pop();
            }
            task();
        }
    }
public:
    NativeExecutor() {
        constexpr size_t stackBytes = 16 * 1024 * 1024;
#ifdef _WIN32
        thread_ = CreateThread(nullptr,stackBytes,entry,this,STACK_SIZE_PARAM_IS_A_RESERVATION,nullptr);
        if (!thread_) throw std::runtime_error("Cannot create native rendering SDK worker");
#else
        pthread_attr_t attributes;
        if (pthread_attr_init(&attributes)!=0) throw std::runtime_error("Cannot initialize native SDK thread attributes");
        int result=pthread_attr_setstacksize(&attributes,stackBytes);
        if (!result) result=pthread_create(&thread_,&attributes,entry,this);
        pthread_attr_destroy(&attributes);
        if (result) throw std::runtime_error("Cannot create native rendering SDK worker");
#endif
    }
    ~NativeExecutor() {
        { std::lock_guard<std::mutex> lock(mutex_); stopping_ = true; }
        ready_.notify_one();
#ifdef _WIN32
        WaitForSingleObject(thread_,INFINITE); CloseHandle(thread_);
#else
        pthread_join(thread_,nullptr);
#endif
    }
    template<class Function> auto invoke(Function function) -> std::invoke_result_t<Function> {
        if (executing_) return function();
        using Result = std::invoke_result_t<Function>;
        auto task=std::make_shared<std::packaged_task<Result()>>(std::move(function));
        auto result=task->get_future();
        { std::lock_guard<std::mutex> lock(mutex_); tasks_.push([task] { (*task)(); }); }
        ready_.notify_one();
        return result.get();
    }
};
inline NativeExecutor& nativeExecutor() { static NativeExecutor executor; return executor; }
}
