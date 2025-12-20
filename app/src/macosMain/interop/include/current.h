// src/nativeInterop/cinterop/bridge.h
#include <mach-o/dyld.h>
#include <limits.h>
#include <stdlib.h>

// 显式声明一下以确保导出，虽然 mach-o/dyld.h 已经包含了它
extern int _NSGetExecutablePath(char* buf, uint32_t* bufsize);
