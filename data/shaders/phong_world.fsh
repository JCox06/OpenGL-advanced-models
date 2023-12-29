#version 330 core

//Phong lighting in world space

out vec4 FragColour;

in vec2 fTexelCoord;
in vec3 fNormal;
in vec3 fWorldPos;

struct Material {
    sampler2D diffuse;
    sampler2D specular;
    sampler2D emission;
    float shininess;
};


struct LightShading {
    vec3 ambience;
    vec3 diffuse;
    vec3 specular;
};


struct DirectionalLight {
    LightShading lightShading;
    vec3 direction;
};

struct PointLight {
    LightShading lightShading;
    vec3 worldPosition;
    float constant;
    float linear;
    float quadratic;
    float angleCutoff;
    float angleOuterCutoff;
    vec3 direction;
    bool spotLight;
    bool on;
};


vec3 calcDirectionalLighting(vec3 normal);
vec3 calcPointsLighting(PointLight light, vec3 normal);
vec3 calcAmbient(vec3 ambience);
vec3 calcDiffuse(vec3 diffuse, vec3 normal, vec3 lightDir);
vec3 calcSpecular(vec3 specular, vec3 normal, vec3 lightDir);


#define LIGHTS 2

uniform vec3 cameraPos;
uniform Material material;
uniform DirectionalLight directionalLight;
uniform PointLight pointLights[LIGHTS];

void main() {
    vec3 normalVec = normalize(fNormal);
    vec3 shading = calcDirectionalLighting(normalVec);

    for (int i = 0; i < pointLights.length(); i++) {
        PointLight light = pointLights[i];
        if (light.lightShading.ambience != 0 && light.on) {
            shading += calcPointsLighting(light, normalVec);
        }
    }

//    FragColour = vec4(shading, 1.0f) + texture(material.emission, fTexelCoord);
    FragColour = vec4(1.0f, 1.0f, 1.0f, 1.0f);
}



vec3 calcDirectionalLighting(vec3 normal) {
    //Calculate Ambient Shading:
    vec3 ambient = calcAmbient(directionalLight.lightShading.ambience);

    //Calculate Diffuse Shading:
    vec3 diffuse = calcDiffuse(directionalLight.lightShading.diffuse, normal, directionalLight.direction);

    //Calculate Specular Shading:
    vec3 specular = calcSpecular(directionalLight.lightShading.specular, normal, directionalLight.direction);

    return (ambient + diffuse + specular);
}


vec3 calcPointsLighting(PointLight light, vec3 normal) {

    vec3 fragToLight = normalize(light.worldPosition - fWorldPos);

    float angleTest = dot(fragToLight, normalize(-light.direction));
    float angleDiff = light.angleCutoff - light.angleOuterCutoff;
    float intensity = clamp((angleTest - light.angleOuterCutoff) / angleDiff, 0.0f, 1.0f);

    if (light.spotLight && angleTest <= light.angleOuterCutoff) {
        return vec3(0.0f, 0.0f, 0.0f);
    }

    float fragLightLength = length(light.worldPosition - fWorldPos);
    float Fatt = 1.0f / (light.constant + light.linear * fragLightLength + light.quadratic * pow(fragLightLength, 2));

    vec3 ambient = calcAmbient(light.lightShading.ambience);
    vec3 diffuse = calcDiffuse(light.lightShading.diffuse, normal, fragToLight);
    vec3 specular = calcSpecular(light.lightShading.specular, normal, fragToLight);

    if (light.direction != 0) {
        Fatt*= intensity;
    }

    return (ambient + diffuse + specular) *Fatt;
}


vec3 calcAmbient(vec3 ambience) {
    return ambience * vec3(texture(material.diffuse, fTexelCoord));
}


vec3 calcDiffuse(vec3 diffuse, vec3 normal, vec3 lightDir) {
    float diffuseSF = max(dot(normal, lightDir), 0.0f);
    return diffuseSF * diffuse * vec3(texture(material.diffuse, fTexelCoord));
}


vec3 calcSpecular(vec3 specular, vec3 normal, vec3 lightDir) {
    vec3 fragToCam = normalize(cameraPos - fWorldPos);
    vec3 reflectedRay = reflect(-lightDir, normal);
    float specSF = pow(max(dot(fragToCam, reflectedRay), 0.0f), material.shininess);
    return specular * specSF * vec3(texture(material.specular, fTexelCoord));
}