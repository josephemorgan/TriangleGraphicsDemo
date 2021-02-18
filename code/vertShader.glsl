#version 430

uniform float x_offset;
uniform float y_offset;
uniform bool is_gradient_enabled;
uniform float scale_factor;
out vec4 vertex_color;

void main(void)
{
  mat4 scaleMatrix = mat4(
  scale_factor, 0.0, 0.0, 0.0,
  0.0, scale_factor, 0.0, 0.0,
  0.0, 0.0, scale_factor, 0.0,
  0.0, 0.0, 0.0, 1.0);

  mat4 translationMatrix = mat4(
  1.0, 0.0, 0.0, 0.0,
  0.0, 1.0, 0.0, 0.0,
  0.0, 0.0, 1.0, 0.0,
  x_offset, y_offset, 0.0, 1.0);

  mat4 catMatrix = translationMatrix * scaleMatrix;

  if (gl_VertexID == 0) {
    gl_Position = catMatrix * vec4(-0.25, -0.35, 0.0, 1.0);
    if (is_gradient_enabled) {
      vertex_color =  vec4(0.8f, 0.0f, 0.0f, 1.0f);
    } else {
      vertex_color = vec4(0.0f, 0.7f, 0.7f, 1.0f);
    }
  } else if (gl_VertexID == 1) {
    gl_Position = catMatrix * vec4(-0.25, 0.35, 0.0, 1.0);
    if (is_gradient_enabled) {
      vertex_color =  vec4(0.0f, 0.8f, 0.0f, 1.0f);
    } else {
      vertex_color = vec4(0.0f, 0.7f, 0.7f, 1.0f);
    }
  } else if (gl_VertexID == 2) {
    gl_Position = catMatrix * vec4(0.25, 0.0, 0.0, 1.0);
    if (is_gradient_enabled) {
      vertex_color =  vec4(0.0f, 0.0f, 0.8f, 1.0f);
    } else {
      vertex_color = vec4(0.0f, 0.7f, 0.7f, 1.0f);
    }
  }

}