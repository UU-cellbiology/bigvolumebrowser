// --- CONFIGURATION ---
#define STACK_COUNT 6        // Half-count of boxes above/below origin
#define CUBE_SIZE 0.45       // Width and depth half-extent
#define CUBE_HEIGHT 0.12     // Reduced height half-extent (flatter box shape)
#define GAP 0.1              // Vertical gap between adjacent boxes
#define ROTATION_SPEED 1.2   // Rotation speed multiplier

out vec4 fragColor;
in vec2 posW;


uniform float scale;
uniform float fTime;
uniform vec2 u_renderSize;
uniform vec3 u_uu; // Right vector (X)
uniform vec3 u_vv; // Up vector (Y)
uniform vec3 u_ww; // Forward/Look vector (Z)

// 3D Rotation matrix around the X-axis
mat3 rotateX(float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return mat3(
        1.0, 0.0, 0.0,
        0.0,   c,  -s,
        0.0,   s,   c
    );
}

// 3D Rotation matrix around the Y-axis (for orbiting)
mat3 rotateY(float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return mat3(
           c, 0.0,   s,
         0.0, 1.0, 0.0,
          -s, 0.0,   c
    );
}

// 2D Rotation matrix around Y-axis (for local boxes)
mat2 rot2D(float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return mat2(c, -s, s, c);
}

// Signed Distance Function for a Box
float sdBox(vec3 p, vec3 b) {
    vec3 q = abs(p) - b;
    return length(max(q, 0.0)) + min(max(q.x, max(q.y, q.z)), 0.0);
}

// Distance Estimator for the Stacked Boxes
float map(vec3 p, float time) {
    float pitch = (CUBE_HEIGHT * 2.0) + GAP;
    float centerIdx = floor((p.y + pitch * 0.5) / pitch);
    
    float d = 1e5;
    vec3 extents = vec3(CUBE_SIZE, CUBE_HEIGHT, CUBE_SIZE);

    for (int i = -1; i <= 1; i++) {
        float cellIdx = clamp(centerIdx + float(i), -float(STACK_COUNT), float(STACK_COUNT));
        
        vec3 pLocal = p;
        pLocal.y -= cellIdx * pitch;

        float phase = cellIdx * 0.35; 
        float angle = time * ROTATION_SPEED + phase;
        pLocal.xz = rot2D(angle) * pLocal.xz;

        float dBox = sdBox(pLocal, extents) - 0.015;
        d = min(d, dBox);
    }
    //float dFloor = p.y + (float(STACK_COUNT) + 3.5) * pitch;

    //return min(d, dFloor);

    return d;
}

// Surface Normal Calculation
vec3 calcNormal(vec3 p, float time) {
    vec2 e = vec2(0.001, 0.0);
    return normalize(vec3(
        map(p + e.xyy, time) - map(p - e.xyy, time),
        map(p + e.yxy, time) - map(p - e.yxy, time),
        map(p + e.yyx, time) - map(p - e.yyx, time)
    ));
}

void main() {
    // Aspect ratio correction relative to center [-1, 1]
    vec2 uv = posW * 2.0 - 1.0;
    if (u_renderSize.y > 0.0) {
        uv.x *= u_renderSize.x / u_renderSize.y;
    }

    float time = fTime * 0.001;

    // --- CAMERA ROTATION USING UNIFORMS ---
    float dist = -4.5;
    
    // Rotation matrix for orbiting around the stack center (0,0,0)
    mat3 rotOrbit = rotateY(time * 0.5) * rotateX(3.14159265 * 0.05);

    // 1. Base camera origin derived from u_ww
    vec3 base_ro = u_ww * dist * scale;

    // 2. Rotate camera position around world origin (0,0,0)
    vec3 ro = rotOrbit * base_ro;

    // 3. Rotate basis vectors directly (keeps ray direction aligned to target)
    vec3 cu = rotOrbit * u_uu;
    vec3 cv = rotOrbit * u_vv;
    vec3 cw = rotOrbit * u_ww;

    // Construct ray direction using the transformed basis vectors
    vec3 rd = normalize(uv.x * cu + uv.y * cv + 1.5 * cw);

    // Raymarching Loop
    float t = 0.0;
    float tMax = 20.0;

    for (int i = 0; i < 120; i++) {
        vec3 p = ro + rd * t;
        float d = map(p, time);
        if (d < 0.0008 || t > tMax) break;
        t += d;
    }

    // Shading
    vec3 fogColor = vec3(0.03, 0.05, 0.04);
    vec3 col = fogColor;

    if (t < tMax) {
        vec3 p = ro + rd * t;
        vec3 n = calcNormal(p, time);
        vec3 lightDir = normalize(vec3(1.5, 3.0, 2.0));

        // Diffuse & Ambient
        float diff = max(dot(n, lightDir), 0.0);
        float ambient = 0.25;

        // Ambient Occlusion
        float ao = clamp(map(p + n * 0.08, time) / 0.08, 0.0, 1.0);

        // Specular highlight
        vec3 halfVec = normalize(lightDir - rd);
        float spec = pow(max(dot(n, halfVec), 0.0), 32.0);

        // --- LOCAL FACE-DEPENDENT COLORING ---
        float pitch = (CUBE_HEIGHT * 2.0) + GAP;
        float cellIdx = floor((p.y + pitch * 0.5) / pitch);
        
        // Transform normal back into local box space
        float phase = cellIdx * 0.35; 
        float angle = time * ROTATION_SPEED + phase;
        
        vec3 localN = n;
        localN.xz = rot2D(-angle) * localN.xz; // Inverse rotation

        // Side faces (X-axis) = Green | Front/Back faces (Z-axis) = White
        float faceFactor = abs(localN.x); 
        
        vec3 pureWhite = vec3(0.95, 0.98, 0.96);
        vec3 vibrantGreen = vec3(0.05, 0.75, 0.30);
        vec3 baseColor = mix(pureWhite, vibrantGreen, faceFactor);

        // Top/bottom cap color tint
        baseColor = mix(baseColor, vec3(0.4, 0.85, 0.6), abs(localN.y));

        col = baseColor * (diff * 0.8 + ambient) * ao + spec * vec3(0.8);

        // Fog
        float fogFactor = 1.0 - exp(-0.005 * t * t);
        col = mix(col, fogColor, clamp(fogFactor, 0.0, 1.0));
    }

    fragColor = vec4(col, 1.0);
}