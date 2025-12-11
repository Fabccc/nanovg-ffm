CC := gcc
CC_ARM := aarch64-linux-gnu-gcc
CFLAGS := -fPIC -I./nanovg/src -I.
LDFLAGS := -shared -lm -lGL -lGLEW -Wl,--version-script=exports.def
CORE_LIB_DIR := ../core/lib
PHONY := build-library-x86-64-linux build-library-arm64-linux

build-library-x86-64-linux:
	$(CC) $(CFLAGS) -c nanovg/src/nanovg.c -o nanovg.o
	$(CC) $(CFLAGS) -c nanovgffm.c -o nanovgffm.o
	$(CC) $(LDFLAGS) -o libnanovgffm.so nanovg.o nanovgffm.o
	rm -f nanovg.o nanovgffm.o
	cp libnanovgffm.so $(CORE_LIB_DIR)/libnanovgffm-x86-64-linux.so

build-library-arm64-linux:
	$(CC_ARM) $(CFLAGS) -c nanovg/src/nanovg.c -o nanovg.o
	$(CC_ARM) $(CFLAGS) -c nanovgffm.c -o nanovgffm.o
	$(CC_ARM) $(LDFLAGS) -o libnanovgffm.so nanovg.o nanovgffm.o
	rm -f nanovg.o nanovgffm.o
	cp libnanovgffm.so $(CORE_LIB_DIR)/libnanovgffm-aarch64-linux.so

build-library-x86-64-windows:
	echo "Not implemented"
	exit 1

build-library-x86-64-macos:
	echo "Not implemented"
	exit 1

build-library-x86-64-freebsd:
	echo "Not implemented"
	exit 1

clean:
	rm -f libnanovgffm.so *.o