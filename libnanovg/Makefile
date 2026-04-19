CC ?= gcc
CC_ARM := aarch64-linux-gnu-gcc
CFLAGS := -fPIC -I./nanovg/src -I./glad/include -I.
CORE_LIB_DIR := ../core/lib

# Default values if not provided
OS_NAME ?= linux
ARCH_NAME ?= x86-64

# Platform specific configuration
ifeq ($(OS_NAME),linux)
	EXT := so
	LDFLAGS := -shared -lm -Wl,--version-script=exports.def
else ifeq ($(OS_NAME),windows)
	EXT := dll
	LDFLAGS := -shared
else ifeq ($(OS_NAME),macos)
	EXT := dylib
	LDFLAGS := -dynamiclib
else ifeq ($(OS_NAME),freebsd)
	EXT := so
	LDFLAGS := -shared -lm
endif

LIB_FILENAME := libnanovgffm-$(ARCH_NAME)-$(OS_NAME).$(EXT)

.PHONY: all clean build build-library-x86-64-linux build-library-arm64-linux

all: build

build: $(LIB_FILENAME)

$(LIB_FILENAME): nanovg.o glad.o nanovgffm.o
	$(CC) $(LDFLAGS) -o $@ $^
	mkdir -p $(CORE_LIB_DIR)
	cp $@ $(CORE_LIB_DIR)/$@

nanovg.o: nanovg/src/nanovg.c
	$(CC) $(CFLAGS) -c $< -o $@

glad.o: glad/src/gl.c
	$(CC) $(CFLAGS) -c $< -o $@

nanovgffm.o: nanovgffm.c
	$(CC) $(CFLAGS) -c $< -o $@

# Legacy targets for backward compatibility or direct usage
build-library-x86-64-linux:
	$(MAKE) OS_NAME=linux ARCH_NAME=x86-64 build

build-library-arm64-linux:
	$(MAKE) CC=$(CC_ARM) OS_NAME=linux ARCH_NAME=aarch64 build

clean:
	rm -f *.o *.so *.dll *.dylib libnanovgffm-*