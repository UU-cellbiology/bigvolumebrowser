// --- CONFIGURATION: Cube Selection (7x7 Bitmask) ---
// Each integer controls one row of 7 cubes from left-to-right using binary digits (1 = show, 0 = hide).
bool isCubeActive(int x, int y) {
    int rowMasks[7] = int[7](
        92,  // Row 0 (Bottom)
        68,  // Row 1
        85,  // Row 2
        85,  // Row 3 (Center)
        85,  // Row 4
        64,  // Row 5
        127  // Row 6 (Top)
    );
    return ((rowMasks[y] >> x) & 1) == 1;
}

// Target uniforms & IO
in vec2 posW;
out vec4 fragColor;

uniform float fTime;
uniform vec2 u_renderSize;
uniform vec3 u_uu; // Right vector (X)
uniform vec3 u_vv; // Up vector (Y)
uniform vec3 u_ww; // Forward/Look vector (Z)

uniform float scale;

// 3D Rotation matrix around X-axis
mat3 rotateX(float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return mat3(
        1.0, 0.0, 0.0,
        0.0,   c,  -s,
        0.0,   s,   c
    );
}

// 3D Rotation matrix around Y-axis (for orbiting)
mat3 rotateY(float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return mat3(
           c, 0.0,   s,
         0.0, 1.0, 0.0,
          -s, 0.0,   c
    );
}

mat3 rotateZ(float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return mat3(
           c,  -s,  0.0,
           s,   c,  0.0,
          0.0, 0.0, 1.0
    );
}

// --- DISTANCE FUNCTIONS ---

float sdBox(vec3 p, vec3 b, float r) {
    vec3 q = abs(p) - b;
    return length(max(q, 0.0)) + min(max(q.x, max(q.y, q.z)), 0.0) - r;
}

// Scene SDF: Combines active cubes into one distance field
float map(vec3 p) {
    float minDist = 1e5;
    
    float cubeSize = 0.35;     // Half-extent of each cube
    float spacing = 1.0;       // Distance between cube centers
    float cornerRadius = 0.04; // Edge roundness
    
    // Evaluate 7x7 grid bounds
    for (int y = 0; y < 7; y++) {
        for (int x = 0; x < 7; x++) {
            if (!isCubeActive(x, y)) continue;

            vec3 cubeCenter = vec3(
                float(x - 3) * spacing,
                float(y - 3) * spacing,
                0.0
            );

            float d = sdBox(p - cubeCenter, vec3(cubeSize), cornerRadius);
            minDist = min(minDist, d);
        }
    }

    return minDist;
}

// --- LIGHTING & SHADOWS ---

vec3 calcNormal(vec3 p) {
    vec2 e = vec2(0.001, 0.0);
    return normalize(vec3(
        map(p + e.xyy) - map(p - e.xyy),
        map(p + e.yxy) - map(p - e.yxy),
        map(p + e.yyx) - map(p - e.yyx)
    ));
}

float calcSoftShadow(vec3 ro, vec3 rd, float mint, float maxt, float k) {
    float res = 1.0;
    float t = mint;
    for (int i = 0; i < 32; i++) {
        float h = map(ro + rd * t);
        res = min(res, k * h / t);
        t += clamp(h, 0.02, 0.2);
        if (res < 0.001 || t > maxt) break;
    }
    return clamp(res, 0.0, 1.0);
}

float calcAO(vec3 pos, vec3 normal) {
    float occ = 0.0;
    float sca = 1.0;
    for (int i = 0; i < 5; i++) {
        float h = 0.01 + 0.12 * float(i) / 4.0;
        float d = map(pos + h * normal);
        occ += (h - d) * sca;
        sca *= 0.95;
    }
    return clamp(1.0 - 3.0 * occ, 0.0, 1.0);
}

// --- MAIN RENDERING ---

void main() {
    // 1. Aspect ratio correction relative to center [-1, 1]
    vec2 uv = posW * 2.0 - 1.0;
    if (u_renderSize.y > 0.0) {
        uv.x *= u_renderSize.x / u_renderSize.y;
    }

    float time = fTime * 0.001;
    

    // 2. Camera setup using target basis vectors & orbit matrix
    float dist = -22.0 * scale;
    mat3 rotOrbit = rotateY(time * 0.3 - 0.3) * rotateX(0.05) * rotateZ(3.14);

    vec3 base_ro = u_ww * dist;
    vec3 ro = rotOrbit * base_ro;

    vec3 cu = rotOrbit * u_uu;
    vec3 cv = rotOrbit * u_vv;
    vec3 cw = rotOrbit * u_ww;

    vec3 rd = normalize(uv.x * cu + uv.y * cv + 1.5 * cw);

    // 3. Surface Raymarching
    float t = 0.0;
    float tMax = 30.0;
    for (int i = 0; i < 100; i++) {
        vec3 p = ro + rd * t;
        float d = map(p);
        if (d < 0.001 || t > tMax) break;
        t += d;
    }

    // Vignetted background gradient
    vec3 col = mix(vec3(0.05, 0.07, 0.12), vec3(0.01, 0.01, 0.02), length(uv) * 0.8);

    // 4. Full Lighting Evaluation
    if (t < tMax) {
        vec3 pos = ro + rd * t;
        vec3 N = calcNormal(pos);
        vec3 lightPos = vec3(5.0, 8.0, -6.0);
        vec3 L = normalize(lightPos - pos);
        vec3 V = -rd;
        vec3 H = normalize(L + V);

        vec3 baseBlueColor = vec3(0.08, 0.45, 0.95);
        float NdotL = max(dot(N, L), 0.0);
        float shadow = calcSoftShadow(pos + N * 0.002, L, 0.02, 10.0, 16.0);
        float ao = calcAO(pos, N);

        float spec = pow(max(dot(N, H), 0.0), 32.0) * NdotL;

        vec3 ambient = vec3(0.1, 0.15, 0.25) * ao;
        vec3 diffuse = baseBlueColor * NdotL * shadow;
        vec3 specular = vec3(0.8, 0.9, 1.0) * spec * shadow;

        col = ambient + diffuse + specular;

        // Soft rim illumination
        float rim = 1.0 - max(dot(V, N), 0.0);
        col += vec3(0.2, 0.5, 1.0) * pow(rim, 3.0) * 0.5;
    }

    // Gamma correction
    col = pow(col, vec3(1.0 / 2.2));
    fragColor = vec4(col, 1.0);
}