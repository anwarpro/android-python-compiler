//
// Created by anwar on ১৮/৮/২১.
//

#include <stdio.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <unistd.h>
#include <string.h>
#include <locale.h>

#define ENTRYPOINT_MAXLEN 128

typedef void (*PyMem_RawFreePtr)(void *);

int main(int argc, char **argv) {
    void *handle;
    char *error;

    char *env_argument = NULL;
    char *env_entrypoint = NULL;
    char *env_logname = NULL;
    char entrypoint[ENTRYPOINT_MAXLEN];
    int ret = 0;
    char *waitForExit = NULL;

    char *exePath = NULL;

    PyMem_RawFreePtr PyMem_RawFree = 0;

    // Set a couple of built-in environment vars:
    setenv("P4A_BOOTSTRAP", "SDL2", 1);  // env var to identify p4a to applications
    env_argument = getenv("ANDROID_ARGUMENT");
    exePath = getenv("EXEPATH");

    waitForExit = getenv("WAIT_FOR_EXIT");

    setenv("ANDROID_APP_PATH", env_argument, 1);
    env_entrypoint = getenv("ANDROID_ENTRYPOINT");
    env_logname = getenv("PYTHON_NAME");
    if (!getenv("ANDROID_UNPACK")) {
        setenv("ANDROID_UNPACK", env_argument, 1);
    }
    if (env_logname == NULL) {
        env_logname = "python";
        setenv("PYTHON_NAME", "python", 1);
    }

    void (*pySetProgramName)(const wchar_t *);
    wchar_t *(*pyDecodeLocale)(const char *arg, size_t *size);
    void (*pySetPath)(const wchar_t *);
    void (*pyInit)();
    void (*pyEvalInit)();
    int (*pySimpleString)(const char *s);

    void *(*pyMemRawMalloc)(size_t size);

    int (*pyMain)(int argc, const wchar_t **argv);


    handle = dlopen(exePath, RTLD_LAZY);
    if (!handle) {
        fputs(dlerror(), stderr);
        exit(1);
    }

    pySetProgramName = dlsym(handle, "Py_SetProgramName");
    if ((error = dlerror()) != NULL) {
        fputs(error, stderr);
        exit(1);
    }

    pyDecodeLocale = dlsym(handle, "Py_DecodeLocale");
    if ((error = dlerror()) != NULL) {
        fputs(error, stderr);
        exit(1);
    }

    pySetPath = dlsym(handle, "Py_SetPath");
    if ((error = dlerror()) != NULL) {
        fputs(error, stderr);
        exit(1);
    }

    pyInit = dlsym(handle, "Py_Initialize");
    if ((error = dlerror()) != NULL) {
        fputs(error, stderr);
        exit(1);
    }

    pyEvalInit = dlsym(handle, "PyEval_InitThreads");
    if ((error = dlerror()) != NULL) {
        fputs(error, stderr);
        exit(1);
    }

    pySimpleString = dlsym(handle, "PyRun_SimpleString");
    if ((error = dlerror()) != NULL) {
        fputs(error, stderr);
        exit(1);
    }

    pyMemRawMalloc = dlsym(handle, "PyMem_RawMalloc");
    if ((error = dlerror()) != NULL) {
        fputs(error, stderr);
        exit(1);
    }

    PyMem_RawFree = (PyMem_RawFreePtr) dlsym(handle, "PyMem_RawFree");
    if ((error = dlerror()) != NULL) {
        fputs(error, stderr);
        exit(1);
    }

    pyMain = dlsym(handle, "Py_Main");
    if ((error = dlerror()) != NULL) {
        fputs(error, stderr);
        exit(1);
    }

    (*pySetProgramName)(L"android_python");

    // Set up the python path
    char paths[256];

    char python_bundle_dir[256];
    snprintf(python_bundle_dir, 256,
             "%s/_python_bundle", getenv("ANDROID_UNPACK"));

    snprintf(paths, 256,
             "%s/stdlib.zip:%s/modules",
             python_bundle_dir, python_bundle_dir);

    const wchar_t *wchar_paths = (*pyDecodeLocale)(paths, NULL);
    (*pySetPath)(wchar_paths);
    (*pyInit)();
    (*pyEvalInit)();


    /* inject our bootstrap code to redirect python stdin/stdout
     * replace sys.path with our path
     */
    (*pySimpleString)("import sys, posix\n");

    char add_site_packages_dir[256];
    char add_prefix[256];
    char add_lib_path[256];

    snprintf(add_site_packages_dir, 256,
             "sys.path.append('%s/site-packages')",
             python_bundle_dir);

    snprintf(add_lib_path, 256,
             "sys.path.append('%s/lib/python3.8/site-packages')",
             getenv("ANDROID_UNPACK"));

    snprintf(add_prefix, 256,
             "sys.prefix = '%s'",
             getenv("ANDROID_UNPACK"));

    (*pySimpleString)("import sys\n"
                      "sys.argv = ['notaninterpreterreally']\n"
                      "from os.path import realpath, join, dirname");
    (*pySimpleString)(add_prefix);
    (*pySimpleString)(add_site_packages_dir);
    (*pySimpleString)(add_lib_path);
    (*pySimpleString)("sys.path = ['.'] + sys.path");

    //open shell

    wchar_t **argv_copy = (wchar_t **) (*pyMemRawMalloc)(sizeof(wchar_t *) * (argc + 1));
    wchar_t **argv_copy2 = (wchar_t **) (*pyMemRawMalloc)(sizeof(wchar_t *) * (argc + 1));

    char *oldloc = strdup(setlocale(LC_ALL, 0));
    setlocale(LC_ALL, "");

    for (int i = 0; i < argc; ++i) {
        argv_copy[i] = (*pyDecodeLocale)(argv[i], 0);
        if (argv_copy[i] == 0) {
            free(oldloc);
            fprintf(stderr, "Fatal Python error: unable to decode the command line argument #%i\n",
                    i + 1);
            exit(1);
        }
        argv_copy2[i] = argv_copy[i];
    }
    argv_copy2[argc] = argv_copy[argc] = 0;
    setlocale(LC_ALL, oldloc);
    free(oldloc);

    ret = (*pyMain)(argc, argv_copy);

    //TODO free memory for next use
    for (int i = 0; i < argc; i++) {
        PyMem_RawFree(argv_copy2[i]);
    }
    PyMem_RawFree(argv_copy);
    PyMem_RawFree(argv_copy2);

    if (handle != 0) {
        dlclose(handle);
    }

    return ret;
}