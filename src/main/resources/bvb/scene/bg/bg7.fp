#define IMAGE_SCALE 0.1    // Size of the bouncing image (0.1 to 1.0)
#define SPEED 0.05         // Movement speed

// Target uniforms & IO
in vec2 posW;
out vec4 fragColor;

uniform float fTime;
uniform vec2 u_renderSize;
uniform vec2 u_canvasSize;
uniform sampler2D logoImg; // Active texture unit
uniform float imgAspect;

// Bouncing movement helper: Maps linear time to a ping-pong [0, 1] wave
float pingPong(float t) {
    return abs(fract(t) * 2.0 - 1.0);
}

// Helper function: Converts Hue/Saturation/Value to RGB
vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec2 getPosScale() {
    float renderAspect = u_renderSize.x / u_renderSize.y;
    float canvasAspect = u_canvasSize.x / u_canvasSize.y;
    vec2 posScale = vec2(1.0);

    if (canvasAspect > renderAspect) {
        posScale.y = canvasAspect / renderAspect;
    } else {
        posScale.x = renderAspect / canvasAspect;
    }
    return posScale;
}

void main() {
    vec2 posScale = getPosScale();
    vec2 uv = 0.5 * ((posW * 2.0 - 1.0) * posScale + 1.0);

    float screenAspect = (u_renderSize.y > 0.0) ? (u_renderSize.x / u_renderSize.y) : 1.0;
    float time = fTime * 0.001;

    vec2 imgSize = vec2((IMAGE_SCALE * imgAspect) / screenAspect, IMAGE_SCALE);
    imgSize *= posScale;

    vec2 moveRange = vec2(1.0) - imgSize;
    moveRange = max(moveRange, vec2(0.0001));

    // Calculate progress on each axis
    float progressX = time * SPEED / moveRange.x;
    float progressY = time * (SPEED * 0.75) / moveRange.y;

    // Ping-pong trajectory
    vec2 pos;
    pos.x = pingPong(progressX) * moveRange.x;
    pos.y = pingPong(progressY) * moveRange.y;

    // --- BOUNCE COLOR LOGIC 
    // Multiplying progress by 2.0 counts BOTH directions (0.0 wall AND 1.0 wall)
    float bouncesX = floor(progressX * 2.0);
    float bouncesY = floor(progressY * 2.0);
    float totalBounces = bouncesX + bouncesY;

    // Generate a unique hue shift for every bounce hit
    float hue = fract(totalBounces * 0.61803398875);
    vec3 bounceColor = hsv2rgb(vec3(hue, 0.45, 1.0)); // Saturation: 0.75, Brightness: 1.0

    // Local UV coordinates relative to moving bounding box
    vec2 imgUV = (uv - pos) / imgSize;

    // Background gradient color
    vec3 col = mix(vec3(0.05, 0.07, 0.12), vec3(0.01, 0.01, 0.02), length(uv - 0.5) * 1.2);

    // Sample texture inside image frame
    if (imgUV.x >= 0.0 && imgUV.x <= 1.0 && imgUV.y >= 0.0 && imgUV.y <= 1.0) 
    {
        vec2 sampledUV = vec2(imgUV.x, 1.0 - imgUV.y);
        vec4 tex = texture(logoImg, sampledUV);

        // Tint texture RGB using the bounce color tint
        vec3 tintedImage = tex.rgb * bounceColor;

        // Preserve alpha transparency over background
        col = mix(col, tintedImage, tex.a);
    }

    fragColor = vec4(col, 1.0);
}