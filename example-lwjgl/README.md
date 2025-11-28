

## On wayland

According to [this github issue](https://github.com/glfw/glfw/issues/2680), you need to set the `__GL_THREADED_OPTIMIZATIONS` env variable to `0` to make LWJGL and GLFW works.
I don't know why it works with FFM tho.

```shell
__GL_THREADED_OPTIMIZATIONS=0 mvn exec:java
```

