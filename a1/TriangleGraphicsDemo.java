package a1;

import javax.swing.*;
import static com.jogamp.opengl.GL4.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.*;

import java.awt.*;
import java.awt.event.*;

public class TriangleGraphicsDemo extends JFrame implements GLEventListener, KeyListener, MouseWheelListener {
	private int renderingProgram;
	private final int[] vao = new int[1];
	private final int[] vbo = new int[1];
	private float xOffset = 0.0f;
	private float yOffset = 0.0f;
	private float inc = 0.01f;
	private float theta = 0;
	private float scaleFactor = 1;
	private boolean moveHorizontalMode;
	private boolean moveVerticalMode;
	private boolean moveCircularMode;
	private boolean isGradientEnabled;
	private long lastFrameTimeMillis;

	private GLCanvas renderingCanvas;
	private JButton moveHorizontalButton;
	private JButton moveVerticalButton;
	private JButton moveCircularButton;

	public TriangleGraphicsDemo() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		moveHorizontalMode = true;
		moveVerticalMode = false;
		moveCircularMode = false;
		isGradientEnabled = false;
		buildWindow();
		renderingCanvas.setFocusable(true);
		renderingCanvas.requestFocus();
	}

	private void buildWindow() {
		setTitle("Homework 1");
		setSize(400, 400);

		renderingCanvas = new GLCanvas();
		JPanel renderingPanel = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		buttonPanel.setBorder(BorderFactory.createLineBorder(Color.red));
		moveHorizontalButton = new JButton("Move Horizontally");
		moveVerticalButton = new JButton("Move Vertically");
		moveCircularButton = new JButton("Move Circularly");
		JButton toggleGradientEnabledButton = new JButton("Toggle Color");
		JButton keyInfoButton = new JButton("?");
		GridBagConstraints constraints = new GridBagConstraints();

		renderingPanel.add(renderingCanvas);
		this.add(BorderLayout.CENTER, renderingCanvas);

		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1.0f;
		constraints.weighty = 1.0f;
		constraints.insets = new Insets(2, 2, 2, 2);
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 2;

		buttonPanel.add(moveHorizontalButton, constraints);

		constraints.gridx = 2;
		constraints.gridy = 0;
		buttonPanel.add(moveVerticalButton, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		buttonPanel.add(moveCircularButton, constraints);

		constraints.gridx = 2;
		constraints.gridy = 1;
		buttonPanel.add(toggleGradientEnabledButton, constraints);

		constraints.gridwidth = 1;
		constraints.gridheight = 2;
		constraints.gridx = 4;
		constraints.gridy = 0;
		constraints.weightx = .25;
		buttonPanel.add(keyInfoButton, constraints);

		moveVerticalButton.setEnabled(!moveVerticalMode);
		moveHorizontalButton.setEnabled(!moveHorizontalMode);
		moveCircularButton.setEnabled(!moveCircularMode);
		renderingCanvas.addGLEventListener(this);

		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, renderingCanvas);
		add(BorderLayout.SOUTH, buttonPanel);

		this.addMouseWheelListener(this);
		renderingCanvas.addKeyListener(this);
		buttonPanel.addKeyListener(this);
		moveHorizontalButton.addActionListener(e -> enableMoveHorizontalMode());

		moveVerticalButton.addActionListener(e -> enableMoveVerticalMode());

		moveCircularButton.addActionListener(e -> enableMoveCircularMode());

		toggleGradientEnabledButton.addActionListener(e -> toggleGradientEnabled());

		keyInfoButton.addActionListener(e -> showHelp());

		setVisible(true);
		Animator animator = new Animator(renderingCanvas);
		animator.start();
		pack();
	}

	private void enableMoveHorizontalMode() {
		moveHorizontalMode = true;
		moveVerticalMode = false;
		moveCircularMode = false;
		moveHorizontalButton.setEnabled(false);
		moveVerticalButton.setEnabled(true);
		moveCircularButton.setEnabled(true);
	}

	private void enableMoveVerticalMode() {
		moveHorizontalMode = false;
		moveVerticalMode = true;
		moveCircularMode = false;
		moveHorizontalButton.setEnabled(true);
		moveVerticalButton.setEnabled(false);
		moveCircularButton.setEnabled(true);
	}

	private void enableMoveCircularMode() {
		moveHorizontalMode = false;
		moveVerticalMode = false;
		moveCircularMode = true;
		moveHorizontalButton.setEnabled(true);
		moveVerticalButton.setEnabled(true);
		moveCircularButton.setEnabled(false);
	}

	private void toggleGradientEnabled() {
		isGradientEnabled = !isGradientEnabled;
	}

	private void showHelp() {
	    String helpString = "Available Keybinds:\n" +
				"h - Switch to Horizontal Movement Mode\n" +
				"v - Switch to Vertical Movement Mode\n" +
				"c - Switch to Circular Movement Mode\n" +
				"o - Toggle between solid and gradient color\n" +
				"Additionally, use of scroll wheel scales triangle size.";
	    JOptionPane.showMessageDialog(this, helpString);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		renderingProgram = Utils.createShaderProgram("a1/vertShader.glsl", "a1/fragShader.glsl");
		Utils.printProgramLog(renderingProgram);
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(1, vbo, 0);
		lastFrameTimeMillis = System.currentTimeMillis();
		System.out.println("GL Version: " + gl.glGetString(GL_VERSION));
		System.out.println("JOGL Version: " + Package.getPackage("com.jogamp.opengl").getImplementationVersion());
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glUseProgram(renderingProgram);
		moveObject(gl);

		int scale_factor_ptr = gl.glGetUniformLocation(renderingProgram, "scale_factor");
		gl.glProgramUniform1f(renderingProgram, scale_factor_ptr, scaleFactor);
		gl.glDrawArrays(GL_TRIANGLES,0,3);
		if (Utils.checkOpenGLError()) {
			Utils.printProgramLog(renderingProgram);
		}
	}

	private void moveObject(GL4 gl) {
		long currentTimeMillis = System.currentTimeMillis();
		long elapsedTimeMillis = currentTimeMillis - lastFrameTimeMillis;
		lastFrameTimeMillis = currentTimeMillis;
		long timeFactor = elapsedTimeMillis / 10L;
		if (moveHorizontalMode) {
			moveHorizontally(gl, timeFactor);
		} else if (moveVerticalMode) {
			moveVertically(gl, timeFactor);
		} else if (moveCircularMode) {
			moveCircularly(gl, timeFactor);
		}
	}

	private void moveHorizontally(GL4 gl, long timeFactor) {
	     xOffset += (inc * timeFactor);

	     if (xOffset >= 1.0f)
	     	inc = -0.01f;

	     if (xOffset <= -1.0f)
	     	inc = 0.01f;

		int x_offset_ptr = gl.glGetUniformLocation(renderingProgram, "x_offset");
		int is_gradient_enabled_ptr = gl.glGetUniformLocation(renderingProgram, "is_gradient_enabled");
		gl.glProgramUniform1f(renderingProgram, x_offset_ptr, xOffset);
		gl.glProgramUniform1i(renderingProgram, is_gradient_enabled_ptr, isGradientEnabled ? 1 : 0);
	}

	private void moveVertically(GL4 gl, long timeFactor) {
		yOffset += (inc * timeFactor);

		if (yOffset >= 1.0f)
			inc = -0.01f;

		if (yOffset <= -1.0f)
			inc = 0.01f;

		int y_offset_ptr = gl.glGetUniformLocation(renderingProgram, "y_offset");
		int is_gradient_enabled_ptr = gl.glGetUniformLocation(renderingProgram, "is_gradient_enabled");
		gl.glProgramUniform1f(renderingProgram, y_offset_ptr, yOffset);
		gl.glProgramUniform1i(renderingProgram, is_gradient_enabled_ptr, isGradientEnabled ? 1 : 0);
	}

	private void moveCircularly(GL4 gl, long timeFactor) {
		xOffset = 0.8f * (float) Math.cos(theta);
		yOffset = 0.8f * (float) Math.sin(theta);

		int x_offset_ptr = gl.glGetUniformLocation(renderingProgram, "x_offset");
		int y_offset_ptr = gl.glGetUniformLocation(renderingProgram, "y_offset");
		int is_gradient_enabled_ptr = gl.glGetUniformLocation(renderingProgram, "is_gradient_enabled");
		gl.glProgramUniform1f(renderingProgram, x_offset_ptr, xOffset);
		gl.glProgramUniform1f(renderingProgram, y_offset_ptr, yOffset);
		gl.glProgramUniform1i(renderingProgram, is_gradient_enabled_ptr, isGradientEnabled ? 1 : 0);

		theta += inc * timeFactor;
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	@Override
	public void dispose(GLAutoDrawable drawable) {}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		if (notches < 0) {
		    scaleFactor -= 0.1;
		} else {
			scaleFactor += 0.1;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyChar() == 'h') {
			enableMoveHorizontalMode();
		} else if (e.getKeyChar() == 'v') {
			enableMoveVerticalMode();
		} else if (e.getKeyChar() == 'c') {
			enableMoveCircularMode();
		} else if (e.getKeyChar() == 'o') {
			toggleGradientEnabled();
		}
	}
}