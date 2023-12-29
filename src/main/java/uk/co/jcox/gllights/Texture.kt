package uk.co.jcox.gllights

data class Texture(
    val textureID: String,
    val objectID: Int,
) {

    enum class Type {
        DIFFUSE,
        SPECULAR
    }
}


