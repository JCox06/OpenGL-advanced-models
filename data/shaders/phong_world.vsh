#version 330 core

//Phong lighting in world space

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTex;


uniform mat4 camMatrix;
uniform mat4 projMatrix;
uniform mat4 modelMatrix;


out vec2 fTexelCoord;
out vec3 fNormal;
out vec3 fWorldPos;


void main() {

    fNormal = mat3(transpose(inverse(modelMatrix))) * aNormal;
    fWorldPos = vec3(modelMatrix * vec4(aPos, 1.0f));
    fTexelCoord = aTex;
    gl_Position = projMatrix * camMatrix * modelMatrix * vec4(aPos, 1.0f);
}