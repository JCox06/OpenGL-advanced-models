package uk.co.jcox.gllights;


import org.lwjgl.assimp.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class GLLoader {


    private GLLoader() {

    }

    public static final int ASSIMP_POST_PROCESS =
              Assimp.aiProcess_Triangulate
            | Assimp.aiProcess_FlipUVs
            | Assimp.aiProcess_OptimizeMeshes
            | Assimp.aiProcess_GenNormals;

    public static final int VERTICES_3 = 3;
    public static final int VERTICES_2 = 2;


    public static Model importModel(File modelPath) {
        //Step 1 - Import the file into an AIScene object
        if (! modelPath.isFile()) {
            throw new RuntimeException();
        }

        try (final AIScene aiScene = Assimp.aiImportFile(modelPath.toString(), ASSIMP_POST_PROCESS);) {
            if (aiScene == null) {
                throw new RuntimeException();
            }

            //Step 2 - Go through each node and collect mesh data from the scene object
            final List<Mesh> meshList = new ArrayList<>();
            processNode(aiScene.mRootNode(), aiScene, meshList);

            return new Model(modelPath.toString(), meshList);
        }
    }

    private static void processNode(AINode aiNode, AIScene aiScene, List<Mesh> meshList) {
        //Step 1 - Go through each of the meshes in the root node
        for (int i = 0; i < aiNode.mNumMeshes(); i++) {
            long meshPointer = aiScene.mMeshes().get(aiNode.mMeshes().get(i));
            try (AIMesh aiMesh = AIMesh.create(meshPointer);) {
                Mesh processedMesh = aiMeshToMesh(aiMesh);
                meshList.add(processedMesh);
            }
        }

        //Step 2 - Recursively run through all children from the root node
        for (int i = 0; i < aiNode.mNumChildren(); i++) {
            long nodePointer = aiNode.mChildren().get(i);
            try (AINode childNode = AINode.create(nodePointer)) {
                processNode(childNode, aiScene, meshList);
            }
        }
    }

    private static Mesh aiMeshToMesh(AIMesh aiMesh) {
        float[] vertexPos = getFloat3Buffer(aiMesh::mVertices);
        float[] vertexTex = getTexels(aiMesh.mTextureCoords(0));
        float[] vertexNorm = getFloat3Buffer(aiMesh::mNormals);
        int[] indices = getIndices(aiMesh);

        return buildGLGeometry(vertexPos, vertexTex, vertexNorm, indices);
    }

    private static float[] getFloat3Buffer(Supplier<AIVector3D.Buffer> vector3DSupplier) {
        AIVector3D.Buffer vertices = vector3DSupplier.get();
        float[] buffer = new float[vertices.remaining() * VERTICES_3];
        int pos = 0;
        while (vertices.hasRemaining()) {
            AIVector3D genericVec = vertices.get();
            buffer[pos++] = genericVec.x();
            buffer[pos++] = genericVec.y();
            buffer[pos++] = genericVec.z();
        }
        return buffer;
    }

    private static float[] getTexels(AIVector3D.Buffer texData) {
        float[] buffer = new float[texData.remaining() * VERTICES_2];
        int pos = 0;
        while (texData.hasRemaining()) {
            AIVector3D genericVec = texData.get();
            buffer[pos++] = genericVec.x();
            buffer[pos++] = genericVec.y();
        }

        return buffer;
    }

    private static int[] getIndices(AIMesh aiMesh) {
        List<Integer> indices = new ArrayList<>();
        int faces = aiMesh.mNumFaces();
        AIFace.Buffer aiFaces = aiMesh.mFaces();
        for (int i = 0; i < faces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.hasRemaining()) {
                indices.add(buffer.get());
            }
        }

        return indices.stream().mapToInt(Integer::intValue).toArray();
    }

    private static Mesh buildGLGeometry(float[] vertexPos, float[] vertexTex, float[] vertexNorm, int[] indices) {
        final int vertexArray = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vertexArray);

        //Pos Buffer
        final int posBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, posBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexPos, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);

        //UV Buffer
        final int texBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexTex, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(1);

        //Normal Buffer
        final int normalBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexNorm, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(2);

        //Indices
        final int indexBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

        return new Mesh(vertexArray, List.of(posBuffer, texBuffer, normalBuffer, indexBuffer), indices.length);
    }
}