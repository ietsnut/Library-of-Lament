// Helper function to calculate barycentric coordinates
void barycentric(float x1, float z1, float x2, float z2, float x3, float z3,
                 float x, float z, float* out) {
    float det = (z2 - z3) * (x1 - x3) + (x3 - x2) * (z1 - z3);
    float l1 = ((z2 - z3) * (x - x3) + (x3 - x2) * (z - z3)) / det;
    float l2 = ((z3 - z1) * (x - x3) + (x1 - x3) * (z - z3)) / det;
    float l3 = 1.0f - l1 - l2;
    out[0] = l1;
    out[1] = l2;
    out[2] = l3;
}

// Helper function to check if point is inside triangle
bool inside(float* bary) {
    return bary[0] >= 0 && bary[0] <= 1 &&
           bary[1] >= 0 && bary[1] <= 1 &&
           bary[2] >= 0 && bary[2] <= 1;
}

// Helper function to calculate normal
float3 calculate_normal(float3 v0, float3 v1, float3 v2) {
    float3 edge1 = v1 - v0;
    float3 edge2 = v2 - v0;
    return normalize(cross(edge1, edge2));
}

// Simple height calculation kernel
__kernel void calculate_height(__global float* vertices,
                              __global int* indices,
                              __global float* result,
                              float xQuery,
                              float zQuery,
                              int triangleCount) {
    // Initialize result
    result[0] = 0.0f;  // height
    result[1] = 0.0f;  // found flag

    float bary[3];

    // Iterate through all triangles
    for (int t = 0; t < triangleCount; t++) {
        // Get vertex indices
        int idx0 = indices[t * 3 + 0] * 3;
        int idx1 = indices[t * 3 + 1] * 3;
        int idx2 = indices[t * 3 + 2] * 3;

        // Get vertex positions
        float x0 = vertices[idx0];
        float y0 = vertices[idx0 + 1];
        float z0 = vertices[idx0 + 2];

        float x1 = vertices[idx1];
        float y1 = vertices[idx1 + 1];
        float z1 = vertices[idx1 + 2];

        float x2 = vertices[idx2];
        float y2 = vertices[idx2 + 1];
        float z2 = vertices[idx2 + 2];

        // Calculate barycentric coordinates
        barycentric(x0, z0, x1, z1, x2, z2, xQuery, zQuery, bary);

        // Check if point is inside triangle
        if (inside(bary)) {
            result[0] = bary[0] * y0 + bary[1] * y1 + bary[2] * y2 + 1.7f;
            result[1] = 1.0f;  // Found
            return;
        }
    }
}

// Movement-based height calculation kernel
__kernel void calculate_height_movement(__global float* vertices,
                                       __global int* indices,
                                       __global float* result,
                                       float originX,
                                       float originY,
                                       float originZ,
                                       float movementX,
                                       float movementY,
                                       float movementZ,
                                       float slopeThreshold,
                                       int triangleCount) {
    // Initialize result with origin position
    result[0] = originX;
    result[1] = originY;
    result[2] = originZ;
    result[3] = 0.0f;  // not found

    float3 origin = (float3)(originX, originY, originZ);
    float3 movement = (float3)(movementX, movementY, movementZ);
    float3 tempPos = origin + movement;

    float bary[3];

    // First pass: check if movement destination is valid
    for (int t = 0; t < triangleCount; t++) {
        // Get vertex indices
        int idx0 = indices[t * 3 + 0] * 3;
        int idx1 = indices[t * 3 + 1] * 3;
        int idx2 = indices[t * 3 + 2] * 3;

        // Get vertex positions
        float3 v0 = (float3)(vertices[idx0], vertices[idx0 + 1], vertices[idx0 + 2]);
        float3 v1 = (float3)(vertices[idx1], vertices[idx1 + 1], vertices[idx1 + 2]);
        float3 v2 = (float3)(vertices[idx2], vertices[idx2 + 1], vertices[idx2 + 2]);

        // Calculate face normal
        float3 normal = calculate_normal(v0, v1, v2);
        if (normal.y < slopeThreshold) continue;

        // Calculate barycentric coordinates for temp position
        barycentric(v0.x, v0.z, v1.x, v1.z, v2.x, v2.z, tempPos.x, tempPos.z, bary);

        if (inside(bary)) {
            float yResult = bary[0] * v0.y + bary[1] * v1.y + bary[2] * v2.y + 1.7f;
            if (fabs(yResult - tempPos.y) < 0.5f) {
                result[0] = tempPos.x;
                result[1] = yResult;
                result[2] = tempPos.z;
                result[3] = 1.0f;  // Found
                return;
            }
        }
    }

    // Second pass: check for edge sliding
    for (int t = 0; t < triangleCount; t++) {
        // Get vertex indices
        int idx0 = indices[t * 3 + 0] * 3;
        int idx1 = indices[t * 3 + 1] * 3;
        int idx2 = indices[t * 3 + 2] * 3;

        // Get vertex positions
        float3 v0 = (float3)(vertices[idx0], vertices[idx0 + 1], vertices[idx0 + 2]);
        float3 v1 = (float3)(vertices[idx1], vertices[idx1 + 1], vertices[idx1 + 2]);
        float3 v2 = (float3)(vertices[idx2], vertices[idx2 + 1], vertices[idx2 + 2]);

        // Calculate face normal
        float3 normal = calculate_normal(v0, v1, v2);
        if (normal.y < slopeThreshold) continue;

        // Calculate barycentric coordinates for origin
        barycentric(v0.x, v0.z, v1.x, v1.z, v2.x, v2.z, origin.x, origin.z, bary);

        if (inside(bary)) {
            float yResult = bary[0] * v0.y + bary[1] * v1.y + bary[2] * v2.y + 1.7f;
            if (fabs(yResult - origin.y) < 0.1f) {
                // Normalize movement direction
                float movementLength = length(movement);
                float3 movementDir = normalize(movement);

                // Determine which edge we're near
                float3 edgeVector = (float3)(0.0f, 0.0f, 0.0f);
                if (bary[0] < 0.05f) {
                    edgeVector = (float3)(v1.x - v2.x, 0, v1.z - v2.z);
                } else if (bary[1] < 0.05f) {
                    edgeVector = (float3)(v2.x - v0.x, 0, v2.z - v0.z);
                } else if (bary[2] < 0.05f) {
                    edgeVector = (float3)(v1.x - v0.x, 0, v1.z - v0.z);
                }

                if (length(edgeVector) > 0.001f) {
                    edgeVector = normalize(edgeVector);

                    // Project movement onto edge
                    float dotProduct = dot(movementDir, edgeVector);
                    float3 projectedMovement = edgeVector * (dotProduct * movementLength);
                    float3 newPosition = origin + projectedMovement;

                    result[0] = newPosition.x;
                    result[1] = yResult;
                    result[2] = newPosition.z;
                    result[3] = 1.0f;  // Found
                    return;
                }
            }
        }
    }
}