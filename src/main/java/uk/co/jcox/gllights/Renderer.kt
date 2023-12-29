package uk.co.jcox.gllights

import org.lwjgl.opengl.*

class Renderer {

    fun setup() {
        GL.createCapabilities()
        GLUtil.setupDebugMessageCallback()
        GL11.glEnable(GL11.GL_DEPTH_TEST)
    }

    fun setRenderColour(x: Float, y: Float, z: Float) {
        GL11.glClearColor(x, y, z, 1.0f)
    }

    fun clearBuffers() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }

    fun setViewCanvas(screenX: Int, screenY: Int) {
        GL11.glViewport(0, 0, screenX, screenY)
    }


    fun drawModel(model: Model, program: ShaderProgram) {
        drawModel(model, program, false)
    }

    fun drawModel(model: Model, program: ShaderProgram, wireframe: Boolean) {
        program.bind()

        if (wireframe) {
            GL15.glPolygonMode(GL15.GL_FRONT_AND_BACK, GL15.GL_LINE)
        } else {
            GL15.glPolygonMode(GL15.GL_FRONT_AND_BACK, GL15.GL_FILL)
        }
        model.meshList.forEach {
            GL30.glBindVertexArray(it.vertexArray)
            GL15.glDrawElements(GL15.GL_TRIANGLES, it.indices, GL15.GL_UNSIGNED_INT, 0)
            GL30.glBindVertexArray(0)
        }
        program.unbind()
    }


    fun freeModelResources(model: Model) {
        model.meshList.forEach { mesh ->
            GL30.glDeleteVertexArrays(mesh.vertexArray)
            mesh.arrayBuffers.forEach { buff ->
                GL15.glDeleteBuffers(buff)
            }
        }
    }
}