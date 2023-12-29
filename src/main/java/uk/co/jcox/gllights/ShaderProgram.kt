package uk.co.jcox.gllights

import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL20


class ShaderProgram (
    val name: String,
): BindableState {

    private val shaderIds: MutableList<Int> = mutableListOf()
    private var program: Int = -1


    fun createProgram(vararg shaderInfos: ShaderInfo) {
        this.program = GL20.glCreateProgram()

        if (shaderInfos.isEmpty()) {
            throw RuntimeException("No shaders found for compilation")
        }

        for (info in shaderInfos) {
            val shaderId: Int = GL20.glCreateShader(info.shaderType.glObjectType)
            GL20.glShaderSource(shaderId, info.shaderSource)
            GL20.glCompileShader(shaderId)
            GL20.glAttachShader(program, shaderId)
            shaderIds.add(shaderId)
        }

        GL20.glLinkProgram(program)

        if (getProgramInfoLog().isNotEmpty()) {
            throw RuntimeException("Shader Compilation failed for $name : " + getProgramInfoLog());
        }

        bind()

        validateProgram()
    }

    fun getProgramInfoLog(): String {
        return GL20.glGetProgramInfoLog(program, 1024)
    }

    fun validateProgram() {
        GL20.glValidateProgram(program)
        if (GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) == 0) {
            throw RuntimeException("Shader validation failed: " + getProgramInfoLog())
        }
    }


    private fun getLoc(uniformName: String): Int {
        bind()
        val location: Int = GL20.glGetUniformLocation(program, uniformName)
        if (location == -1) {
            throw RuntimeException("Did not expect uniform $uniformName in program $name");
        }
        return location;
    }

    fun send(uniformName: String, value: Matrix4f) {
        val loc: Int = getLoc(uniformName)
        val buff = BufferUtils.createFloatBuffer(16)
        value.get(buff)
        GL20.glUniformMatrix4fv(loc, false, buff)
    }

    fun send(uniformName: String, value: Vector3f) {
        val loc: Int = getLoc(uniformName)
        GL20.glUniform3f(loc, value.x, value.y, value.z)
    }

    fun send(uniformName: String, value: Int) {
        val loc: Int = getLoc(uniformName)
        GL20.glUniform1i(loc, value)
    }

    fun send(uniformName: String, value: Float) {
        val loc = getLoc(uniformName)
        GL20.glUniform1f(loc, value)
    }

    override fun destroy() {
        unbind()
        shaderIds.forEach {
            GL20.glDetachShader(program, it)
            GL20.glDeleteShader(it)
        }

        GL20.glDeleteProgram(program)
    }

    override fun bind() {
        GL20.glUseProgram(program)
    }

    override fun unbind() {
        GL20.glUseProgram(0)
    }


    class ShaderInfo (
        val shaderType: ShaderType,
        val shaderSource: String,
    )

    enum class ShaderType (val glObjectType: Int) {
        VERTEX(GL20.GL_VERTEX_SHADER),
        FRAGMENT(GL20.GL_FRAGMENT_SHADER)
    }
}