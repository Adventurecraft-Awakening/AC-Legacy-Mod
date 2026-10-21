#pragma once

// Original bounded CRT adapters for the external SDK's Windows-style helpers.
#include <algorithm>
#include <cerrno>
#include <cstdarg>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <cwchar>
#include <cwctype>
#include <climits>
#include <cmath>
#include <limits>
#include <locale>
#include <FidelityFX/host/ffx_util.h>

#ifndef _WIN32
template<typename T, size_t N> constexpr size_t _countof(const T (&)[N]) noexcept { return N; }
inline int wcscpy_s(wchar_t* destination, size_t size, const wchar_t* source) {
    if (!destination || !source || !size) return EINVAL;
    size_t length = std::wcslen(source);
    if (length >= size) { destination[0] = 0; return ERANGE; }
    std::wmemcpy(destination, source, length + 1);
    return 0;
}
template<size_t N> inline int wcscpy_s(wchar_t (&destination)[N], const wchar_t* source) {
    return wcscpy_s(destination, N, source);
}
inline int strcpy_s(char* destination, size_t size, const char* source) {
    if (!destination || !source || !size) return EINVAL;
    size_t length = std::strlen(source);
    if (length >= size) { destination[0] = 0; return ERANGE; }
    std::memcpy(destination, source, length + 1);
    return 0;
}
inline int sprintf_s(char* destination, size_t size, const char* format, ...) {
    if (!destination || !size || !format) return -1;
    va_list arguments;
    va_start(arguments, format);
    int result = std::vsnprintf(destination, size, format, arguments);
    va_end(arguments);
    if (result < 0 || static_cast<size_t>(result) >= size) { destination[0] = 0; return -1; }
    return result;
}
inline int wcstombs_s(size_t* converted, char* destination, size_t size, const wchar_t* source, size_t count) {
    if (!converted || !destination || !source || !size) return EINVAL;
    size_t result = std::wcstombs(destination, source, std::min(size - 1, count));
    if (result == static_cast<size_t>(-1)) { destination[0] = 0; *converted = 0; return EILSEQ; }
    destination[result] = 0;
    *converted = result + 1;
    return 0;
}
#endif
