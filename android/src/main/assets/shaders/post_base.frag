#version 130

precision mediump float;

varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main() {
  vec2 uv=v_texCoords;
  vec4 color = texture2D(u_texture, uv);
  color.a=1.0;
  
  //color.b=1.0;
  
  gl_FragColor = color*v_color;
}
