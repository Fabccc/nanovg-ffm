#!/bin/bash


# set -e
echo "Generating GL, GLEW and GLFW bindings with jextract"

echo "Generating GLFW bindings..."
rm -r src/main/java/com/fabcc/nanovg/example/glfwffm/lib || true
jextract -t com.fabcc.nanovg.example.glfwffm.lib --output src/main/java \
    -l glfw \
    --header-class-name "GLFW" \
    --dump-includes includes.txt \
    /usr/include/GLFW/glfw3.h

echo "Filtering GLFW includes..."
rm include_filtered.txt || true
touch include_filtered.txt

{
    grep -E '(--include-typedef GLFW)' includes.txt
    grep -E '(--include-constant GLFW)' includes.txt
    grep -E '(--include-function glfw)' includes.txt
} >> include_filtered.txt
echo "Regenerating GLFW bindings with filtered includes..."

set -x
jextract -t com.fabcc.nanovg.example.glfwffm.lib --output src/main/java \
    -l glfw \
    --header-class-name "GLFW" \
    @include_filtered.txt \
    /usr/include/GLFW/glfw3.h
echo "Done generating GLFW bindings!"

echo "Generating GL and GLEW bindings..."
set +x
rm includes.txt || true
rm include_filtered.txt || true
if ! (jextract -t com.fabcc.nanovg.example.glfwffm.lib --output src/main/java \
    -l GLEW \
    --header-class-name "GL" \
    --dump-includes includes.txt \
    /usr/include/GL/glew.h
); then
    echo "jextract failed to generate GL and GLEW bindings"
    exit 1
fi

echo "Filtering GL and GLEW includes..."
rm include_filtered.txt || true
touch include_filtered.txt

{
    grep -E '(--include-typedef GL)' includes.txt
    grep -E '(--include-constant GL)' includes.txt
    grep -E '(--include-function gl)' includes.txt
    grep -E '(--include-function glew)' includes.txt
} >> include_filtered.txt

echo "Regenerating GL and GLEW bindings with filtered includes..."
set -x
jextract -t com.fabcc.nanovg.example.glfwffm.lib --output src/main/java \
    -l GLEW \
    --header-class-name "GL" \
    @include_filtered.txt \
    /usr/include/GL/glew.h

echo "Done generating GL and GLEW bindings!"