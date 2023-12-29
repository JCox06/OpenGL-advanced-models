package uk.co.jcox.gllights

import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

private val window = Window("Window", 1200, 1200)
private val renderer = Renderer()
private val program = ShaderProgram("MainProgram")
private val camera = Camera()

private const val CAM_SPEED: Float = 5f
private const val CAM_SENSE: Float = 0.25f

private var model: Model? = null

fun main() {

    window.createGlContext()
    renderer.setup()
    renderer.setRenderColour(0.01f, 0.01f, 0.01f)

    model = GLLoader.importModel(File("data/models/backpack/backpack.obj"));

    try {
        val vshsrc = Files.readString(Paths.get("data/shaders/phong_world.vsh"))
        val fshsrc = Files.readString(Paths.get("data/shaders/phong_world.fsh"))
        val vsh = ShaderProgram.ShaderInfo(ShaderProgram.ShaderType.VERTEX, vshsrc)
        val fsh = ShaderProgram.ShaderInfo(ShaderProgram.ShaderType.FRAGMENT, fshsrc)

        program.createProgram(vsh, fsh)
    } catch (e: IOException) {
        e.printStackTrace()
    }

    engineLoop()

    renderer.freeModelResources(model!!)
    program.destroy()
    window.terminate()
}


private fun engineLoop() {

    window.setMouseFunc {
        if (window.mousePressed(GLFW.GLFW_MOUSE_BUTTON_1)) {
            camera.rotate(window.getxOffset() * CAM_SENSE, window.getyOffset() * CAM_SENSE)
        }
    }

    var lastFrameTime = 0.0f
    var deltaTime: Float

    while (! window.shouldClose()) {

        val time: Float = window.timeElapsed.toFloat()
        deltaTime = time - lastFrameTime
        lastFrameTime = time

        renderer.clearBuffers()

        render()
        input(deltaTime)
        update(deltaTime)

        window.runWindowUpdates()
    }
}

private fun render() {
    renderer.setViewCanvas(window.width, window.height)
    program.bind()
    program.send("camMatrix", camera.lookAt)
    program.send("projMatrix", camera.getProjection(window.aspectRatio()))
    program.send("modelMatrix", Matrix4f())

    renderer.drawModel(model!!, program, window.isPressed(GLFW.GLFW_KEY_H))
}

private fun input(deltaTime: Float) {
    if (window.isPressed(GLFW.GLFW_KEY_W)) {
        camera.moveForward(CAM_SPEED * deltaTime)
    }
    if (window.isPressed(GLFW.GLFW_KEY_S)) {
        camera.moveForward(-CAM_SPEED * deltaTime)
    }
    if (window.isPressed(GLFW.GLFW_KEY_D)) {
        camera.moveRight(CAM_SPEED * deltaTime)
    }
    if (window.isPressed(GLFW.GLFW_KEY_A)) {
        camera.moveRight(-CAM_SPEED * deltaTime)
    }
}

private fun update(delta: Float) {

}