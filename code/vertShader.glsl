#version 430

layout (location = 0) in vec3 position;
uniform float x_offset;
uniform float y_offset;
uniform bool is_gradient_enabled;
out vec4 vertex_color;

void main(void)
{
  if (gl_VertexID == 0) {
    gl_Position = vec4(-0.25 + x_offset, -0.35 + y_offset, 0.0, 1.0);
  } else if (gl_VertexID == 1) {
    gl_Position = vec4(-0.25 + x_offset, 0.35 + y_offset, 0.0, 1.0);
  } else if (gl_VertexID == 2) {
    gl_Position = vec4(0.25 + x_offset, 0.0 + y_offset, 0.0, 1.0);
  }

  if (is_gradient_enabled) {
    vertex_color = vec4(gl_Position) + vec4(0.5f, 0.5f, 0.5f, 0.5f);
  } else {
    vertex_color = vec4(0.0f, 0.7f, 0.7f, 1.0f);
  }
}