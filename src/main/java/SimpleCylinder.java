import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import org.joml.Matrix4f;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class SimpleCylinder {

    public static final int SCREEN_WIDTH = 600;
    public static final int SCREEN_HEIGHT = 600;
    public static float CAMERA_RADIUS = 3.0f;

    // The window handle
    private long window;
    boolean pressed;


    private int VBO_pos;
    private int VBO_norm;
    private int VBO_tex;

    private Shader shader;

    public void run() {
        init();
        loop();

        shader.dispose();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }



    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

/*        glEnable(GL_CULL_FACE);
	    glCullFace(GL_BACK);
	    glFrontFace(GL_CW);*/

        glEnable(GL_DEPTH_TEST);



        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*
            glfwGetFramebufferSize(window, pWidth, pHeight);
            glViewport(0, 0, pWidth.get(0), pHeight.get(0));
        } // the stack frame is popped automatically

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            glViewport(0, 0, width, height);
        });

        shader = new Shader("src/shaders/vertex.shader", "src/shaders/fragment.shader");

        Matrix4f view = new Matrix4f();
        final Vector3f cameraPos = new Vector3f(0.0f, 2.0f, 3.0f);
        final Vector3f cameraFront = new Vector3f(0.0f, -1.0f, 0.0f);
        final Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);

        // set up vertex data (and buffer(s)) and configure vertex attributes
        // ------------------------------------------------------------------
//        float vertices[] = {
//                -0.5f,  0.5f, 0.0f, 0.0f, 0.0f,0.0f,
//                0.5f,  -0.5f, 0.0f, 0.0f, 0.0f,0.0f,
//                -0.5f,  -0.5f, 0.0f, 0.0f, 0.0f,0.0f,
//
//                -0.5f,  0.5f,0.0f, 0.0f, 0.0f,0.0f,
//                0.5f,  0.5f,0.0f, 0.0f, 0.0f,0.0f,
//                0.5f,  -0.5f,0.0f, 0.0f, 0.0f,0.0f,};


        // rendering the top of the cylinder


//        int segmentCount = 3;
//        int sectorCount = 20;
//        float radius = .5f;
//
//
//        int fanIndicesCount = sectorCount + 2;
//        int stripIndicesCount = 2 * sectorCount + 2;
//
//        int indicesSize = segmentCount * stripIndicesCount + 2 * fanIndicesCount;
//        int verticesSize = ((segmentCount + 2) * sectorCount + 2 ) * 6;
//
//
//        int southCenterIndex = (verticesSize - 2 * 6);
//        int northCenterIndex = verticesSize - 6;
//
//        float[] vertices = new float[verticesSize];
//
//        int verticesOffset = 0;

//        for (int i = 0; i < segmentCount; i++) {
//            float[] segmentVertices = getCircleVertices(sectorCount, radius, -.5f + (float)i/(segmentCount-1));
//
//
//
//            for (int k = 0; k < segmentVertices.length; k++) {
//                vertices[k + verticesOffset] = segmentVertices[k];
//            }
//            verticesOffset += segmentVertices.length;
//
//        }
//
//
//        float[] southVertices = getCircleVertices(sectorCount, radius, -.5f);
//        float[] northVertices = getCircleVertices(sectorCount, radius, .5f);


//        for (int i = 0; i < southVertices.length; i++) {
//            vertices[i+verticesOffset] = southVertices[i];
//        }
//
//        verticesOffset += southVertices.length;
//
//        for (int i = 0; i < northVertices.length; i++) {
//            vertices[i + verticesOffset] = northVertices[i];
//        }
//

//        //setting south center vertex values
//        vertices[southCenterIndex] = 0;
//        vertices[southCenterIndex + 1] = -.5f;
//        vertices[southCenterIndex + 2] = 0;
//        vertices[southCenterIndex + 3] = 0;
//        vertices[southCenterIndex + 4] = -1;
//        vertices[southCenterIndex + 5] = 0;
//
//        //setting north center vertex values
//        vertices[northCenterIndex] = 0;
//        vertices[northCenterIndex + 1] = .5f;
//        vertices[northCenterIndex + 2] = 0;
//        vertices[northCenterIndex + 3] = 0;
//        vertices[northCenterIndex + 4] = 1;
//        vertices[northCenterIndex + 5] = 0;
//
//
//        int[] indices = new int[indicesSize];
//
//        int indexOffset = 0;

//        for (int i = 0; i < segmentCount; i++) {
//            int[] stripIndices = getStripIndices(sectorCount, i * sectorCount, (i+1) * sectorCount);
//
//
//            for (int k = 0; k < stripIndices.length; k++) {
//                indices[k + indexOffset] = stripIndices[k];
//            }
//            indexOffset += stripIndices.length;
//
//        }
//
//
//
//        int[] southIndices = getFanIndices(sectorCount, southCenterIndex/6 , (segmentCount) * sectorCount);
//        int[] northIndices = getFanIndices(sectorCount, northCenterIndex/6 , (segmentCount+1) * sectorCount);


//        for (int i = 0; i < southIndices.length; i++) {
//            indices[i + indexOffset] = southIndices[i];
//        }
//
//        indexOffset += southIndices.length;
//
//        for (int i = 0; i < northIndices.length; i++) {
//            indices[indexOffset + i] = northIndices[i];
//        }
//
        Surface terrain = new Surface(5, 3, 400);


        int VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        storeDataInAttributeList(0, 3, terrain.getVertices());

        storeDataInAttributeList(1, 2, terrain.getTextureCoords());

        storeDataInAttributeList(2, 3, terrain.getNormals());

        GL30.glBindVertexArray(0);
//
//        VBO_pos = glGenBuffers();
//        glBindBuffer(GL_ARRAY_BUFFER, VBO_pos);
//        glBufferData(GL_ARRAY_BUFFER, terrain.getVertices(), GL_STATIC_DRAW);
//
//        VBO_norm = glGenBuffers();
//        glBindBuffer(GL_ARRAY_BUFFER, VBO_norm);
//        glBufferData(GL_ARRAY_BUFFER, terrain.getNormals(), GL_STATIC_DRAW);
//
//        VBO_tex = glGenBuffers();
//        glBindBuffer(GL_ARRAY_BUFFER, VBO_tex);
//        glBufferData(GL_ARRAY_BUFFER, terrain.getTextureCoords(), GL_STATIC_DRAW);
//
//        int INB = glGenBuffers();
//        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, INB);
//        glBufferData(GL_ELEMENT_ARRAY_BUFFER, terrain.getIndices(), GL_STATIC_DRAW);



        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        Matrix4f model = new Matrix4f();
        Matrix4f projection = new Matrix4f();

        //position
//        glVertexAttribPointer(0, 3, GL_FLOAT, false, 4 * 8, 0);
//        glEnableVertexAttribArray(0);
//
//        //uv
//        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * 8, 3 * 4);
//        glEnableVertexAttribArray(1);
//        //normal
//        glVertexAttribPointer(2, 3, GL_FLOAT, false, 4 * 8, 5 * 4);
//        glEnableVertexAttribArray(2);



        // glBindBuffer(GL_ARRAY_BUFFER, 0);
        shader.use();


        GLFW.glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override public void invoke (long win, double dx, double dy) {
                CAMERA_RADIUS += dy * 0.5f;
            }
        });

        double yaw = Math.toRadians(-90), pitch = Math.toRadians(0);
        final double[] xPos = new double[1];
        final double[] yPos = new double[1];
        final Vector2f lastMousePos = new Vector2f();

        GLFW.glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                pressed = action == 1;
                glfwGetCursorPos(window, xPos, yPos);
                lastMousePos.x = (float) xPos[0];
                lastMousePos.y = (float) yPos[0];
            }
        });

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.

        while (!glfwWindowShouldClose(window)) {
            // set the view matrix

//            lookAt.rotate(0.01f, new Vector3f(0, 1, 0));

            float mouseSensitivity = 0.0001f;
            float cameraSpeed = 0.02f;

            if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
                cameraPos.add(new Vector3f(cameraFront).mul(cameraSpeed));
            if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
                cameraPos.sub(new Vector3f(cameraFront).mul(cameraSpeed));
            if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
                cameraPos.add(new Vector3f(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed));
            if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
                cameraPos.sub(new Vector3f(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed));

            if (pressed) {

                glfwGetCursorPos(window, xPos, yPos);

                float xOffset = lastMousePos.x - (float) xPos[0];
                float yOffset = lastMousePos.y - (float) yPos[0];

                yaw -= xOffset * mouseSensitivity;
                pitch += yOffset * mouseSensitivity;


                if (pitch > (float) Math.toRadians(89.0f))
                    pitch = (float) Math.toRadians(89.0f);
                if (pitch < (float) Math.toRadians(-89.0f))
                    pitch = (float) Math.toRadians(-89.0f);
            }

            // update camera
            cameraFront.set(
                    (float) ( Math.cos(pitch) * Math.cos(yaw)),
                    (float) ( Math.sin(pitch)),
                    (float) ( Math.cos(pitch) * Math.sin(yaw))
            );
            cameraFront.normalize();


            view.identity();
            view.lookAt(cameraPos, new Vector3f(cameraPos).add(cameraFront), cameraUp);
            projection.identity();
            projection.perspective((float) Math.toRadians(45.0f), SCREEN_WIDTH / SCREEN_HEIGHT, 0.1f, 100.0f);
            // Set the clear color
            glClearColor(.2f, .4f, .7f, .4f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            model.identity();

            shader.use();

            shader.setMatrix("model", model);
            shader.setMatrix("view", view);
            shader.setMatrix("projection", projection);


            shader.setVec3("objectColor", 1.0f, 0.5f, 0.31f);
            shader.setVec3("lightColor", 1.0f, 1.0f, 1.0f);
            shader.setVec3("lightPos", (float) (Math.sin(glfwGetTime()) * 2.0f), 2.0f, (float) (Math.cos(glfwGetTime()) * 2.0f));
            shader.setVec3("viewPos", 2.0f, 2.0f, 2.0f);


            GL30.glBindVertexArray(VAO);
            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glEnableVertexAttribArray(2);
//            Texture texture = terrain.getTexture();
//            shader.loadShineVariables(texture.getShininess(), texture.getSpecularStrength());
//            GL13.glActiveTexture(GL13.GL_TEXTURE0);
//            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getID());
//            Texture heightMap = terrain.getHeightMap();
//            GL13.glActiveTexture(GL13.GL_TEXTURE1);
//            GL11.glBindTexture(GL11.GL_TEXTURE_2D, heightMap.getID());
//            glDrawElements(GL_TRIANGLE_STRIP, terrain.getSize() , GL_UNSIGNED_INT, 0);

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new SimpleCylinder().run();
    }

//    private float[] getCircleVertices(int sectorCount, float radius, float y) {
//
//        float[] vertices = new float[6 * sectorCount];
//
//        for (int i = 0; i < sectorCount; i++) {
//
//            int index = i * 6;
//
//            float angle = (float) ((float)i / sectorCount * Math.PI * 2);
//            float x = (float) Math.sin(angle);
//            float z = (float) Math.cos(angle);
//
//            vertices[index] = radius * x;
//            vertices[index + 1] = y;
//            vertices[index + 2] = radius * z;
//            vertices[index + 3] = x;
//            vertices[index + 4] = 0;
//            vertices[index + 5] = z;
//        }
//
//
//        return vertices;
//    }
//
//    private int[] getFanIndices(int sectorCount, int centerIndex, int segmentIndex){
//        int[] indices = new int[sectorCount + 2];
//        indices[0] = centerIndex;
//
//        for (int i = 1; i <= sectorCount; i++) {
//            indices[i] = segmentIndex + i - 1;
//        }
//
//        indices[sectorCount + 1] = segmentIndex;
//
//        return indices;
//    }
//
//
//    private int[] getStripIndices(int sectorCount, int firstCircleIndex, int secondCircleIndex){
//        int[] indices = new int[2 * sectorCount + 2];
//
//        for (int i = 0; i < sectorCount; i++) {
//            int index = i * 2;
//
//            indices[index] = firstCircleIndex + i;
//            indices[index+1] = secondCircleIndex + i;
//        }
//
//        indices[2 * sectorCount] = firstCircleIndex;
//        indices[2 * sectorCount + 1] = secondCircleIndex;
//
//
//        return indices;
//    }

    private void storeDataInAttributeList(int attributeNumber, int coordinateSize,float[] data) {
        int vboID = GL15.glGenBuffers();

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        FloatBuffer buffer = storeDataInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attributeNumber,coordinateSize,GL11.GL_FLOAT,false,0,0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private FloatBuffer storeDataInFloatBuffer(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

}
