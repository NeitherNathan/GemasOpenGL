package br.pucpr.mage;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Representa a malha poligonal estática. A malha é formada por um conjunto de vértices, definidos por buffers de
 * atributos, um indexbuffer (opcional) que indica como os triangulos são compostos, um conjunto de uniforms (como a
 * textura) e o shader que será usado para o desenho.
 */
public class Mesh {
    private int id;
    private Shader shader;
    private IndexBuffer indexBuffer;

    private Map<String, ArrayBuffer> attributes = new HashMap<>();
    private Map<String, Uniform> uniforms = new HashMap<>();
    private boolean wireframe = false;

    Mesh() {
        id = glGenVertexArrays();
    }

    /**
     * @return O id dessa malha (VAO) no OpenGL
     */
    public int getId() {
        return id;
    }

    /**
     * Define o index buffer da malha. Esse método não pode ser chamado diretamente. Utilize os métodos de index buffer
     * da classe MeshBuilder para isso.
     * @param indexBuffer O index buffer a ser definido.
     * @return A própria malha
     * @see MeshBuilder#setIndexBuffer(IndexBuffer)
     */
    Mesh setIndexBuffer(IndexBuffer indexBuffer) {
        this.indexBuffer = indexBuffer;
        return this;
    }

    /**
     * Altera o shader a ser utilizado no desenho da malha.
     * @param shader O shader a ser usado no desenho
     * @return A própria malha
     */
    public Mesh setShader(Shader shader) {
        this.shader = shader;
        return this;
    }

    /**
     * @return O shader associado a malha.
     */
    public Shader getShader() {
        return shader;
    }

    /**
     * Associa um buffer a malha.
     * @param name O nome do buffer a ser associado. Uma vez associado, o buffer não pode ser substituído.
     * @param data O ArrayBuffer com os dados do atributo.
     */
    void addAttribute(String name, ArrayBuffer data) {
        if (attributes.containsKey(name)) {
            throw new IllegalArgumentException("Attribute already exists: " + name);
        }
        if (data == null) {
            throw new IllegalArgumentException("Data can't be null!");
        }
        
        attributes.put(name, data);
    }

    /**
     * Define o valor de um uniforme dentro da malha
     * @param name Nome do uniforme
     * @param value valor a ser definido
     * @return A própria malha
     */
    private Mesh setUniform(String name, UniformType type, Object value) {
        if (value == null)
            uniforms.remove(name);
        else {
            uniforms.put(name, new Uniform(type, value));
        }
        return this;
    }

    /**
     * Define o valor de um uniforme dentro da malha
     * @param name Nome do uniforme
     * @param matrix valor a ser definido
     * @return A própria malha
     */
    public Mesh setUniform(String name, Matrix3f matrix) {
        return setUniform(name, UniformType.Matrix3f, matrix);
    }

    /**
     * Define o valor de um uniforme dentro da malha
     * @param name Nome do uniforme
     * @param matrix valor a ser definido
     * @return A própria malha
     */
    public Mesh setUniform(String name, Matrix4f matrix) {
        return setUniform(name, UniformType.Matrix4f, matrix);
    }

    /**
     * Define o valor de um uniforme dentro da malha
     * @param name Nome do uniforme
     * @param vector valor a ser definido
     * @return A própria malha
     */
    public Mesh setUniform(String name, Vector2f vector) {
        return setUniform(name, UniformType.Vector2f, vector);
    }

    /**
     * Define o valor de um uniforme dentro da malha
     * @param name Nome do uniforme
     * @param vector valor a ser definido
     * @return A própria malha
     */
    public Mesh setUniform(String name, Vector3f vector) {
        return setUniform(name, UniformType.Vector3f, vector);
    }
    /**
     * Define o valor de um uniforme dentro da malha
     * @param name Nome do uniforme
     * @param vector valor a ser definido
     * @return A própria malha
     */
    public Mesh setUniform(String name, Vector4f vector) {
        return setUniform(name, UniformType.Vector4f, vector);
    }

    /**
     * Define o valor de um uniforme dentro da malha
     * @param name Nome do uniforme
     * @param value valor a ser definido
     * @return A própria malha
     */
    public Mesh setUniform(String name, float value) {
        return setUniform(name, UniformType.Float, value);
    }

    /**
     * Define o valor de um uniforme dentro da malha
     * @param name Nome do uniforme
     * @param value valor a ser definido
     * @return A própria malha
     */
    public Mesh setUniform(String name, int value) {
        return setUniform(name, UniformType.Integer, value);
    }

    /**
     * Define o valor de um uniforme dentro da malha
     * @param name Nome do uniforme
     * @param value valor a ser definido
     * @return A própria malha
     */
    public Mesh setUniform(String name, boolean value) {
        return setUniform(name, UniformType.Boolean, value);
    }

    /**
     * Define se a malha sera desenhada no modo wireframe ou não
     * @param wireframe True para desenhar wireframe
     * @return A própria malha.
     */
    public Mesh setWireframe(boolean wireframe) {
        this.wireframe = wireframe;
        return this;
    }

    /**
     * @return True se desenhará apenas o wireframe, falso se a malha será desenhada de maneira sólida
     */
    public boolean isWireframe() {
        return wireframe;
    }
    /**
     * Desenha a malha.
     * @return A própria mesh
     */
    public Mesh draw() {
        if (shader == null || attributes.size() == 0) {
            return this;
        }

        glPolygonMode(GL_FRONT_AND_BACK, wireframe ? GL_LINE : GL_FILL);

        //Precisamos dizer qual VAO iremos desenhar
        glBindVertexArray(id);

        //E qual shader program irá ser usado durante o desenho
        shader.bind();

        //Associação dos ArrayBuffers de atributos (value) as suas variáveis no shader (key)
        //----------------------------------------------------------------------------------
        for (Map.Entry<String, ArrayBuffer> attribute : attributes.entrySet()) {
            ArrayBuffer buffer = attribute.getValue();
            shader.setAttribute(attribute.getKey(), buffer);
        }

        //Associação dos uniforms (value) as suas variáveis no shader (key)
        //---------------------------------------------------
        for (Map.Entry<String, Uniform> entry : uniforms.entrySet()) {            
            entry.getValue().set(shader, entry.getKey());
        }

        if (indexBuffer == null) {
            //Se não houver index buffer, busca pelo primeiro ArrayBuffer e desenha com ele.
            attributes.values().iterator().next().draw();
        } else {
            //Se houver, desenha com o index buffer.
            indexBuffer.draw();
        }

        // Faxina
        for (String attribute : attributes.keySet()) {
            shader.setAttribute(attribute, null);
        }
        shader.unbind();
        glBindVertexArray(0);
        return this;
    }
}
